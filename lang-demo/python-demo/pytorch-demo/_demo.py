import torch
import torchaudio
import torchvision

print(torch.__version__)
print(torch.cuda.is_available())
print(torchvision.__version__)
print(torchaudio.__version__)

# 输出示例：
# 2.9.0+cu130
# True
# 0.24.0+cu130
# 2.9.0+cu130
# 如果输出不一致，考虑全局安装的组件是否影响。