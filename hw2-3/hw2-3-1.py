# %%
import torch
import torch.nn as nn

class MyModel(nn.Module):
    def __init__(self):
        super(MyModel, self).__init__()

    def forward(self, A, B):
        # Perform matrix multiplication
        out = torch.matmul(A, B)
        return out

model = MyModel()
dummy_input1 = torch.randn(128, 128)
dummy_input2 = torch.randn(128, 128)
torch.onnx.export(model, (dummy_input1, dummy_input2), "model2.onnx", input_names=["A", "B"], output_names=["C"])


