"""主流程: request_verification_code -> auto_login -> get_recommend_friends_list

auto_login 会交互式提示输入验证码。在 TaQuDemo 目录下执行:
    python main.py
"""

import subprocess
import sys

from common.log import log

for module in (
    "api.request_verification_code",
    "api.auto_login",
    "api.get_recommend_friends_list",
):
    log("=" * 40, module)
    subprocess.run([sys.executable, "-m", module], check=True)
