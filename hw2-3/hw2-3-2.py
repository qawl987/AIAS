# %%
import torch
import torch.nn as nn

class SubMatrixMultiplication(nn.Module):
    def __init__(self):
        super(SubMatrixMultiplication, self).__init__()
        
    def forward(self, A, B):
        # Perform 2D matrix multiplication
        return torch.matmul(A, B)

class SplitMatrix(nn.Module):
    def __init__(self):
        super(SplitMatrix, self).__init__()
        
    def forward(self, x, row, col):
        row_half = row // 2
        col_half = col // 2
        x00 = x[:row_half, :col_half]
        x01 = x[row_half:row, :col_half]
        x10 = x[:row_half, col_half:col]
        x11 = x[row_half:row, col_half:col]
        return x00, x01, x10, x11

class MatrixSplitMultiplication(nn.Module):
    def __init__(self):
        super(MatrixSplitMultiplication, self).__init__()
        self.split_matrix = SplitMatrix()
        self.submatrix_mul = SubMatrixMultiplication()
    def forward(self, A, B):
        m, k = A.shape[0], A.shape[1]
        k, n = B.shape[0], B.shape[1]
        a00, a01, a10, a11 = self.split_matrix(A, m, k)
        b00, b01, b10, b11 = self.split_matrix(B, k, n)
        c00 = self.submatrix_mul(a00, b00) + self.submatrix_mul(a01, b10)
        c01 = self.submatrix_mul(a00, b01) + self.submatrix_mul(a01, b11)
        c10 = self.submatrix_mul(a10, b00) + self.submatrix_mul(a11, b10)
        c11 = self.submatrix_mul(a10, b01) + self.submatrix_mul(a11, b11)
        
        c0 = torch.cat([c00, c01], dim=1)
        c1 = torch.cat([c10, c11], dim=1)
        c = torch.cat([c0, c1], dim=0)
        return c

model = MatrixSplitMultiplication()
dummy_input1 = torch.randn(128, 128)
dummy_input2 = torch.randn(128, 128)
torch.onnx.export(model, (dummy_input1, dummy_input2), "matrixsplit.onnx", input_names=["A", "B"], output_names=["C"])


