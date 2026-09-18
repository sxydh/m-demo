"""主流程
login -> reg_init -> send_otp -> verify_otp
"""

import subprocess
import sys

for module in ("api.login", "api.reg_init", "api.send_otp", "api.verify_otp"):
    print("=" * 40, module)
    subprocess.run([sys.executable, "-m", module], check=True)
