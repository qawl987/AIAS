# %%
import torchvision.models as models

# 加載 GoogLeNet 模型
model = models.googlenet(pretrained=True)
print(model)

input_shape = (3, 224, 224)

# %%
total_params = sum(p.numel() for p in model.parameters())
print("Total number of parameters: ", total_params)

# %%



