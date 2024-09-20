# %%
import torchinfo
import onnx
import torchvision.models as models
models = models.mobilenet_v2(pretrained=True)
torchinfo.summary(models, (3, 224, 224), batch_dim=0, col_names=("input_size", "output_size", "num_params", "kernel_size", "mult_adds"), verbose=0)


