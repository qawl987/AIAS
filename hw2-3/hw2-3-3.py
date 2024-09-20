# %%
import torch
import torch.nn as nn
import torchinfo

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
        linear_tensor = super(MatrixSplitMultiplication, self).forward(input)
        self.compare(linear_tensor, c)
        return c

# %%
import torchvision.models as models
def replace_module(module):
    for child_name, child_module in module.named_children():
        # print(f"Layer: {child_name}, Type: {type(child_module).__name__}")
        if isinstance(child_module, nn.Linear):
            matrix_split_multiplication = MatrixSplitMultiplication(child_module.in_features, child_module.out_features)
            setattr(module, child_name, matrix_split_multiplication)
        else:
            replace_module(child_module)

model = models.alexnet(pretrained=True)
replace_module(model)
dummy_input = torch.randn(1, 3, 224, 224)
# torchinfo.summary(model, [(3, 224, 224)], col_names=("input_size", "output_size"), batch_dim=0, verbose=0)
torch.onnx.export(model, dummy_input, "changelinear.onnx", input_names=["Input"], output_names=["C"], verbose=0)


