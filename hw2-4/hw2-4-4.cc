#include <torch/script.h>
#include <torch/torch.h>
#include <iostream>
#include <iomanip>
std::string getOperatorType(std::string s)
{
    s = s.substr(0, s.find("\n"));
    size_t last_dot = s.rfind('.');
    size_t brace_pos = s.find('{');
    std::string result = s.substr(last_dot + 1, brace_pos - last_dot - 2);
    return result;
}

void getConstantParam(torch::jit::Node *node, const std::string &param_type_str, torch::jit::Stack *stack)
{
    if (node->hasAttributes())
    {
        auto attributes = node->attributeNames();
        auto primary_attr = attributes[0];

        if (strcmp(primary_attr.toUnqualString(), "value") == 0)
        {
            auto attr_kind = node->kindOfS("value");
            switch (attr_kind)
            {
            case torch::jit::AttributeKind::i:
                if (param_type_str.substr(0, 4) == "bool")
                {
                    stack->push_back(node->i(primary_attr) == 1);
                }
                else
                {
                    stack->push_back(node->i(primary_attr));
                }
                break;
            case torch::jit::AttributeKind::f:
                stack->push_back(node->f(primary_attr));
                break;
            case torch::jit::AttributeKind::t:
                stack->push_back(node->t(primary_attr));
                break;
            case torch::jit::AttributeKind::s:
                if (param_type_str.substr(0, 6) == "Device" && node->s(primary_attr) == "cpu")
                {
                    torch::Device device(torch::kCPU);
                    stack->push_back(torch::IValue(device));
                }
                break;
            }
        }
    }
    else
    {
        stack->push_back(torch::jit::IValue());
    }
}

void getListParam(torch::jit::Node *node, torch::jit::Stack *stack, std::map<std::string, torch::IValue> *input_map)
{
    std::vector<int64_t> integer_list;
    std::vector<float> float_list;
    std::vector<torch::Tensor> tensor_list;

    for (const auto &input : node->inputs())
    {
        // Find input map first if exist node name
        if (input_map->find(input->debugName()) != input_map->end())
        {
            if ((*input_map)[input->debugName()].isInt())
            {
                integer_list.push_back((*input_map)[input->debugName()].toInt());
            }
            else if ((*input_map)[input->debugName()].isTensor())
            {
                tensor_list.push_back((*input_map)[input->debugName()].toTensor());
            }
        }
        else
        {
            auto parent_node = input->node();
            auto attr_name = parent_node->attributeNames()[0];

            if (strcmp(attr_name.toUnqualString(), "value") == 0)
            {
                auto attr_type = parent_node->kindOfS("value");

                switch (attr_type)
                {
                case torch::jit::AttributeKind::i:
                    integer_list.push_back(parent_node->i(attr_name));
                    break;
                case torch::jit::AttributeKind::f:
                    float_list.push_back(parent_node->f(attr_name));
                    break;
                }
            }
        }
    }

    if (integer_list.size() == node->inputs().size())
    {
        stack->push_back(torch::jit::IValue(integer_list));
    }
    else if (float_list.size() == node->inputs().size())
    {
        stack->push_back(torch::jit::IValue(float_list));
    }
    else
    {
        stack->push_back(torch::jit::IValue(tensor_list));
    }
}

void getAttrParam(torch::jit::Node *node, const torch::jit::named_attribute_list &attr_list, torch::jit::Stack *stack)
{
    auto attr_name = node->s(node->attributeNames()[0]);
    for (const auto &attr : attr_list)
    {
        if (attr.name == attr_name)
        {
            stack->push_back(attr.value.toTensor());
            return;
        }
    }
}

void traverseModule(torch::jit::Module module, torch::Tensor *input_tensor, int *macs_sum)
{
    if (module.named_children().size() != 0)
    {
        for (const auto &sub_module : module.named_children())
        {
            traverseModule(sub_module.value, input_tensor, macs_sum);
        }
    }
    else
    {
        torch::jit::Stack stack;
        // Input map served for some operation need to save intermediate value for future used
        std::map<std::string, torch::IValue> input_map;
        auto original_input_size = input_tensor->sizes();
        auto op_type = getOperatorType(module.dump_to_str(1, 0, 0));
        if (op_type == "Linear" || op_type == "MatrixSplitMultiplication")
        {
            // Transfer tensor to (1, size) for Linear
            *input_tensor = input_tensor->view({1, input_tensor->numel()});
        }
        std::cout << op_type << std::endl;
        auto graph = module.get_method("forward").graph();
        for (const auto &node : graph->nodes())
        {
            // Seperate Tensor from list and save to input map
            if (node->kind() == torch::prim::ListUnpack)
            {
                // search tensor list in inputmap
                auto tensor_list = input_map[node->inputs()[0]->debugName()].toTensorList();
                for (int i = 0; i < tensor_list.size(); ++i)
                {
                    // Stores each tensor in input_map, associated with the corresponding output node name
                    input_map[node->outputs()[i]->debugName()] = torch::IValue(tensor_list[i]);
                }
            }
            else if (node->maybeOperator())
            {
                auto op = node->getOperation();
                auto schema = node->schema();
                stack.clear();
                auto node_inputs = node->inputs();
                int arg_index = 0;

                for (const auto &arg : schema.arguments())
                {
                    auto node_input = node_inputs[arg_index]->node();

                    switch (node_input->kind())
                    {
                    // If not the forth type before, it be something like split, push it from input_map
                    case torch::prim::Constant:
                        getConstantParam(node_input, arg.type()->str(), &stack);
                        break;
                    case torch::prim::ListConstruct:
                        getListParam(node_input, &stack, &input_map);
                        break;
                    case torch::prim::GetAttr:
                        getAttrParam(node_input, module.named_attributes(), &stack);
                        break;
                    case torch::prim::Param:
                        stack.push_back(*input_tensor);
                        break;
                    default:
                        stack.push_back(input_map[node_inputs[arg_index]->debugName()]);
                        break;
                    }

                    arg_index++;
                }
                op(stack);
                // Put output tensor to input_map
                input_map[node->outputs()[0]->debugName()] = stack.back();
            }
        }
        *input_tensor = stack.back().toTensor();
        auto output_size = input_tensor->sizes();
        if (op_type == "Conv2d")
        {
            int64_t batch_size = original_input_size[0];
            int64_t in_channels = original_input_size[1];
            int64_t out_channels = output_size[1];
            int64_t out_height = output_size[2];
            int64_t out_width = output_size[3];

            // Extract Conv2d parameters
            auto conv2d_module = module.attr("weight").toTensor();
            auto kernel_size = conv2d_module.sizes(); // [out_channels, in_channels, kernel_height, kernel_width]
            int64_t kernel_height = kernel_size[2];
            int64_t kernel_width = kernel_size[3];

            int64_t conv_macs = out_channels * in_channels * out_height * out_width * kernel_height * kernel_width;
            *macs_sum += conv_macs;

            std::cout << "MACs for this Conv2d: " << conv_macs << std::endl;
        }
        else if (op_type == "MatrixSplitMultiplication")
        {
            auto linear_size = module.attr("weight").toTensor().sizes();
            int64_t m = linear_size[0];
            int64_t n = linear_size[1];
            int64_t linear_macs = m * n;
            *macs_sum += linear_macs;
            std::cout << "MACs for this MatrixSplitMultiplication: " << linear_macs << std::endl;
        }
    }
}

int main(int argc, const char *argv[])
{
    if (argc != 2)
    {
        std::cerr << "usage: example-app <path-to-exported-script-module>\n";
        return -1;
    }

    torch::jit::script::Module module;
    try
    {
        module = torch::jit::load(argv[1]);
    }
    catch (const c10::Error &e)
    {
        std::cerr << "Error loading the model\n";
        return -1;
    }

    torch::Tensor input_tensor = torch::randn({1, 3, 224, 224});
    int total_macs = 0;

    for (const auto &sub_module : module.named_children())
    {
        traverseModule(sub_module.value, &input_tensor, &total_macs);
    }
    std::cout << "Total MACs: " << total_macs << std::endl;
    return 0;
}
