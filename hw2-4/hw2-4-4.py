# %%
import torch
import torch.nn as nn
import torchinfo
import random
import numpy as np
import torchvision.models as models
np.random.seed(0)
random.seed(0)
torch.manual_seed(0)
torch.backends.cudnn.deterministic = True
torch.backends.cudnn.benchmark = False

# %%
class MatrixSplitMultiplication(nn.Linear):
    def __init__(self, in_features, out_features, bias=True):
        super(MatrixSplitMultiplication, self).__init__(in_features, out_features, bias)
        
    def split_matrix(self, x, row, col):
        row_half = row // 2
        col_half = col // 2
        x00 = x[:row_half, :col_half]
        x01 = x[:row_half, col_half:col]
        x10 = x[row_half:row, :col_half]
        x11 = x[row_half:row, col_half:col]
        return x00, x01, x10, x11
    
    def compare(self, new_output, old_output):
        if torch.allclose(new_output, old_output, rtol=1e-2):
            print("The tensors are very close.")
        else:
            print("The tensors are not close.")

    def forward(self, input):
        a00, a01, a10, a11 = self.split_matrix(input, input.shape[0], input.shape[1])
        t_weight = torch.transpose(self.weight, 0, 1)
        b00, b01, b10, b11 = self.split_matrix(t_weight, t_weight.shape[0], t_weight.shape[1])
        c00 = torch.matmul(a00, b00) + torch.matmul(a01, b10)
        c01 = torch.matmul(a00, b01) + torch.matmul(a01, b11)
        c10 = torch.matmul(a10, b00) + torch.matmul(a11, b10)
        c11 = torch.matmul(a10, b01) + torch.matmul(a11, b11)
        c0 = torch.cat([c00, c01], dim=1)
        c1 = torch.cat([c10, c11], dim=1)
        c = torch.cat([c0, c1], dim=0)
        if self.bias is not None:
            c = torch.add(c, self.bias)
        # linear_tensor = super(MatrixSplitMultiplication, self).forward(input)
        # self.compare(linear_tensor, c)
        return c

# %%
def replace_module(module):
    for child_name, child_module in module.named_children():
        # print(f"Layer: {child_name}, Type: {type(child_module).__name__}")
        if isinstance(child_module, nn.Linear):
            matrix_split_multiplication = MatrixSplitMultiplication(child_module.in_features, child_module.out_features)
            matrix_split_multiplication.weight.data.copy_(child_module.weight.data)
            if child_module.bias is not None:
                matrix_split_multiplication.bias.data.copy_(child_module.bias.data)
            setattr(module, child_name, matrix_split_multiplication)
        else:
            replace_module(child_module)

# %%
model = models.alexnet(pretrained=True)
replace_module(model)
# model.eval()
# with torch.no_grad():
#     # Perform inference
#     output = model(input_tensor)
#     print(output)
dummy_input = torch.randn(1, 3, 224, 224)
# torchinfo.summary(model, [(3, 224, 224)], depth=3, col_names=("input_size", "output_size"), batch_dim=0, verbose=0)
# torch.onnx.export(model, dummy_input, "hw2-3-3.onnx", input_names=["Input"], output_names=["C"], verbose=0)

# %%
import torch
import torchvision.models as models

import torch.nn.functional as F
import torch.nn as nn
def calculate_output_shape(input_shape, layer):
    # Calculate the output shape for Conv2d, MaxPool2d, and Linear layers
    if isinstance(layer, (nn.Conv2d, nn.MaxPool2d)):
        kernel_size = (
            layer.kernel_size
            if isinstance(layer.kernel_size, tuple)
            else (layer.kernel_size, layer.kernel_size)
        )
        stride = (
            layer.stride
            if isinstance(layer.stride, tuple)
            else (layer.stride, layer.stride)
        )
        padding = (
            layer.padding
            if isinstance(layer.padding, tuple)
            else (layer.padding, layer.padding)
        )
        dilation = (
            layer.dilation
            if isinstance(layer.dilation, tuple)
            else (layer.dilation, layer.dilation)
        )

        output_height = (
            input_shape[1] + 2 * padding[0] - dilation[0] * (kernel_size[0] - 1) - 1
        ) // stride[0] + 1
        output_width = (
            input_shape[2] + 2 * padding[1] - dilation[1] * (kernel_size[1] - 1) - 1
        ) // stride[1] + 1
        return (
            layer.out_channels if hasattr(layer, "out_channels") else input_shape[0],
            output_height,
            output_width,
        )
    elif isinstance(layer, nn.Linear):
        # For Linear layers, the output shape is simply the layer's output features
        return (layer.out_features,)
    else:
        return input_shape


def calculate_macs(layer, input_shape, output_shape):
    # Calculate MACs for Conv2d and Linear layers
    if isinstance(layer, nn.Conv2d):
        kernel_ops = (
            layer.kernel_size[0]
            * layer.kernel_size[1]
            * (layer.in_channels / layer.groups)
        )
        output_elements = output_shape[1] * output_shape[2]
        macs = int(kernel_ops * output_elements * layer.out_channels)
        return macs
    elif isinstance(layer, nn.Linear):
        # For Linear layers, MACs are the product of input features and output features
        macs = int(layer.in_features * layer.out_features)
        return macs
    else:
        return 0
# Initial input shape
input_shape = (3, 224, 224)
total_macs = 0

# Iterate through the layers of the model
for name, layer in model.named_modules():
    if isinstance(layer, (nn.Conv2d, nn.MaxPool2d, nn.ReLU, nn.Linear)):
        output_shape = calculate_output_shape(input_shape, layer)
        macs = calculate_macs(layer, input_shape, output_shape)
        total_macs += macs
        if isinstance(layer, (nn.Conv2d, nn.Linear)):
            print(
                f"Layer: {name}, Type: {type(layer).__name__}, Input Shape: {input_shape}, Output Shape: {output_shape}, MACs: {macs}"
            )
        elif isinstance(layer, nn.MaxPool2d):
            # Also print shape transformation for MaxPool2d layers (no MACs calculated)
            print(
                f"Layer: {name}, Type: {type(layer).__name__}, Input Shape: {input_shape}, Output Shape: {output_shape}, MACs: N/A"
            )
        input_shape = output_shape  # Update the input shape for the next layer

print(f"Total MACs: {total_macs}")

# %%



