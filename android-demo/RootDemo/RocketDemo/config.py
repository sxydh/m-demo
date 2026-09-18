import os

BASE_URL = os.environ.get("ROCKET_BASE_URL", "https://rocket-ekycservice.dutchbanglabank.com")
BASE_URL_TXN = os.environ.get("ROCKET_BASE_URL_TXN", "https://rocket-consumertxnservice.dutchbanglabank.com")
PHONE = os.environ.get("ROCKET_PHONE", "01833856750")
DEVICE_ID = os.environ.get("ROCKET_DEVICE_ID", PHONE)
DEVICE_INFO = os.environ.get("ROCKET_DEVICE_INFO", "BP2A.250605.031.A2-Pixel 7 Pro")
CLIENT_VERSION = os.environ.get("ROCKET_CLIENT_VERSION", "3.1.5")
CLIENT_LANGUAGE = os.environ.get("ROCKET_CLIENT_LANGUAGE", "EN")
GATEWAY_SESSION_ID = os.environ.get("ROCKET_GATEWAY_SESSION_ID", "123")
PRE_SESSION_ID = os.environ.get("ROCKET_PRE_SESSION_ID", "1234")
ROCKET_SESSION_ID = os.environ.get("ROCKET_ROCKET_SESSION_ID", "7654321")
TIMEOUT = 20

OUTER_PASSWORD = "sdfheu$*sdfGw2"
BASIC_CREDENTIALS = "rocket-app-enc:Rk#t40ckEt*#"
CHANNEL_ID = "ROCKET_APP_USER"

OUTPUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "_Temp")
os.makedirs(OUTPUT_DIR, exist_ok=True)
