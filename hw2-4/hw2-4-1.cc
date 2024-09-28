#include <torch/script.h> // One-stop header.
#include <torch/csrc/jit/api/module.h>
#include <torch/csrc/jit/jit_log.h>
#include <torch/csrc/jit/ir/ir.h>

#include <iostream>
#include <memory>

using namespace torch::jit;

int main(int argc, const char *argv[])
{
    // param_size = sum(p.numel() * p.element_size() for p in model.parameters())
    // print("Total memory for parameters: ", param_size)
    if (argc != 2)
    {
        std::cerr << "usage: example-app <path-to-exported-script-module>\n";
        return -1;
    }

    set_jit_logging_levels("GRAPH_DUMP");

    torch::jit::script::Module module;
    try
    {
        // Deserialize the ScriptModule from a file using torch::jit::load().
        module = torch::jit::load(argv[1]);
    }
    catch (const c10::Error &e)
    {
        std::cerr << "error loading the model\n";
        return -1;
    }

    std::cout << "load the torchscript model, " + std::string(argv[1]) + ", successfully \n";
    int64_t size_cnt = 0;
    for (const auto &param : module.parameters())
    {
        int64_t numel = param.numel();
        int64_t element_size = param.element_size();
        int64_t total_size_bytes = numel * element_size;
        size_cnt += total_size_bytes;
    }
    std::cout << size_cnt;
}
