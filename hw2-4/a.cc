#include "IRTracer.hh"
#include <bits/stdc++.h>
#include <torch/csrc/jit/frontend/tracer.h>
#include <torch/csrc/jit/ir/alias_analysis.h>
#include <torch/csrc/jit/ir/graph_utils.h>
#include <torch/csrc/jit/passes/onnx.h>
#include <torch/csrc/onnx/onnx.h>
#include <torch/script.h>
#include <torch/torch.h>

#include <iostream>
#include <memory>
#include <unordered_map>
#include <string>
#include <stdlib.h>
torch::jit::Stack stack;
std::string getOperatorType(std::string s)
{
    std::string delimiter = "\n";
    s = s.substr(0, s.find(delimiter));
    size_t last_dot = s.rfind('.');
    size_t brace_pos = s.find('{');
    std::string result = s.substr(last_dot + 1, brace_pos - last_dot - 2);
    // std::cout << "Substring: " << result << std::endl;
    return result;
}

void IRTracer::getConstantParam(torch::jit::Node *parent_node, std::string parme_type_string)
{
    // %21 : bool = prim::Constant[value=1]()
    if (parent_node->hasAttributes())
    {
        auto parent_attr_names = parent_node->attributeNames();
        auto parent_attr = parent_attr_names[0];
        if (strcmp(parent_attr.toUnqualString(), "value") == 0)
        {
            auto parent_attr_kind = parent_node->kindOfS("value");
            switch (parent_attr_kind)
            {
            case torch::jit::AttributeKind::i:
            {
                if (parme_type_string.substr(0, 4) == "bool")
                {
                    stack.push_back(parent_node->i(parent_attr) == 1);
                }
                else
                {
                    stack.push_back(parent_node->i(parent_attr));
                }
                break;
            }
            case torch::jit::AttributeKind::f:
            {
                stack.push_back(parent_node->f(parent_attr));
                break;
            }
            case torch::jit::AttributeKind::t:
            {
                stack.push_back(parent_node->t(parent_attr));
                break;
            }
            }
        }
    }
    else
    {
        // Constant none value
        stack.push_back(torch::jit::IValue());
    }
}
void IRTracer::getListParam(torch::jit::Node *parent_node)
{
    // %11 : int[] = prim::ListConstruct(%9, %9)
    std::vector<int64_t> list_i;
    std::vector<float> list_f;
    std::vector<torch::Tensor> list_tensor;
    for (const auto &parent_in : parent_node->inputs())
    {
        if (map_method_outputs.find(parent_in) != map_method_outputs.end())
        { /* cat input tensor[] */
            list_tensor.push_back(map_method_outputs[parent_in].toTensor());
        }
        else
        {
            auto grand_node = parent_in->node();
            auto grand_node_attr = grand_node->attributeNames()[0];

            if (strcmp(grand_node_attr.toUnqualString(), "value") == 0)
            {
                auto grand_node_kind = grand_node->kindOfS("value");

                switch (grand_node_kind)
                {
                case torch::jit::AttributeKind::i:
                    list_i.push_back(grand_node->i(grand_node_attr));
                    break;
                case torch::jit::AttributeKind::f:
                    list_f.push_back(grand_node->f(grand_node_attr));
                    break;
                }
                // std::cout << node->inputs[i].
            }
        }
    }
    if (list_i.size() == parent_node->inputs().size())
        stack.push_back(torch::jit::IValue(list_i));
    else if (list_f.size() == parent_node->inputs().size())
        stack.push_back(torch::jit::IValue(list_f));
    else
        stack.push_back(torch::jit::IValue(list_tensor)); // 推回空 list
}

void IRTracer::getGetAttrParam(torch::jit::Node *parent_node, torch::jit::named_attribute_list attribut_list)
{
    bool isParameterArg = false;
    at::Tensor parameter;
    auto parent_attr_name = parent_node->s(parent_node->attributeNames()[0]);
    for (const auto &param : attribut_list)
    {
        // std::cout << param.name << "-" << in.name() << " ";
        if (param.name == parent_attr_name)
        {
            isParameterArg = true;
            // std::cout << param.value;
            parameter = param.value.toTensor();
            break;
        }
    }
    stack.push_back(parameter);
}

torch::Tensor *IRTracer::prepareChildInput(torch::Tensor *currentInput, torch::jit::Value *forward_input)
{
    if (forward_input->node()->kind() == torch::prim::Param)
    {
        return currentInput;
    }
    else
    {
        return &map_method_outputs[forward_input].toTensor();
    }
}

// Recursive DFS function to process modules and submodules
torch::Tensor IRTracer::dfs_module(const torch::jit::Module &module, torch::Tensor input_tensor)
{
    std::cout << "dfs" << std::endl;
    for (const auto &x : module.named_children())
    {
        auto conv_module = x.value;
        std::cout << "Module name: " << x.name << std::endl;
        // std::cout << typeid(conv_module).name() << std::endl;
        // x.value.dump(1, 0, 0);
    }
    // Check if the module is of Sequential type (torch::nn::Sequential in PyTorch C++)
    if (module.named_children().size() != 0)
    { // Check if the module has submodules

        // module.dump(1, 0, 0);
        torch::Tensor sequential_input = input_tensor;

        // Iterate over all submodules in the Sequential container
        for (const auto &sub_module : module.named_children())
        {
            std::cout << "submodule" << sub_module.name << std::endl;
            // Recursively call dfs_module for each submodule
            sequential_input = dfs_module(sub_module.value, sequential_input);
        }

        return sequential_input; // Final output after processing all submodules
    }
    else
    {

        auto operator_type = getOperatorType(module.dump_to_str(1, 0, 0));
        if (operator_type == "MatrixSplitMultiplication")
        {
            std::cout << "MatrixSplitMultiplication" << std::endl;
            for (const auto &param : module.named_attributes())
            {
                std::cout << "attribute: " << param.name << std::endl;
            }
            for (const auto &param : module.named_parameters())
            {
                std::cout << "parameter: " << param.name << std::endl;
                std::cout << param.value.sizes() << std::endl;
            }
            for (const auto &param : module.named_buffers())
            {
                std::cout << "buffer: " << param.name << std::endl;
                std::cout << param.value.sizes() << std::endl;
            }
        }

        if (operator_type == "BatchNorm2d")
        {
            std::cout << "BatchNorm2d" << std::endl;
            std::cout << operator_type << " " << input_tensor.sizes() << " " << input_tensor.sizes() << std::endl;
            return input_tensor;
        }
        else if (operator_type == "MaxPool2d")
        {
            std::cout << "MaxPool2d" << std::endl;
            std::cout << operator_type << " " << input_tensor.sizes() << " " << input_tensor.sizes() << std::endl;
            return input_tensor;
        }
        else if (operator_type == "ReLU")
        {
            std::cout << "ReLU" << std::endl;
            std::cout << operator_type << " " << input_tensor.sizes() << " " << input_tensor.sizes() << std::endl;
            return input_tensor;
        }
        else if (operator_type == "Linear" || operator_type == "MatrixSplitMultiplication")
        {
            std::cout << "Linear | MatrixSplitMultiplication" << std::endl;
            auto dim = input_tensor.numel();
            input_tensor = input_tensor.view({1, dim});
            std::cout << operator_type << " " << input_tensor.sizes() << " " << input_tensor.sizes() << std::endl;
            return input_tensor;
        }
        else if (operator_type == "Conv2d")
        {
            std::cout << "Conv2d" << std::endl;
            auto conv_graph = module.get_method("forward").graph();
            auto conv_nodes = conv_graph->nodes();
            torch::Tensor output_tensor;
            for (const auto &n : conv_nodes)
            {
                if (n->maybeOperator())
                {
                    auto operation = n->getOperation();
                    auto schema = n->schema();

                    std::cout << "schema:" << std::endl;
                    std::cout << schema << std::endl;

                    stack.clear();

                    auto input_nodes = n->inputs();
                    int idx = 0;
                    for (const auto &param : schema.arguments())
                    {
                        auto input_node = input_nodes[idx]->node();
                        idx++;

                        switch (input_node->kind())
                        {
                        case torch::prim::Constant:
                        {
                            getConstantParam(input_node, param.type()->str());
                            break;
                        }

                        case torch::prim::ListConstruct:
                        {
                            getListParam(input_node);
                            break;
                        }

                        case torch::prim::GetAttr:
                        {
                            getGetAttrParam(input_node, module.named_attributes());
                            break;
                        }

                        case torch::prim::Param:
                        {
                            stack.push_back(input);
                            break;
                        }
                        }
                    }
                    operation(stack);
                    torch::jit::IValue output = stack.back();
                    output_tensor = output.toTensor();
                }
            }
            // torch::Tensor output_tensor = count_tensor(module, input_tensor);
            std::cout << operator_type << " " << input_tensor.sizes() << " " << input_tensor.sizes() << std::endl;
            std::cout << operator_type << " " << output_tensor.sizes() << " " << output_tensor.sizes() << std::endl;
            return output_tensor;
        }
        // If it's a leaf module, just run forward on the module
        // return count_tensor(module, input_tensor);
    }
}

int main(int argc, const char *argv[])
{
    std::stack<int> mystack;
    int a = 0;
    if (argc != 2)
    {
        std::cerr << "usage: example-app <path-to-exported-script-module>\n";
        return -1;
    }
    try
    {
        // Deserialize the ScriptModule from a file using torch::jit::load().
        IRTracer *tracer = new IRTracer("");
        tracer->loadModel(argv[1]);
        torch::Tensor input = torch::randn({1, 3, 224, 224});
        tracer->run(input);
    }
    catch (const c10::Error &e)
    {
        std::cerr << e.msg() << std::endl;
        return -1;
    }
    return 0;
}

void IRTracer::loadModel(std::string modelFileName)
{
    try
    {
        // Deserialize the ScriptModule from a file using torch::jit::load().
        this->module = torch::jit::load(modelFileName);
    }
    catch (const c10::Error &e)
    {
        std::cerr << "error loading the model\n";
    }
    std::cout << "load the torchscript model, " + modelFileName + ", successfully \n";
}

void IRTracer::run(torch::Tensor input)
{
    auto output_tensor = dfs_module(this->module, input);
    // std::cout << "output tensor:" << output_tensor.sizes() << std::endl;
}