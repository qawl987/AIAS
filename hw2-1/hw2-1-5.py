# %%
import torchvision.models as models
import torch

# Load a pre-trained AlexNet model
model = models.googlenet(pretrained=True)
model.eval()

# %%
import torch.nn.functional as F
import torch.nn as nn
# Define a hook function
def get_activation(name):
    def hook(model, input, output):
        activation[name] = output.detach()
    return hook


# Dictionary to store activations from each layer
activation = {}
# Register hook to each linear layer
for layer_name, layer in model.named_modules():
    if isinstance(layer, nn.BatchNorm2d):
        # Register forward hook
        layer.register_forward_hook(get_activation(layer_name))

# Run model inference
data = torch.randn(1, 3, 224, 224)
output = model(data)

# Access the saved activations
for layer in activation:
    print(f"Activation from layer {layer}: {activation[layer]}")

# %%



