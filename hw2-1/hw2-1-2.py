# %%
import torchvision.models as models

# 加載 GoogLeNet 模型
model = models.googlenet(pretrained=True)
print(model)

input_shape = (3, 224, 224)

# %%
# Calculating the size of the model's parameters in bytes
param_size = sum(p.numel() * p.element_size() for p in model.parameters())
print("Total memory for parameters: ", param_size)


