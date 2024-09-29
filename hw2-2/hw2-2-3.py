# %%
import torchvision.models as models
import torch
import torch.nn as nn
# Load a pre-trained AlexNet model
model = models.mobilenet_v2(pretrained=True)
model.eval()

# %%
# Dictionary to store activations from each layer
activation = {}
# Define a hook function
def get_activation(name):
    def hook(model, input, output):
        activation[name] = output.detach()
    return hook


# %%
def dfs_module(module_name, module):
    # print(len(list(module.named_children())))
    for sub_module_name, sub_module in module.named_children():
        dfs_module(module_name + "." + sub_module_name, sub_module)
    module.register_forward_hook(get_activation(module_name))

# %%
# Register hook to each linear layer
for layer_name, layer in model.named_children():
    dfs_module(layer_name, layer)

# %%
# Run model inference
data = torch.randn(1, 3, 224, 224)
output = model(data)
total_activation_memory_usage = 0
# Access the saved activations
for layer in activation:
    print(f"Activation from layer {layer}: {activation[layer].shape}")
for layer in activation:
    total_activation_memory_usage += torch.numel(activation[layer]) * 4
print("total_activation_memory_usage: ", total_activation_memory_usage)


