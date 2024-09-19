# %%
import torch
import torchvision.models as models

# Use an existing model from Torchvision, note it 
# will download this if not already on your computer (might take time)
model = models.googlenet(pretrained=True)


# %%
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
    # elif isinstance(layer, nn.)
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

# %%
# Initial input shape
input_shape = (3, 224, 224)
total_macs = 0

inception_shape = (0, 0, 0)
branch_name = ""

def count_inception_shape(layer, input_shape):
    print(input_shape)
    inception_shape = input_shape
    for name, sub_layer in layer.named_children():
        print(name)
        print({type(sub_layer).__name__})
        # print(sub_layer.out_features)
        input_shape_2 = input_shape
        output_shape_2 = input_shape_2
        for name, sub_sub_layer in sub_layer.named_children():
            print(name)
            print({type(sub_sub_layer).__name__})
            if isinstance(sub_sub_layer, (nn.Conv2d, nn.MaxPool2d, nn.ReLU, nn.Linear)):
                output_shape_2 = calculate_output_shape(input_shape_2, sub_sub_layer)
                input_shape_2 = output_shape_2  # Update the input shape for the next layer
            else:
                output_shape_2 = input_shape_2
            print(output_shape_2)
        inception_shape = inception_shape + output_shape_2
    print(
        f"Layer: {name}, Type: {type(layer).__name__}, Input Shape: {input_shape}, Output Shape: {inception_shape}, MACs: {macs}"
    )
    # for name, sub_layer in layer.named_children():
    #     for name, sub_sub_layer in sub_layer.named_children():
    #         if isinstance(layer, (nn.Conv2d, nn.MaxPool2d, nn.ReLU, nn.Linear)):
    #             pass
    #         last_shape = sub_sub_layer.out_features
    #     # last_shape = sub_layer.named_children()[-1].out_features
    #     # if isinstance(sub_layer, (nn.Conv2d, nn.MaxPool2d, nn.ReLU, nn.Linear)):
    #     #     inception_shape = inception_shape + sub_layer.out_features
    #     inception_shape = inception_shape + last_shape
    
# Iterate through the layers of the model
model.named_children()
for name, layer in model.named_modules():
    print(name)
    if name[:9] == "inception" and len(name) == 11:
        count_inception_shape(layer, input_shape)
        
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
import torchinfo
torchinfo.summary(model, (3, 224, 224), batch_dim=0, col_names=("input_size", "output_size", "num_params", "kernel_size", "mult_adds"), verbose=0)


