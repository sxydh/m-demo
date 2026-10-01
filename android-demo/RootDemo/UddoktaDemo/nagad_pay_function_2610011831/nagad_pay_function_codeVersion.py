"""
Uddokta (Nagad) 支付模块

转换自: Uddokta.py
转换日期: 2025-01-10
转换模式: 完整模式（提示词1-3）

功能:
- 登录: do_login (首次OTP验证登录 + 二次mpaId登录)
- 查账: do_check_balance, do_lookup_transactions
- 转账: do_transfer (Cash-In)
- 状态管理: 全部使用session（无成员变量保存状态）

注意:
- 所有运行时状态保存在session中
- 设备指纹在do_login中生成
- RSA512密钥对在initialize_wallet中生成
- ATC严格递增管理
"""

import hashlib
import json
import os
import re
import secrets
import struct
import random
import binascii
from decimal import Decimal, ROUND_HALF_UP
from typing import List, Optional, Dict, Any
from datetime import datetime
from Crypto.Cipher import DES
from cryptography.hazmat.primitives.ciphers import Cipher, modes
from cryptography.hazmat.decrepit.ciphers.algorithms import TripleDES
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.primitives.asymmetric import padding as asym_padding
from cryptography.hazmat.backends import default_backend
from cryptography import x509

# ===== 框架导入（对齐线上 JazzCash / BKashAgent）=====
from mp_sdk.interfaces.pay_function import PayFunctionBase
from mp_sdk.models.pay_context import PayContext
from mp_sdk.models.action_result import ActionResult, ActionResultT
from mp_sdk.models.parsed_transaction import ParsedTransaction
from mp_sdk.models.payout_item import PayoutItem
from mp_sdk.models.payout_result import PayoutResult
from mp_sdk.decorators.bank_automation import BankAutomation

# ===== 内嵌证书（sign_cert.der，base64编码）=====
_EMBEDDED_SIGN_CERT_DER_B64 = (
    "MIIEmjCCBAOgAwIBAgIBCjANBgkqhkiG9w0BAQUFADA2MQswCQYDVQQGEwJCRDEMMAoGA1UECgwDS1NMMQwwCgYDVQQLDANSJkQx"
    "CzAJBgNVBAMMAkNBMB4XDTE2MDQxOTEwMTIwOFoXDTE2MTAxNjEwMTIwOFowIjELMAkGA1UECwwCY2ExEzARBgNVBAMMCk5vcnVu"
    "IE5vYmkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCzuZCjRLvpQWDzYLH7oNhaZNo3/+U0gqgWKKeWAyjxn7C7rZ0Q"
    "nayDFl/inJtDecQm04bDXcvne4AZUlslaQDdj8VhKOBZGFKBWAbjEFmiDQ9MHNEMCe3zQxV4cwrt9bKUi1w7XqL6H2JhrxjoBm4a4"
    "n7jVty7d1MmrQH5xZTbNiiPiXeu+HJdUnNu40ZIZM3jVsfgf1APGD+umdy1nRaSHItOost1z/e+fxqCFjGgqiNd5DC6cMpZtmG/fT"
    "alDenKYBHp/2KbWbb69Jy1joMauUI983qoGic2Rt8G1/wiHTGHJ5AxhL4+psoguYXGF8EiUOd6n87HZq9luf1mihG1AgMBAAGjgg"
    "JGMIICQjBFBgNVHR8EPjA8MDqgOKA2hjRsZGFwOi8vbG9jYWxob3N0OjM4OS9jbj1kcDFwMSxvdT1jYSxkYz1tYXhjcmMsZGM9Y29t"
    "MA8GA1UdDwEB/wQFAwMH/4AwdQYDVR0lBG4wbAYIKwYBBQUHAwQGCCsGAQUFBwMCBgorBgEEAYI3FAICBggrBgEFBQcDBgYIKwYB"
    "BQUHAwkGCCsGAQUFBwMIBggrBgEFBQcDAQYIKwYBBQUHAwMGCCsGAQUFBwMHBggrBgEFBQcDBQYEVR0lADBiBgNVHSMEWzBZgBRA"
    "gRo6oZbO/F1Gy3DgQS/HqH8J+qE+pDwwOjELMAkGA1UEBhMCQkQxDDAKBgNVBAoMA0tTTDEMMAoGA1UECwwDUiZEMQ8wDQYDVQQD"
    "DAZSb290Q0GCAQEwHQYDVR0OBBYEFPjU2ETCOEST8rW3QrpVQhSzqs9oMFwGA1UdIAEB/wRSMFAwTgYFKgMGCAswRTAgBggrBgEF"
    "BQcCARYUaHR0cDovL3d3dy5rb25haS5jb20wIQYIKwYBBQUHAgIwFQwTS09OQSBDQSBVU0VSIE5PVElDRTAeBgNVHRIEFzAVoBMG"
    "CSqDGoyaRAoBAaAGMAQMAkNBMHAGA1UdEQRpMGeBFW5vcnVuLm5vYmlAa3NsZGV2LmNvbaBOBgkqgxqMmkQKAQGgQTA/DApOb3J1"
    "biBOb2JpMDEwLwYKKoMajJpECgEBATAhMAcGBSsOAwIaoBYEFNdyBmTxAmKhyFWeI/U1pr2TvWmXMA0GCSqGSIb3DQEBBQUAA4GB"
    "AITvAzPZzh8fabEdl8pWTVT2GhYEqLGZiWeXk68z3MjYDSIzXHRHopTAY27y1KPJacSdF+OMIJDu0oBIZo85JAdC61XsMkZJ4Z5ig8"
    "QrZvCTCom/UO7XwDij+ixHOfU08CdTIjB5Wax1O9CKjsN7g/syP6wq2PA966/AFx+ozAzv"
)


@BankAutomation("NagadUddokta")
class NagadAgentPayFunction(PayFunctionBase):
    """Uddokta (Nagad) 支付模块"""
    
    def __init__(self):
        """初始化Uddokta支付模块
        
        ⚠️ 构造函数已大幅简化：
        - 删除所有成员变量（用户参数、运行时状态）
        - 删除文件操作（_load_user_data）
        - 删除requests.Session
        - 所有运行时状态都存储在session中
        """
        super().__init__()
        
        # TODO-③: ✅ 保留固定配置常量
        self.manufacturer = "Google"
        self.PhoneModel = "Pixel 6"
        self.build_product = "oriole"
        self.macAddress = "02:00:00:00:00:00"
        self.X_KM_User_AspId = "100012345612345"
        self.User_Agent = "okhttp/3.14.9"
        self.appUA = "ANDROID/1120"
        self.osVersion = "14"
        
        # ⚠️ 不保存运行时状态到成员变量
    
    # ═══════════════════════════════════════════════
    # 会话管理方法（状态读写）
    # ═══════════════════════════════════════════════
    
    async def _read_login_data(self, context: PayContext) -> dict:
        """从session读取登录数据
        
        Returns:
            包含所有运行时状态的字典：
            - userId, authSession, authToken, refreshToken
            - walletKeys, mpaId, par, atc
        """
        result = await context.session_manager.get_session_data("nagad_login_data")
        if not result or not result.is_success or not result.data:
            return {}
        
        try:
            return json.loads(result.data) if isinstance(result.data, str) else dict(result.data)
        except Exception:
            return {}
    
    async def _update_login_data(self, context: PayContext, updates: dict):
        """更新session中的登录数据
        
        Args:
            updates: 要更新的字段字典
        """
        current = await self._read_login_data(context)
        try:
            current.update(updates or {})
        except Exception:
            pass

        # ⚠️ 持久化必须保证可JSON序列化，否则 set_session_data 会失败导致状态丢失
        def _json_safe(v: Any) -> Any:
            if v is None or isinstance(v, (str, int, float, bool)):
                return v
            if isinstance(v, bytes):
                return v.hex()
            if isinstance(v, datetime):
                return v.isoformat()
            if isinstance(v, (list, tuple, set)):
                return [_json_safe(x) for x in list(v)]
            if isinstance(v, dict):
                out: Dict[str, Any] = {}
                for kk, vv in v.items():
                    out[str(kk)] = _json_safe(vv)
                return out
            # 兜底：转字符串，避免序列化报错
            try:
                return str(v)
            except Exception:
                return ""

        payload = None
        try:
            payload = json.dumps(current, ensure_ascii=False)
        except Exception:
            payload = json.dumps(_json_safe(current), ensure_ascii=False)

        await context.session_manager.set_session_data("nagad_login_data", payload)

    def _maybe_fix_utf8_mojibake(self, s: str) -> str:
        """尝试将错误解码导致的乱码修复为UTF-8孟加拉语文本（不保证100%命中，但尽量恢复可读性）。"""
        if not isinstance(s, str):
            return str(s)
        text = s.strip()
        if not text:
            return text
        # 常见特征：出现大量 "Ã", "Â", "à¦" 或 Unicode replacement char
        try:
            if ("�" in text) or ("Ã" in text) or ("Â" in text) or ("à¦" in text):
                fixed = text.encode("latin1", errors="ignore").decode("utf-8", errors="ignore").strip()
                return fixed or text
        except Exception:
            pass
        return text

    def _extract_message_field(self, payload: Any) -> str:
        """从API返回JSON中提取可读错误信息（优先 message 字段）。"""
        if not payload:
            return ""
        if isinstance(payload, dict):
            # 优先级：message -> statusDesc -> reason -> error
            for k in ["message", "messageBn", "message_bn", "statusDesc", "status_desc", "reason", "error", "error_description"]:
                v = payload.get(k)
                if isinstance(v, str) and v.strip():
                    return self._maybe_fix_utf8_mojibake(v)
            # 兼容嵌套结构
            for k in ["data", "error", "errors", "executionStatus"]:
                v = payload.get(k)
                if isinstance(v, dict):
                    inner = self._extract_message_field(v)
                    if inner:
                        return inner
                if isinstance(v, list) and v:
                    for item in v:
                        inner = self._extract_message_field(item)
                        if inner:
                            return inner
        if isinstance(payload, str):
            return self._maybe_fix_utf8_mojibake(payload)
        return self._maybe_fix_utf8_mojibake(str(payload))

    async def _http_error_text(self, response: Any) -> str:
        """为 HTTP>=400 生成更可读的错误文本（提取 message 并尝试UTF-8纠偏）。"""
        status = getattr(response, "status_code", "")
        payload = None
        try:
            payload = await response.json()
        except Exception:
            payload = None

        msg = self._extract_message_field(payload)
        if not msg:
            try:
                raw_text = getattr(response, "text", "")
                if isinstance(raw_text, bytes):
                    raw_text = raw_text.decode("utf-8", errors="ignore")
                if isinstance(raw_text, str) and raw_text.strip():
                    msg = self._maybe_fix_utf8_mojibake(raw_text)
            except Exception:
                msg = ""

        if msg:
            return f"HTTP {status}: {msg}"
        return f"HTTP {status}"

    def _normalize_payout_error_text(self, message: str) -> str:
        return " ".join(str(message or "").split()).lower()

    def _is_definite_payout_error_message(self, message: str) -> bool:
        """需自动驳回的明确失败文案。"""
        normalized = self._normalize_payout_error_text(message)
        if not normalized:
            return False
        patterns = [
            # 对方账户不行 / 账号不一致
            "customer not found",
            "তথ্য পুনরায় যাচাই করুন এবং আবার চেষ্টা করুন",
            "অ্যাকাউন্ট নাম্বার সঠিক নয় অথবা নিষ্ক্রিয় আছে",
            "গ্রাহক টি রেসট্রিকটেড",
        ]
        return any(p in normalized for p in patterns)

    def _is_redispatch_payout_error_message(self, message: str) -> bool:
        """需自动重派的失败文案。"""
        normalized = self._normalize_payout_error_text(message)
        if not normalized:
            return False
        patterns = [
            # 达到限额
            "লেনদেনের দৈনিক সীমা অতিক্রম করেছেন",
            "মাসিক লেনদেনের সর্বোচ্চ অনুমোদিত সীমা",
            "transaction is not possible due to limit issue of this account",
            # 余额不足
            "পর্যাপ্ত ব্যালেন্স নেই",
        ]
        return any(p in normalized for p in patterns)

    def _build_payout_failure_result(self, message: str, fallback_message: str) -> PayoutResult:
        msg = str(message or "").strip() or fallback_message
        if self._is_redispatch_payout_error_message(msg):
            return PayoutResult.redispatch(msg)
        if self._is_definite_payout_error_message(msg):
            return PayoutResult.definite_failure(msg)
        return PayoutResult.uncertain_failure(msg)

    def _wallet_keys_for_session(self, wallet_keys: dict) -> dict:
        """将 wallet_keys 裁剪成可持久化版本（避免 private_key 对象导致 JSON 序列化失败）"""
        if not isinstance(wallet_keys, dict):
            return {}
        keep_keys = ["private_key_hex", "public_key_hex", "csr_hex", "user_id"]
        out: Dict[str, Any] = {}
        for k in keep_keys:
            if k in wallet_keys and wallet_keys.get(k) is not None:
                out[k] = wallet_keys.get(k)
        return out

    def _extract_credential_value(self, raw_value: Any, key: str) -> str:
        """
        兼容凭证输入格式：
        1) 纯字符串：如 "123456"
        2) dict：如 {"otp":"123456"}
        3) JSON字符串：如 '{"otp":"123456"}'
        """
        if raw_value is None:
            return ""
        try:
            if isinstance(raw_value, dict):
                return str(raw_value.get(key, "") or "").strip()

            text = str(raw_value).strip()
            if not text:
                return ""
            if text.startswith("{") and text.endswith("}"):
                try:
                    obj = json.loads(text)
                    if isinstance(obj, dict):
                        return str(obj.get(key, "") or "").strip()
                except Exception:
                    pass
            return text
        except Exception:
            return ""

    def _is_iso_format_error(self, result: dict) -> bool:
        """识别 Nagad ISO 报文解析失败（常见于卡参数过期/不匹配）"""
        if not isinstance(result, dict):
            return False
        reason = str(result.get("reason", "") or "").strip()
        if reason == "50_0018_730":
            return True
        dev_msg = str(result.get("devMessage", "") or "").lower()
        if "iso message format error" in dev_msg:
            return True
        raw = result.get("raw")
        if isinstance(raw, dict):
            raw_reason = str(raw.get("reason", "") or "").strip()
            raw_dev = str(raw.get("devMessage", "") or "").lower()
            if raw_reason == "50_0018_730" or "iso message format error" in raw_dev:
                return True
        return False

    def _is_atc_inactive_error(self, result: dict) -> bool:
        """识别 Nagad ATC 未激活/过期错误（需要补卡）"""
        if not isinstance(result, dict):
            return False
        reason = str(result.get("reason", "") or "").strip()
        if reason == "50_0016_763":
            return True
        dev_msg = str(result.get("devMessage", "") or "").lower()
        if "atc not active" in dev_msg:
            return True
        raw = result.get("raw")
        if isinstance(raw, dict):
            raw_reason = str(raw.get("reason", "") or "").strip()
            raw_dev = str(raw.get("devMessage", "") or "").lower()
            if raw_reason == "50_0016_763" or "atc not active" in raw_dev:
                return True
        return False

    def _is_ssl_cert_expired_error(self, result: dict) -> bool:
        """识别 SSL 证书校验失败（证书过期）错误"""
        if not isinstance(result, dict):
            return False
        err = str(result.get("error", "") or "").lower()
        if "certificate_verify_failed" in err and "certificate has expired" in err:
            return True
        raw = result.get("raw")
        if isinstance(raw, dict):
            raw_err = str(raw.get("error", "") or "").lower()
            if "certificate_verify_failed" in raw_err and "certificate has expired" in raw_err:
                return True
        return False

    def _is_http_417_support_error(self, result: dict) -> bool:
        """识别转账 HTTP 417 通用故障文案（需要补卡后重试）。

        典型文案：HTTP 417: কোথাও একটি ভুল হচ্ছে। বিস্তারিত জানতে ১৬১৬৭ এ কল করুন।
        """
        if not isinstance(result, dict):
            return False

        status = result.get("status_code")
        status_is_417 = False
        if status is not None:
            try:
                status_is_417 = int(status) == 417
            except Exception:
                status_is_417 = str(status).strip() == "417"

        texts = []
        for key in ("error", "message", "devMessage"):
            val = result.get(key)
            if isinstance(val, str) and val.strip():
                texts.append(val)
        raw = result.get("raw")
        if isinstance(raw, dict):
            for key in ("error", "message", "messageBn", "message_bn", "devMessage", "statusDesc"):
                val = raw.get(key)
                if isinstance(val, str) and val.strip():
                    texts.append(val)
        joined = " ".join(texts)
        if "HTTP 417" in joined:
            status_is_417 = True
        # 只匹配这条“某处出错，请致电 16167”文案，避免把其它 417 也当成补卡场景
        return status_is_417 and "কোথাও একটি ভুল হচ্ছে" in joined

    def _is_ssl_cert_expired_exception(self, err: Exception) -> bool:
        """识别请求异常中的 SSL 证书过期错误"""
        text = str(err or "").lower()
        return "certificate_verify_failed" in text and "certificate has expired" in text

    def _get_last_account_parameter_index(self, login_data: dict) -> str:
        """从 session 的 cardProfile 提取最近 accountParameterIndex"""
        try:
            redownload = login_data.get("reDownloadCard") or {}
            if not isinstance(redownload, dict):
                return ""
            card_profile_raw = redownload.get("cardProfile") or "{}"
            card_profile = json.loads(card_profile_raw) if isinstance(card_profile_raw, str) else dict(card_profile_raw)
            replenishment_list = card_profile.get("cardReplenishmentData") or []
            replenishment_data = replenishment_list[0] if isinstance(replenishment_list, list) and replenishment_list else {}
            return str(replenishment_data.get("accountParameterIndex") or "")
        except Exception:
            return ""

    def _generate_device_fingerprint(self, device_id: str, imei: str) -> str:
        """生成设备指纹 X-KM-DEVICE-FGP
        
        Args:
            device_id: 设备ID
            imei: IMEI
            
        Returns:
            SHA256哈希值（大写）
        """
        sign_data = f"android{self.manufacturer}{self.PhoneModel}{self.build_product}NAAANANA{device_id}{imei}{self.macAddress}true"
        return hashlib.sha256(sign_data.encode('utf-8')).hexdigest().upper()
    
    # ═══════════════════════════════════════════════
    # HTTP API 方法（内部辅助方法）
    # ═══════════════════════════════════════════════

    async def _get_insecure_http_client(self, context: PayContext):
        """统一获取关闭 SSL 校验的 HttpClient（临时兜底）。"""
        http_client = await context.get_http_client()
        try:
            if hasattr(http_client, "verify_ssl"):
                setattr(http_client, "verify_ssl", False)
            if hasattr(http_client, "verify"):
                setattr(http_client, "verify", False)
        except Exception:
            pass
        return http_client

    async def _http_get_insecure(self, context: PayContext, url: str, **kwargs):
        """发送 GET 请求并禁用 SSL 校验。"""
        http_client = await self._get_insecure_http_client(context)
        try:
            return await http_client.get(url, ssl=False, **kwargs)
        except TypeError:
            return await http_client.get(url, **kwargs)

    async def _http_post_insecure(self, context: PayContext, url: str, **kwargs):
        """发送 POST 请求并禁用 SSL 校验。"""
        http_client = await self._get_insecure_http_client(context)
        try:
            return await http_client.post(url, ssl=False, **kwargs)
        except TypeError:
            return await http_client.post(url, **kwargs)
    
    async def _check_user_status(self, context: PayContext, phone_number: str, X_KM_DEVICE_FGP: str) -> dict:
        """检查用户状态"""
        # TODO-①: 设置API端点
        url = "https://app2.mynagad.com:20002/api/user/check-user-status-for-log-in"
        
        # TODO-②: 设置请求参数
        params = {"msisdn": phone_number}
        
        # TODO-③: 设置请求头
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-DEVICE-FGP": X_KM_DEVICE_FGP,
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId
        }
        
        try:
            # TODO-④: 发送GET请求
            response = await self._http_get_insecure(context, url, params=params, headers=headers)
            
            # TODO-⑤: ✅ 检查状态码
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"检查用户状态失败: {err}")
                return {"error": err}
            
            # TODO-⑥: 解析JSON
            data = await response.json()
            
            # TODO-⑦: ⚠️ 提取并保存userId（副作用）
            user_id = data.get("userId")
            if user_id:
                await self._update_login_data(context, {'userId': user_id})
            
            # TODO-⑧: 返回结果
            return data
            
        except Exception as e:
            await context.log_manager.log(f"检查用户状态异常: {str(e)}")
            # TODO-⑨: 错误返回
            return {"error": str(e)}

    async def _get_ussd_otp_payload(self, context: PayContext, wait_seconds: int = 300) -> str:
        """获取USSD OTP（兼容字符串与字典格式）"""
        raw_val: Any = None
        try:
            raw_val = await context.credential.get_next_otp(
                wait_seconds=wait_seconds,
                credential_round_index=1,
                value_key="otp",
            )
        except TypeError:
            try:
                raw_val = await context.credential.get_next_otp(wait_seconds=wait_seconds)
            except Exception:
                raw_val = None
        except Exception:
            raw_val = None

        if isinstance(raw_val, dict):
            return str(
                raw_val.get("ussd_otp")
                or raw_val.get("ussdOtp")
                or raw_val.get("token")
                or raw_val.get("otp")
                or ""
            ).strip()
        if isinstance(raw_val, str):
            txt = raw_val.strip()
            if txt.startswith("{") and txt.endswith("}"):
                try:
                    obj = json.loads(txt)
                    if isinstance(obj, dict):
                        return str(
                            obj.get("ussd_otp")
                            or obj.get("ussdOtp")
                            or obj.get("token")
                            or obj.get("otp")
                            or ""
                        ).strip()
                except Exception:
                    pass
            return txt
        return ""

    async def _check_ussd_otp(self, context: PayContext, phone_number: str, ussd_otp: str, X_KM_DEVICE_FGP: str) -> dict:
        """验证USSD换绑OTP"""
        login_data = await self._read_login_data(context)
        user_id = login_data.get("userId", "")
        url = "https://app2.mynagad.com:20002/api/user/check-ussd-otp"
        params = {
            "msisdn": phone_number,
            "ussdOtp": ussd_otp,
        }
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-DEVICE-FGP": X_KM_DEVICE_FGP,
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-UserId": user_id,
        }

        try:
            response = await self._http_get_insecure(context, url, params=params, headers=headers)
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"USSD OTP验证失败: {err}")
                return {"error": err}
            return await response.json()
        except Exception as e:
            await context.log_manager.log(f"USSD OTP验证异常: {str(e)}")
            return {"error": str(e)}
    
    async def _login(self, context: PayContext, phone_number: str, pin: str, user_id: str, X_KM_DEVICE_FGP: str) -> dict:
        """发送OTP验证码"""
        # TODO-①: 设置API端点
        url = "https://app2.mynagad.com:20002/api/login"
        
        # TODO-②: ✅ PIN SHA256加密（保持100%一致）
        password = hashlib.sha256(pin.encode('utf-8')).hexdigest().upper()
        
        # TODO-③: 设置请求体
        payload = {
            "aspId": self.X_KM_User_AspId,
            "mpaId": None,
            "password": password,
            "username": phone_number
        }
        
        # TODO-④: 设置请求头
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=UTF-8",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-DEVICE-FGP": X_KM_DEVICE_FGP,
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-UserId": user_id
        }
        
        try:
            # TODO-⑤: 发送POST请求
            response = await self._http_post_insecure(
                context,
                url,
                data=json.dumps(payload, separators=(",", ":")),
                headers=headers,
            )
            
            # TODO-⑥: ✅ 检查状态码
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"发送OTP失败: {err}")
                return {"error": err}
            
            # TODO-⑦: 解析JSON
            data = await response.json()
            
            # TODO-⑧: ⚠️ 保存authSession响应头（副作用）
            auth_session = response.headers.get("X-KM-AUTH-SESSION")
            if auth_session:
                await self._update_login_data(context, {'authSession': auth_session})
            
            # TODO-⑨: 返回结果
            return data
            
        except Exception as e:
            await context.log_manager.log(f"发送OTP异常: {str(e)}")
            # TODO-⑩: 错误返回
            return {"error": str(e)}
    
    async def _verify_otp(
        self,
        context: PayContext,
        otp: str,
        uid: str,
        user_id: str,
        phone_number: str,
        X_KM_DEVICE_FGP: str,
        *,
        auth_session_override: Optional[str] = None,
    ) -> dict:
        """验证OTP"""
        # TODO-①: 设置API端点
        url = "https://app2.mynagad.com:20002/api/wallet/verify-authCode/device-change"
        
        # TODO-②: ⚠️ 检查authSession（fallback）
        auth_session = auth_session_override
        if not auth_session:
            login_data = await self._read_login_data(context)
            auth_session = login_data.get('authSession')
        if not auth_session:
            return {"error": "authSession未设置，请先调用login发送OTP"}
        
        # TODO-③: 设置请求体
        payload = {
            "authCode": otp,
            "event": "DEVICE_CHANGE",
            "uid": uid,
            "userId": user_id
        }
        
        # TODO-④: 设置请求头
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=UTF-8",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-AUTH-SESSION": auth_session,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-DEVICE-FGP": X_KM_DEVICE_FGP,
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-UserId": user_id,
            "X-KM-UserName": phone_number
        }
        
        try:
            # TODO-⑤: 发送POST请求
            response = await self._http_post_insecure(
                context,
                url,
                data=json.dumps(payload, separators=(",", ":")),
                headers=headers,
            )
            
            # TODO-⑥: ✅ 检查状态码
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"验证OTP失败: {err}")
                return {"error": err}
            
            # TODO-⑦: 解析JSON
            data = await response.json()
            
            # TODO-⑧⑨⑩: ⚠️ 批量保存3个认证信息（副作用）
            auth_updates = {}
            if response.headers.get("X-KM-AUTH-SESSION"):
                auth_updates['authSession'] = response.headers.get("X-KM-AUTH-SESSION")
            if response.headers.get("X-KM-AUTH-TOKEN"):
                auth_updates['authToken'] = response.headers.get("X-KM-AUTH-TOKEN")
            if response.headers.get("X-KM-REFRESH-TOKEN"):
                auth_updates['refreshToken'] = response.headers.get("X-KM-REFRESH-TOKEN")

            await self._update_login_data(context, auth_updates)
            
            # TODO-⑪: 返回结果
            return data
            
        except Exception as e:
            await context.log_manager.log(f"验证OTP异常: {str(e)}")
            # TODO-⑫: 错误返回
            return {"error": str(e)}
    
    async def _login_with_mpa(self, context: PayContext, phone_number: str, pin: str, X_KM_DEVICE_FGP: str) -> dict:
        """二次登录（使用mpaId）"""
        # TODO-①: ⚠️ 检查mpaId（fallback）
        login_data = await self._read_login_data(context)
        mpa_id = login_data.get('mpaId')
        user_id = login_data.get('userId')
        
        if not mpa_id:
            return {"error": "mpaId未设置，无法进行二次登录"}
        
        # TODO-②: 设置API端点
        url = "https://app2.mynagad.com:20002/api/login"
        
        # TODO-③: ✅ PIN SHA256加密（保持100%一致）
        password = hashlib.sha256(pin.encode('utf-8')).hexdigest().upper()
        
        # TODO-④: 设置请求体（包含mpaId）
        payload = {
            "aspId": self.X_KM_User_AspId,
            "mpaId": mpa_id,
            "password": password,
            "username": phone_number
        }
        
        # TODO-⑤: 设置请求头（包含mpaId）
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=UTF-8",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-DEVICE-FGP": X_KM_DEVICE_FGP,
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-User-MpaId": mpa_id,
            "X-KM-UserId": user_id
        }
        
        try:
            # TODO-⑥: 发送POST请求
            response = await self._http_post_insecure(
                context,
                url,
                data=json.dumps(payload, separators=(",", ":")),
                headers=headers,
            )
            
            # TODO-⑦: ✅ 检查状态码
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"二次登录失败: {err}")
                raw = {}
                try:
                    raw = await response.json()
                except Exception:
                    raw = {}
                return {
                    "error": err,
                    "status_code": response.status_code,
                    "reason": raw.get("reason") if isinstance(raw, dict) else "",
                    "devMessage": raw.get("devMessage") if isinstance(raw, dict) else "",
                    "raw": raw if isinstance(raw, dict) else {},
                }
            
            # TODO-⑧: 解析JSON
            data = await response.json()
            
            # TODO-⑨⑩: ⚠️ 批量保存2个认证信息（副作用）
            auth_updates = {}
            if response.headers.get("X-KM-AUTH-TOKEN"):
                auth_updates['authToken'] = response.headers.get("X-KM-AUTH-TOKEN")
            if response.headers.get("X-KM-REFRESH-TOKEN"):
                auth_updates['refreshToken'] = response.headers.get("X-KM-REFRESH-TOKEN")
            
            await self._update_login_data(context, auth_updates)
            
            # TODO-⑪: 返回结果
            return data
            
        except Exception as e:
            await context.log_manager.log(f"二次登录异常: {str(e)}")
            # TODO-⑫: 错误返回
            return {"error": str(e)}
    
    async def _get_balance(self, context: PayContext, par: str = None) -> dict:
        """查询账户余额"""
        # TODO-①: ⚠️ 检查authToken（fallback）
        login_data = await self._read_login_data(context)
        auth_token = login_data.get('authToken')
        if not auth_token:
            return {"error": "authToken未设置，请先登录"}
        
        # TODO-②③: ⚠️ 参数par的fallback处理
        use_par = par if par else login_data.get('par')
        if not use_par:
            return {"error": "par参数未设置"}
        
        # TODO-④: 设置API端点
        url = "https://app2.mynagad.com:20002/api/getBalanceList"
        
        # TODO-⑤: 设置请求体
        payload = {
            "parList": [{"par": use_par}]
        }
        
        # TODO-⑥: 设置请求头
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=UTF-8",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-AUTH-TOKEN": auth_token,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-User-MpaId": login_data.get('mpaId', ''),
            "X-KM-UserId": login_data.get('userId', '')
        }
        
        try:
            # TODO-⑦: 发送POST请求
            response = await self._http_post_insecure(
                context,
                url,
                data=json.dumps(payload, separators=(",", ":")),
                headers=headers,
            )
            
            # TODO-⑧: ✅ 检查状态码
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"查询余额失败: {err}")
                return {"error": err}
            
            # TODO-⑨: 解析JSON
            data = await response.json()
            
            # TODO-⑩: 返回结果
            return data
            
        except Exception as e:
            await context.log_manager.log(f"查询余额异常: {str(e)}")
            # TODO-⑪: 错误返回
            return {"error": str(e)}
    
    async def _refresh_token(self, context: PayContext) -> dict:
        """刷新Token"""
        # TODO-①: ⚠️ 检查refreshToken（fallback）
        login_data = await self._read_login_data(context)
        refresh_token = login_data.get('refreshToken')
        if not refresh_token:
            return {"error": "refreshToken未设置，无法刷新Token"}
        
        # TODO-②: 设置API端点
        url = "https://app2.mynagad.com:20002/api/refreshToken"
        
        # TODO-③: 设置请求头
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-REFRESH-TOKEN": refresh_token,
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-User-MpaId": login_data.get('mpaId', ''),
            "X-KM-UserId": login_data.get('userId', '')
        }
        
        try:
            # TODO-④: 发送GET请求
            response = await self._http_get_insecure(context, url, headers=headers)
            
            # TODO-⑤: ✅ 检查状态码
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"刷新Token失败: {err}")
                return {"error": err}
            
            # TODO-⑥: 解析JSON
            data = await response.json()
            
            # TODO-⑦⑧: 提取新Token
            new_auth_token = response.headers.get("X-KM-AUTH-TOKEN")
            new_refresh_token = response.headers.get("X-KM-REFRESH-TOKEN")
            
            # TODO-⑨⑩: ⚠️ 条件性更新Token（副作用）
            token_updates = {}
            if new_auth_token:
                token_updates['authToken'] = new_auth_token
            if new_refresh_token:
                token_updates['refreshToken'] = new_refresh_token
            
            if token_updates:
                await self._update_login_data(context, token_updates)
            
            # TODO-⑪: 返回结果
            return data
            
        except Exception as e:
            await context.log_manager.log(f"刷新Token异常: {str(e)}")
            # TODO-⑫: 错误返回
            return {"error": str(e)}
    
    def _is_token_expired(self, response_data: dict) -> bool:
        """检查响应是否为Token过期错误"""
        # TODO-②: ⚠️ 类型检查（fallback）
        if isinstance(response_data, dict):
            # TODO-③: 提取reason字段
            reason = response_data.get("reason", "")
            # TODO-④: 检查是否为Token过期错误
            if reason == "30_0002_004":
                return True
        # TODO-⑤: ⚠️ 默认返回False（fallback）
        return False

    def _is_auth_session_timeout(self, result: Any) -> bool:
        """判断是否会话/Token超时（对齐查询流水：HTTP 401 或 reason=30_0002_004）。"""
        if not isinstance(result, dict):
            return False
        status_code = result.get("status_code")
        try:
            if int(status_code) == 401:
                return True
        except Exception:
            if str(status_code).strip() == "401":
                return True
        if self._is_token_expired(result):
            return True
        raw = result.get("raw")
        if isinstance(raw, dict) and self._is_token_expired(raw):
            return True
        return False

    async def _relogin_after_session_timeout(self, context: PayContext) -> Dict[str, Any]:
        """会话超时时执行一次二次登录（mpa），返回登录结果 dict。"""
        login_data = await self._read_login_data(context)
        phone = getattr(context.account, "bank_code1", None) or ""
        pin = getattr(context.account, "bank_code2", None) or ""
        device_id = (login_data.get("device_id") or "").strip()
        if device_id:
            X_KM_DEVICE_FGP = self._generate_device_fingerprint(device_id, device_id)
        else:
            X_KM_DEVICE_FGP = ""
        return await self._login_with_mpa(context, phone, pin, X_KM_DEVICE_FGP)

    async def _get_transaction_history_page(self, context: PayContext, page: int = 0, page_size: int = 20, 
                                            date_from: int = None, date_to: int = None, par: str = None,
                                            txn_id: str = None) -> dict:
        """查询交易流水"""
        # 401 时重试一次：执行二次登录刷新 token 后再查询
        for attempt in range(2):
            # TODO-①: ⚠️ 检查authToken（fallback）
            login_data = await self._read_login_data(context)
            auth_token = login_data.get('authToken')
            if not auth_token:
                return {"error": "authToken未设置，请先登录"}
            
            # TODO-②③: ⚠️ 参数par的fallback处理
            use_par = par if par else login_data.get('par')
            if not use_par:
                return {"error": "par参数未设置"}
        
            # TODO-④: 设置API端点
            url = "https://app2.mynagad.com:20002/api/dfs/user/get-transaction-history"
        
            # TODO-⑤: 设置搜索条件
            search_criteria = {
                "accountNo": use_par,
                "accountNoType": "PAR",
                "responseCode": "00"
            }
        
            # TODO-⑥⑦: ⚠️ 条件性添加日期筛选（fallback）
            if date_from:
                search_criteria["trDatetimeFrom"] = str(date_from)
            if date_to:
                search_criteria["trDatetimeTo"] = str(date_to)
            if txn_id:
                search_criteria["txnId"] = str(txn_id)
        
            # TODO-⑧: 设置搜索参数
            search_params = {
                "page": page,
                "pageSize": page_size,
                "searchCriteriaContentList": [search_criteria]
            }
        
            # TODO-⑨: ⚠️ URL编码JSON参数（特殊处理）
            params = {
                "searchParams": json.dumps(search_params, separators=(',', ':'))
            }
        
            # TODO-⑩: 设置请求头
            headers = {
                "Accept-Encoding": "gzip",
                "Connection": "Keep-Alive",
                "Host": "app2.mynagad.com:20002",
                "User-Agent": self.User_Agent,
                "X-KM-AUTH-TOKEN": auth_token,
                "X-KM-Accept-language": "bn",
                "X-KM-AppCode": "02",
            "X-KM-User-Agent": self.appUA,
                "X-KM-User-AspId": self.X_KM_User_AspId,
                "X-KM-User-MpaId": login_data.get('mpaId', ''),
                "X-KM-UserId": login_data.get('userId', '')
            }
        
            try:
                # TODO-⑪: 发送GET请求
                response = await self._http_get_insecure(context, url, params=params, headers=headers)
                
                # TODO-⑫: ✅ 检查状态码
                if response.status_code >= 400:
                    # 401：token过期 -> 二次登录刷新后重试一次
                    if response.status_code == 401 and attempt == 0:
                        await context.log_manager.log("查询交易流水返回401，尝试二次登录刷新token后重试一次...")
                        try:
                            phone = getattr(context.account, "bank_code1", None) or ""
                            pin = getattr(context.account, "bank_code2", None) or ""
                            device_id = (login_data.get("device_id") or "").strip()
                            if device_id:
                                X_KM_DEVICE_FGP = self._generate_device_fingerprint(device_id, device_id)
                            else:
                                # 无 device_id 时仍可尝试用已存在的指纹（无法生成则置空）
                                X_KM_DEVICE_FGP = ""
                            relog = await self._login_with_mpa(context, phone, pin, X_KM_DEVICE_FGP)
                            if isinstance(relog, dict) and relog.get("error"):
                                return {"error": f"401 二次登录失败: {relog.get('error')}"}
                        except Exception as e:
                            return {"error": f"401 二次登录异常: {str(e)}"}
                        continue
                    
                    err = await self._http_error_text(response)
                    await context.log_manager.log(f"查询交易流水失败: {err}")
                    return {"error": err}
                
                # TODO-⑬: 解析JSON
                data = await response.json()
                
                # TODO-⑭: 返回结果
                return data
                
            except Exception as e:
                await context.log_manager.log(f"查询交易流水异常: {str(e)}")
                return {"error": str(e)}
        
        # 默认兜底（理论不可达）
        return {"error": "查询交易流水失败"}
    
    async def _initialize_wallet(self, context: PayContext, user_id: str, X_KM_DEVICE_FGP: str, device_id: str) -> dict:
        """初始化钱包，生成RSA512密钥对并发送CSR"""
        # TODO-①: ⚠️ 生成RSA512密钥对（副作用）
        wallet_keys = generate_wallet_keys(user_id)
        
        # TODO-②: 提取CSR
        csr_hex = wallet_keys['csr_hex']
        
        login_data = await self._read_login_data(context)
        auth_session = login_data.get('authSession')
        auth_token = login_data.get('authToken')
        
        # TODO-③: 设置API端点
        url = "https://app2.mynagad.com:20002/api/wallet/initializeWallet"
        
        # TODO-④: 设置复杂请求体（包含deviceInfo嵌套对象）
        imei = device_id
        payload = {
            "activationCode": None,
            "activationId": None,
            "csr": csr_hex,
            "deviceFingerPrint": X_KM_DEVICE_FGP,
            "deviceInfo": {
                "deviceId": device_id,
                "imei": imei,
                "macAddress": self.macAddress,
                "manufacturer": self.manufacturer,
                "mfgFingerPrint": f"google/{self.build_product}/{self.build_product}:{self.osVersion}/UP1A.231",
                "model": self.PhoneModel,
                "nfcSupport": True,
                "osName": "android",
                "processorName": "NAAA",
                "processorNumber": "NA",
                "processorSpeed": "NA",
                "product": self.build_product
            },
            "email": None,
            "mnoName": "LGU Plus",
            "msisdn": "",
            "osFirmwireBuild": "10754064",
            "osVersion": self.osVersion,
            "paymentPin": None,
            "proposedRnsId": "fe5FQ8uro8s:APA91bFgq0B_OWr5B0RbPFilh1_xKrml1ZTmq1zmEdES1FBKS4SgwY1jMtb7I1NTAX7FNP537AnDacodqRP5-D5K9LITvFsWPNLOsHcmtq7Ivj-yh0NomA0",
            "reInitialization": True,
            "rnsProviderType": "05",
            "rooted": False,
            "userId": user_id,
            "wspId": self.X_KM_User_AspId
        }
        
        # TODO-⑤: 设置请求头
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=UTF-8",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-AUTH-SESSION": auth_session,
            "X-KM-AUTH-TOKEN": auth_token,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-DEVICE-FGP": X_KM_DEVICE_FGP,
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-UserId": user_id
        }
        
        try:
            # TODO-⑥: 发送POST请求
            response = await self._http_post_insecure(
                context,
                url,
                data=json.dumps(payload, separators=(",", ":")),
                headers=headers,
            )
            
            # TODO-⑦: ✅ 检查状态码
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"初始化钱包失败: {err}")
                return {"error": err}
            
            # TODO-⑧: 解析JSON
            data = await response.json()
            
            # TODO-⑨⑩: ⚠️ 条件性更新Token（副作用）
            # ⚠️ 只保存可序列化版本到session，避免持久化失败
            updates = {'walletKeys': self._wallet_keys_for_session(wallet_keys)}
            
            if response.headers.get("X-KM-AUTH-TOKEN"):
                updates['authToken'] = response.headers.get("X-KM-AUTH-TOKEN")
            if response.headers.get("X-KM-REFRESH-TOKEN"):
                updates['refreshToken'] = response.headers.get("X-KM-REFRESH-TOKEN")
            
            # TODO-⑪: 提取加密的mpaId
            encrypted_mpa_id = data.get("mpaId")
            
            # TODO-⑫⑬⑭⑮⑯⑰: ⚠️ 解密mpaId（包含fallback和异常处理）
            if encrypted_mpa_id and wallet_keys:
                try:
                    # TODO-⑬: RSA512解密
                    decrypted = decrypt_with_private_key(
                        encrypted_mpa_id,
                        wallet_keys['private_key']
                    )
                    # TODO-⑭: ⚠️ 保存解密后的mpaId（副作用）
                    mpa_id = decrypted.decode('utf-8')
                    updates['mpaId'] = mpa_id
                    
                    # TODO-⑮: ⚠️ log解密后的mpaId（print改为log）
                    await context.log_manager.log(f"解密后的mpaId: {mpa_id}")
                    
                    # TODO-⑯: 添加到返回值
                    data['mpaId_decrypted'] = mpa_id
                except Exception as e:
                    # TODO-⑰: ⚠️ 解密失败处理（fallback）
                    data['mpaId_decrypt_error'] = str(e)
                    await context.log_manager.log(f"解密mpaId失败: {str(e)}")
            
            # TODO-⑱: ⚠️ 提取并保存par（副作用，包含条件判断）
            card_info_list = data.get("cardInfoList")
            if card_info_list and len(card_info_list) > 0:
                par = card_info_list[0].get("par")
                if par:
                    updates['par'] = par

            await self._update_login_data(context, updates)
            
            # TODO-⑲: 返回结果
            return data
            
        except Exception as e:
            await context.log_manager.log(f"初始化钱包异常: {str(e)}")
            # TODO-⑳: 错误返回
            return {"error": str(e)}
    
    async def _redownload_card(self, context: PayContext, purpose: str = "DEVICE_CHANGE") -> dict:
        """重新下载卡片信息"""
        # TODO-①②③: ⚠️ 3个连续的fallback检查
        login_data = await self._read_login_data(context)
        auth_token = login_data.get('authToken')
        mpa_id = login_data.get('mpaId')
        par = login_data.get('par')
        
        if not auth_token:
            return {"error": "authToken未设置，请先登录"}
        
        if not mpa_id:
            return {"error": "mpaId未设置"}
        
        if not par:
            return {"error": "par未设置"}
        
        # TODO-④: 设置API端点
        url = "https://app2.mynagad.com:20002/api/wallet/reDownloadCard"
        
        # TODO-⑤: 设置请求体
        payload = {
            "cardId": None,
            "mpaId": mpa_id,
            "par": par,
            "purpose": purpose,
            "serviceId": None
        }
        
        # TODO-⑥: 设置请求头
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=UTF-8",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-AUTH-TOKEN": auth_token,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-User-MpaId": mpa_id,
            "X-KM-UserId": login_data.get('userId', '')
        }
        
        try:
            # TODO-⑦: 发送POST请求
            response = await self._http_post_insecure(
                context,
                url,
                data=json.dumps(payload, separators=(",", ":")),
                headers=headers,
            )
            
            # TODO-⑧: ✅ 检查状态码
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"重新下载卡片失败: {err}")
                return {"error": err}
            
            # TODO-⑨: 解析JSON
            data = await response.json()
            
            # TODO-⑩: 返回结果
            return data
            
        except Exception as e:
            await context.log_manager.log(f"重新下载卡片异常: {str(e)}")
            # TODO-⑪: 错误返回
            return {"error": str(e)}

    def _load_private_key_from_pkcs8_hex(self, private_key_hex: str) -> "_RSAKeyPure":
        """从 PKCS8 DER hex 重建 _RSAKeyPure 私钥"""
        data = bytes.fromhex(private_key_hex)

        def read_len(pos):
            if data[pos] & 0x80 == 0:
                return data[pos], pos + 1
            n_bytes = data[pos] & 0x7F
            length = int.from_bytes(data[pos + 1:pos + 1 + n_bytes], "big")
            return length, pos + 1 + n_bytes

        def enter_elem(pos):
            l, p = read_len(pos + 1)
            return l, p

        def skip_elem(pos):
            l, p = read_len(pos + 1)
            return p + l

        def read_int(pos):
            if data[pos] != 0x02:
                raise ValueError("Invalid PKCS8: expected INTEGER")
            l, p = read_len(pos + 1)
            return int.from_bytes(data[p:p + l], "big"), p + l

        # PKCS8 outer SEQUENCE
        _, pos = enter_elem(0)
        pos = skip_elem(pos)   # version INTEGER
        pos = skip_elem(pos)   # AlgorithmIdentifier SEQUENCE
        _, pos = enter_elem(pos)  # OCTET STRING（进入内容）
        _, pos = enter_elem(pos)  # RSAPrivateKey SEQUENCE
        pos = skip_elem(pos)   # version INTEGER
        n, pos = read_int(pos)
        e, pos = read_int(pos)
        d, pos = read_int(pos)
        p_val, pos = read_int(pos)
        q_val, pos = read_int(pos)
        return _RSAKeyPure(n, e, d, p_val, q_val)

    def _pkcs7_decrypt_hex_with_private_key(self, encrypted_hex: str, private_key_hex: str) -> bytes:
        """解密 PKCS7 EnvelopedData hex（对齐 Uddokta20260612 实现）"""
        data = bytes.fromhex(encrypted_hex)
        private_key = self._load_private_key_from_pkcs8_hex(private_key_hex)

        def read_len(pos):
            if data[pos] & 0x80 == 0:
                return data[pos], pos + 1
            n_bytes = data[pos] & 0x7F
            length = int.from_bytes(data[pos + 1:pos + 1 + n_bytes], "big")
            return length, pos + 1 + n_bytes

        def enter_elem(pos):
            l, p = read_len(pos + 1)
            return l, p

        def skip_elem(pos):
            l, p = read_len(pos + 1)
            return p + l

        # ContentInfo SEQUENCE
        _, pos = enter_elem(0)
        pos = skip_elem(pos)          # contentType OID
        _, pos = enter_elem(pos)      # [0] EXPLICIT
        _, pos = enter_elem(pos)      # EnvelopedData SEQUENCE
        pos = skip_elem(pos)          # version INTEGER

        # recipientInfos SET
        set_len, set_pos = enter_elem(pos)
        eci_start = set_pos + set_len

        # RecipientInfo SEQUENCE
        _, pos = enter_elem(set_pos)
        pos = skip_elem(pos)          # version INTEGER
        pos = skip_elem(pos)          # issuerAndSerialNumber
        pos = skip_elem(pos)          # keyEncryptionAlgorithm

        # encryptedKey OCTET STRING
        if data[pos] != 0x04:
            raise ValueError("Invalid PKCS7: encryptedKey tag mismatch")
        key_len, key_pos = read_len(pos + 1)
        encrypted_key_bytes = data[key_pos:key_pos + key_len]

        # 用钱包私钥解密会话密钥
        des_key_16 = private_key.decrypt(encrypted_key_bytes)
        des_key_24 = des_key_16 + des_key_16[:8]

        # EncryptedContentInfo SEQUENCE
        _, pos = enter_elem(eci_start)
        pos = skip_elem(pos)          # contentType OID
        pos = skip_elem(pos)          # contentEncryptionAlgorithm

        # encryptedContent [0] IMPLICIT (tag 0x80)
        if data[pos] != 0x80:
            raise ValueError("Invalid PKCS7: encryptedContent tag mismatch")
        content_len, content_pos = read_len(pos + 1)
        encrypted_content = data[content_pos:content_pos + content_len]

        # 3DES-CBC 解密（IV=8个0，与加密一致）
        cipher = Cipher(TripleDES(des_key_24), modes.CBC(bytes(8)), backend=default_backend())
        dec = cipher.decryptor()
        padded = dec.update(encrypted_content) + dec.finalize()
        unpadder = padding.PKCS7(64).unpadder()
        return unpadder.update(padded) + unpadder.finalize()

    async def _replenish_card_request(
        self,
        context: PayContext,
        encrypted_data: str,
        card_id: str,
        mpa_id: str = None,
    ) -> dict:
        """调用 secure-filter replenishCard 接口"""
        login_data = await self._read_login_data(context)
        auth_token = login_data.get("authToken")
        if not auth_token:
            return {"error": "authToken未设置，请先登录"}

        use_mpa_id = mpa_id if mpa_id else login_data.get("mpaId")
        if not use_mpa_id:
            return {"error": "mpaId未设置"}
        if not card_id:
            return {"error": "cardId未设置"}

        url = "https://app2.mynagad.com:20002/secure-filter/api/cards/replenishCard"
        payload = {
            "data": encrypted_data,
            "mpaId": use_mpa_id,
        }

        device_id = (login_data.get("device_id") or "").strip()
        x_km_device_fgp = self._generate_device_fingerprint(device_id, device_id) if device_id else ""
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=UTF-8",
            "Host": "app2.mynagad.com:20002",
            "Referer": "https://app2.mynagad.com:20002/secure-filter/api/cards/replenishCard",
            "User-Agent": self.User_Agent,
            "X-CardId": card_id,
            "X-KM-AUTH-TOKEN": auth_token,
            "X-KM-Accept-Language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-DEVICE-FGP": x_km_device_fgp,
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-User-MpaId": use_mpa_id,
            "X-KM-UserId": login_data.get("userId", ""),
        }

        try:
            response = await self._http_post_insecure(
                context,
                url,
                data=json.dumps(payload, separators=(",", ":")),
                headers=headers,
            )
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                raw = {}
                try:
                    raw = await response.json()
                except Exception:
                    raw = {}
                return {
                    "error": err,
                    "status_code": response.status_code,
                    "reason": raw.get("reason") if isinstance(raw, dict) else "",
                    "devMessage": raw.get("devMessage") if isinstance(raw, dict) else "",
                    "raw": raw if isinstance(raw, dict) else {},
                }
            return await response.json()
        except Exception as e:
            return {"error": str(e)}

    async def _replenish_card(self, context: PayContext, last_api: str = "") -> dict:
        """补卡并更新 session 中 cardProfile.cardReplenishmentData"""
        login_data = await self._read_login_data(context)
        redownload = login_data.get("reDownloadCard") or {}
        if not isinstance(redownload, dict):
            return {"error": "reDownloadCard数据异常"}

        card_id = str(redownload.get("cardId") or "").strip()
        if not card_id:
            return {"error": "缺少cardId，无法补卡"}
        mpa_id = str(login_data.get("mpaId") or "").strip()
        if not mpa_id:
            return {"error": "缺少mpaId，无法补卡"}

        sequence_counter = int(login_data.get("sequenceCounter") or 1)
        payload = {
            "cardId": card_id,
            "mpaId": mpa_id,
            "replenishmentRequestData": {
                "macOfReplenishmentData": "",
                "sequenceCounter": sequence_counter,
                "lastAccountParameterIndexReceived": str(last_api or ""),
                "transactionHistory": [],
            },
        }

        cert_der = _get_embedded_sign_cert_der()
        encrypted = _Pkcs7Encryptor(cert_der=cert_der).encrypt_to_hex(
            json.dumps(payload, separators=(",", ":")).encode("utf-8")
        )
        result = await self._replenish_card_request(
            context=context,
            encrypted_data=encrypted,
            card_id=card_id,
            mpa_id=mpa_id,
        )
        if "error" in result:
            return result

        data_hex = result.get("data")
        if not data_hex:
            return {"error": "补卡响应缺少data字段"}

        wallet_keys = login_data.get("walletKeys") or {}
        private_key_hex = str(wallet_keys.get("private_key_hex") or "").strip()
        if not private_key_hex:
            return {"error": "walletKeys.private_key_hex缺失，无法解密补卡响应"}

        try:
            decrypted = self._pkcs7_decrypt_hex_with_private_key(data_hex, private_key_hex)
            new_data = json.loads(decrypted.decode("utf-8"))
        except Exception as e:
            return {"error": f"补卡响应解密失败: {str(e)}"}

        new_replenishment = new_data.get("cardReplenishmentData")
        if not isinstance(new_replenishment, list) or not new_replenishment:
            return {"error": "补卡响应缺少cardReplenishmentData"}

        card_profile_raw = redownload.get("cardProfile") or "{}"
        card_profile = json.loads(card_profile_raw) if isinstance(card_profile_raw, str) else dict(card_profile_raw)
        card_profile["cardReplenishmentData"] = new_replenishment
        redownload["cardProfile"] = json.dumps(card_profile, separators=(",", ":"))

        await self._update_login_data(
            context,
            {
                "reDownloadCard": redownload,
                "sequenceCounter": sequence_counter + 1,
            },
        )
        return {"ok": True, "replenishmentData": new_replenishment[0], "sequenceCounter": sequence_counter + 1}
    
    async def _cash_in(self, context: PayContext, encrypted_data: str, mpa_id: str = None) -> dict:
        """执行Cash-In转账请求"""
        # TODO-①: ⚠️ 检查authToken（fallback）
        login_data = await self._read_login_data(context)
        auth_token = login_data.get('authToken')
        if not auth_token:
            return {"error": "authToken未设置，请先登录"}
        
        # TODO-②③: ⚠️ 参数mpa_id的fallback处理
        use_mpa_id = mpa_id if mpa_id else login_data.get('mpaId')
        if not use_mpa_id:
            return {"error": "mpaId未设置"}
        
        # TODO-④: 设置API端点
        url = "https://app2.mynagad.com:20002/api/dfs/cash-in"
        
        # TODO-⑤: 设置请求体
        payload = {
            "data": encrypted_data,
            "mpaId": use_mpa_id
        }
        
        # TODO-⑥⑦: 设置请求头（⚠️ 包含特殊请求头）
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=UTF-8",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-AUTH-TOKEN": auth_token,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-Encryption-Algorithm": "PKCS7",  # ⚠️ 特殊请求头，必须保留
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-User-MpaId": use_mpa_id,
            "X-KM-UserId": login_data.get('userId', '')
        }
        
        try:
            # TODO-⑧: 发送POST请求
            await context.log_manager.log(
                f"发送Cash-In请求: url={url}, mpaId={use_mpa_id}, data_len={len(encrypted_data)}"
            )
            response = await self._http_post_insecure(
                context,
                url,
                data=json.dumps(payload, separators=(",", ":")),
                headers=headers,
            )
            
            # TODO-⑨: ✅ 检查状态码
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"Cash-In转账失败: {err}")
                raw = {}
                try:
                    raw = await response.json()
                except Exception:
                    raw = {}
                return {
                    "error": err,
                    "status_code": response.status_code,
                    "reason": raw.get("reason") if isinstance(raw, dict) else "",
                    "devMessage": raw.get("devMessage") if isinstance(raw, dict) else "",
                    "raw": raw if isinstance(raw, dict) else {},
                }
            
            # TODO-⑩: 解析JSON
            data = await response.json()
            
            # TODO-⑪: 返回结果
            return data
            
        except Exception as e:
            await context.log_manager.log(f"Cash-In转账异常: {str(e)}")
            # TODO-⑫: 错误返回
            return {"error": str(e)}

    async def _b2b_to_parent(self, context: PayContext, encrypted_data: str, mpa_id: str = None) -> dict:
        """执行B2B转账请求（b2b-to-parent）"""
        login_data = await self._read_login_data(context)
        auth_token = login_data.get("authToken")
        if not auth_token:
            return {"error": "authToken未设置，请先登录"}

        use_mpa_id = mpa_id if mpa_id else login_data.get("mpaId")
        if not use_mpa_id:
            return {"error": "mpaId未设置"}

        url = "https://app2.mynagad.com:20002/api/dfs/b2b-to-parent"
        payload = {
            "data": encrypted_data,
            "mpaId": use_mpa_id,
        }
        headers = {
            "Accept-Encoding": "gzip",
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=UTF-8",
            "Host": "app2.mynagad.com:20002",
            "User-Agent": self.User_Agent,
            "X-KM-AUTH-TOKEN": auth_token,
            "X-KM-Accept-language": "bn",
            "X-KM-AppCode": "02",
            "X-KM-Encryption-Algorithm": "PKCS7",
            "X-KM-User-Agent": self.appUA,
            "X-KM-User-AspId": self.X_KM_User_AspId,
            "X-KM-User-MpaId": use_mpa_id,
            "X-KM-UserId": login_data.get("userId", ""),
        }

        try:
            await context.log_manager.log(
                f"发送B2B请求: url={url}, mpaId={use_mpa_id}, data_len={len(encrypted_data)}"
            )
            response = await self._http_post_insecure(
                context,
                url,
                data=json.dumps(payload, separators=(",", ":")),
                headers=headers,
            )
            if response.status_code >= 400:
                err = await self._http_error_text(response)
                await context.log_manager.log(f"B2B转账失败: {err}")
                raw = {}
                try:
                    raw = await response.json()
                except Exception:
                    raw = {}
                return {
                    "error": err,
                    "status_code": response.status_code,
                    "reason": raw.get("reason") if isinstance(raw, dict) else "",
                    "devMessage": raw.get("devMessage") if isinstance(raw, dict) else "",
                    "raw": raw if isinstance(raw, dict) else {},
                }
            return await response.json()
        except Exception as e:
            await context.log_manager.log(f"B2B转账异常: {str(e)}")
            return {"error": str(e)}
    
    async def _increment_atc(self, context: PayContext) -> int:
        """递增ATC（Application Transaction Counter）值并保存"""
        try:
            # TODO-①: 从session读取当前ATC
            login_data = await self._read_login_data(context)
            current_atc = login_data.get('atc', 0)
            
            # TODO-③: 递增ATC（核心逻辑）
            new_atc = current_atc + 1
            
            try:
                # TODO-④: 保存新ATC到session
                await self._update_login_data(context, {'atc': new_atc})
                
                # TODO-⑤: ⚠️ log ATC递增信息（print改为log）
                await context.log_manager.log(f"ATC已递增: {current_atc} -> {new_atc}")
            except Exception as e:
                # TODO-⑥: ⚠️ 保存失败处理（fallback）
                await context.log_manager.log(f"保存ATC失败: {str(e)}")
            
            # TODO-⑦: 返回新ATC
            return new_atc
            
        except Exception as e:
            # TODO-②: ⚠️ 读取失败处理（fallback）
            await context.log_manager.log(f"读取ATC失败: {str(e)}")
            return 0
    
    # ═══════════════════════════════════════════════
    # 辅助方法（业务逻辑）
    # ═══════════════════════════════════════════════
    
    def _parse_transaction(self, tx_data: dict) -> Optional[ParsedTransaction]:
        """解析单个交易记录
        
        Args:
            tx_data: 交易记录JSON
            
        Returns:
            ParsedTransaction对象，解析失败返回None
        """
        try:
            if not isinstance(tx_data, dict):
                return None

            # ===== 1) 时间 =====
            ts_raw = (
                tx_data.get("transactionDateTime")
                or tx_data.get("trDatetime")
                or tx_data.get("approvalDate")
                or tx_data.get("txnTime")
            )
            tx_time = None
            try:
                if ts_raw is not None and ts_raw != "":
                    ts_int = int(str(ts_raw).strip())
                    # 粗略判断：>= 10^12 认为是毫秒时间戳
                    if ts_int >= 10**12:
                        tx_time = datetime.fromtimestamp(ts_int / 1000.0)
                    else:
                        tx_time = datetime.fromtimestamp(ts_int)
            except Exception:
                tx_time = None

            # ===== 2) 金额（分） =====
            amt_raw = (
                tx_data.get("transactionAmount")
                if tx_data.get("transactionAmount") is not None
                else tx_data.get("trAmount")
            )
            try:
                amt = float(amt_raw) if amt_raw is not None and amt_raw != "" else 0.0
            except Exception:
                amt = 0.0
            amount_cents = int(round(amt * 100))

            # ===== 3) 余额（分） =====
            bal_raw = (
                tx_data.get("cardAvailableBalance")
                if tx_data.get("cardAvailableBalance") is not None
                else tx_data.get("cardBalance")
            )
            balance_cents = 0
            try:
                if bal_raw is not None and bal_raw != "":
                    balance_cents = int(round(float(bal_raw) * 100))
            except Exception:
                balance_cents = 0

            # ===== 4) 交易方向 =====
            # 统一：0=入账，1=出账（对齐 bKashAgent/JazzCash 的 ParsedTransaction）
            tx_type_str = (tx_data.get("transactionType") or tx_data.get("trType") or "").upper()
            if tx_type_str in ["CREDIT", "IN", "RECEIVED", "DEPOSIT"]:
                transaction_type = 0
            elif tx_type_str in ["DEBIT", "OUT", "SENT", "WITHDRAW"]:
                transaction_type = 1
            else:
                # 兜底：金额为负则认为出账
                transaction_type = 1 if amount_cents < 0 else 0
                amount_cents = abs(amount_cents)

            # ===== 5) 交易码/标识 =====
            transaction_code = (
                tx_data.get("transactionId")
                or tx_data.get("trId")
                or tx_data.get("trxReferenceNumber")
                or tx_data.get("nrNumber")
                or ""
            )

            # ===== 6) 详情/对方信息 =====
            detail = (
                tx_data.get("transactionDescription")
                or tx_data.get("userRefMessage")
                or tx_data.get("narration")
                or ""
            )
            if not detail:
                merchant = tx_data.get("merchantName") or tx_data.get("merchantNameLocation") or ""
                cat = tx_data.get("transactionCategory") or ""
                status = tx_data.get("transactionStatus") or tx_data.get("status") or ""
                parts = [p for p in [merchant, cat, status, tx_type_str] if p]
                detail = " | ".join(parts) if parts else ""

            sender = tx_data.get("senderMobileNo") or ""
            recipient = tx_data.get("recipientMobileNo") or ""
            # 按方向推断对方号码：入账显示发送方，出账显示收款方
            target_card_number = sender if transaction_type == 0 else recipient

            # name：优先商户名，否则对方号码
            name = tx_data.get("merchantName") or (sender if sender else recipient)

            # currency：优先 numeric code 映射；默认 BDT
            currency_code = str(tx_data.get("currencyCode") or "").strip()
            currency = "BDT"
            if currency_code == "050":
                currency = "BDT"
                
            transactionCategory = str(tx_data.get("transactionCategory") or "").strip()
            isB2b = False
            if transactionCategory == "B2B":
                isB2b = True

            return ParsedTransaction(
                time=tx_time,
                amount=amount_cents,
                balance=balance_cents,
                detail=detail,
                transaction_code=str(transaction_code),
                transaction_type=transaction_type,
                message_identify_code=str(tx_data.get("nrNumber") or transaction_code or ""),
                name=str(name or ""),
                target_card_number=str(target_card_number or ""),
                identify_code=str(tx_data.get("processingCode") or tx_data.get("refTransactionId") or ""),
                is_b2b= isB2b
            )
        except Exception as e:
            return None
    
    async def _prepare_transfer_params(
        self,
        context: PayContext,
        to_account: str,
        amount: float,
        *,
        is_b2b: bool = False,
        from_account: str = "",
        use_rp_key: bool = False,
    ) -> Optional[str]:
        """准备转账参数（PKCS7加密）
        
        Args:
            context: 支付上下文
            to_account: 收款账号
            amount: 转账金额
            
        Returns:
            加密后的请求数据（hex字符串），失败返回None
        """
        try:
            login_data = await self._read_login_data(context)
            
            # 需要 cardProfile 中的卡片参数（来自 reDownloadCard）
            card_profile_str = ""
            redownload = login_data.get("reDownloadCard") or {}
            if isinstance(redownload, dict):
                card_profile_str = redownload.get("cardProfile") or ""
            if not card_profile_str:
                await context.log_manager.log("缺少cardProfile：请先完成绑定设备登录并成功reDownloadCard")
                return None

            try:
                card_profile = json.loads(card_profile_str) if isinstance(card_profile_str, str) else dict(card_profile_str)
            except Exception:
                await context.log_manager.log("cardProfile解析失败")
                return None

            # 解析卡片配置数据（对齐 Uddokta.py main 示例）
            card_provisioning = (card_profile.get("cardProvisioningData") or {})
            dc_cp_mpp = (card_provisioning.get("DC_CP_MPP") or {})
            payment_data = (dc_cp_mpp.get("contactlessPaymentData") or {})
            replenishment_list = card_profile.get("cardReplenishmentData") or []
            replenishment_data = replenishment_list[0] if isinstance(replenishment_list, list) and replenishment_list else {}

            # 递增ATC
            atc = await self._increment_atc(context)

            # 转账笔数达到 transactionLimit 时执行自动补卡
            transaction_limit_raw = replenishment_data.get("transactionLimit", 100)
            try:
                transaction_limit = int(transaction_limit_raw)
            except Exception:
                transaction_limit = 100
            if transaction_limit > 0 and atc > 1 and (atc - 1) % transaction_limit == 0:
                await context.log_manager.log(
                    f"达到补卡阈值: ATC={atc}, transactionLimit={transaction_limit}，开始自动补卡"
                )
                replenish_result = await self._replenish_card(
                    context,
                    last_api=str(replenishment_data.get("accountParameterIndex") or ""),
                )
                if "error" in replenish_result:
                    await context.log_manager.log(f"自动补卡失败，继续使用当前卡参数: {replenish_result.get('error')}")
                else:
                    await context.log_manager.log("自动补卡成功，已更新卡参数")
                    login_data = await self._read_login_data(context)
                    redownload = login_data.get("reDownloadCard") or {}
                    card_profile_str = redownload.get("cardProfile") if isinstance(redownload, dict) else ""
                    if card_profile_str:
                        try:
                            card_profile = json.loads(card_profile_str) if isinstance(card_profile_str, str) else dict(card_profile_str)
                            card_provisioning = (card_profile.get("cardProvisioningData") or {})
                            dc_cp_mpp = (card_provisioning.get("DC_CP_MPP") or {})
                            payment_data = (dc_cp_mpp.get("contactlessPaymentData") or {})
                            replenishment_list = card_profile.get("cardReplenishmentData") or []
                            replenishment_data = replenishment_list[0] if isinstance(replenishment_list, list) and replenishment_list else {}
                        except Exception:
                            pass

            pan = payment_data.get("PAN", "")
            expiry = (payment_data.get("applicationExpiryDate", "") or "")[:4]  # YYMM
            psn = payment_data.get("PSN", "01")
            aip = payment_data.get("AIP", "00C0")
            token_requester_id = payment_data.get("tokenRequesterId", "")
            account_parameter_index = replenishment_data.get("accountParameterIndex", "")
            luk_cl_md = replenishment_data.get("lukClMd", "")
            luk_cl_umd = replenishment_data.get("lukClUmd", "")
            luk_rp_md = replenishment_data.get("lukRpMd", "")
            luk_rp_umd = replenishment_data.get("lukRpUmd", "")

            if not all([pan, expiry, token_requester_id, account_parameter_index, luk_cl_md, luk_cl_umd]):
                await context.log_manager.log("cardProfile缺少必要字段，无法生成加密转账数据")
                return None

            await context.log_manager.log(
                "转账核心字段"
                f": pan_tail={pan[-4:] if pan else ''}, expiry={expiry}, psn={psn}, "
                f"tokenRequesterId={token_requester_id}, api={account_parameter_index}, atc={atc}"
            )

            # 读取PIN与mpaId（PIN用于派生密钥）
            # 兼容：优先 bank_code2（与你 do_login 的映射一致），其次 password
            pin = getattr(getattr(context, "account", None), "bank_code2", None) \
                or getattr(getattr(context, "account", None), "password", None) \
                or ""
            mpa_id = login_data.get("mpaId", "") or ""

            cert_der = _get_embedded_sign_cert_der()
            encryptor = NagadCashInEncryptor(
                cert_der=cert_der,
                pan=pan,
                expiry=expiry,
                psn=psn,
                aip=aip,
                token_requester_id=token_requester_id,
                account_parameter_index=account_parameter_index,
                luk_cl_md=(luk_rp_md if (is_b2b and use_rp_key and luk_rp_md) else luk_cl_md),
                luk_cl_umd=(luk_rp_umd if (is_b2b and use_rp_key and luk_rp_umd) else luk_cl_umd),
                pin=pin,
                mpa_id=mpa_id,
            )

            if is_b2b:
                sender_account = (from_account or "").strip()
                if not sender_account:
                    sender_account = str(getattr(getattr(context, "account", None), "bank_code1", "") or "").strip()
                if not sender_account:
                    await context.log_manager.log("B2B转账缺少发送方账号（from_account/bank_code1）")
                    return None
                await context.log_manager.log(
                    "构造B2B加密参数: "
                    f"from={sender_account}, amount={float(amount):.2f}, atc={int(atc)}, "
                    f"key_type={'lukRp' if (use_rp_key and luk_rp_md and luk_rp_umd) else 'lukCl'}"
                )
                req = encryptor.generate_encrypted_b2b_request(
                    from_account=sender_account,
                    amount=float(amount),
                    atc=int(atc),
                )
            else:
                await context.log_manager.log(
                    f"构造Cash-In加密参数: to={to_account}, amount={float(amount):.2f}, atc={int(atc)}"
                )
                req = encryptor.generate_encrypted_request(
                    destination_account=to_account,
                    amount=float(amount),
                    atc=int(atc),
                )

            encrypted_hex = (req or {}).get("data")
            if not encrypted_hex:
                await context.log_manager.log("生成加密请求失败")
                return None
            await context.log_manager.log(
                f"加密请求已生成: len={len(encrypted_hex)}, prefix={encrypted_hex[:32]}"
            )
            
            return encrypted_hex
            
        except Exception as e:
            await context.log_manager.log(f"准备转账参数失败: {str(e)}")
            return None

    async def _get_login_otp_payload(
        self,
        context: PayContext,
        *,
        credential_round_index: int,
        purpose: str,
        wait_seconds: int = 300,
    ) -> Dict[str, str]:
        """按轮次获取并解析登录OTP（支持多次调用）"""
        await context.log_manager.log(
            f"等待OTP验证码（轮次{credential_round_index}，{purpose}）"
        )

        raw_otp: Any = None
        try:
            raw_otp = await context.credential.get_next_otp(
                wait_seconds=wait_seconds,
                credential_round_index=credential_round_index,
                value_key="otp",
            )
        except TypeError:
            # 兼容不支持 credential_round_index/value_key 的旧实现
            raw_otp = await context.credential.get_next_otp(wait_seconds=wait_seconds)
        except Exception as e:
            await context.log_manager.log(f"获取OTP异常（轮次{credential_round_index}）: {str(e)}")
            return {}

        otp = ""
        uid = ""
        try:
            if isinstance(raw_otp, str):
                raw_text = raw_otp.strip()
                if raw_text.startswith("{") and raw_text.endswith("}"):
                    try:
                        raw_otp = json.loads(raw_text)
                    except Exception:
                        raw_otp = raw_text

            if isinstance(raw_otp, str):
                parts = raw_otp.strip().split()
                if len(parts) >= 1:
                    otp = parts[0].strip()
                if len(parts) >= 2:
                    uid = parts[1].strip()
            elif isinstance(raw_otp, dict):
                otp = str(
                    raw_otp.get("otp")
                    or raw_otp.get("verifyCode")
                    or raw_otp.get("authCode")
                    or raw_otp.get("code")
                    or ""
                ).strip()
                uid = str(
                    raw_otp.get("uid")
                    or raw_otp.get("deviceUid")
                    or raw_otp.get("uuid")
                    or ""
                ).strip()
        except Exception:
            otp, uid = "", ""

        if otp:
            await context.log_manager.log(
                f"OTP获取成功（轮次{credential_round_index}）"
            )
        else:
            await context.log_manager.log(
                f"OTP为空（轮次{credential_round_index}）"
            )

        result: Dict[str, str] = {}
        if otp:
            result["otp"] = otp
        if uid:
            result["uid"] = uid
        return result
    
    # ═══════════════════════════════════════════════
    # 公共接口方法（框架要求实现）
    # ═══════════════════════════════════════════════
    
    async def do_login(self, context: PayContext) -> ActionResult:
        """登录流程（对齐线上 pay_function 的 do_login(context) 签名）

        策略：
        - 若session已有 mpaId：优先走二次登录（不需要OTP）
        - 否则：走绑定设备登录（需要OTP+uid）
        """
        # 参数映射（对齐你原脚本 Uddokta.py：deviceId, phoneNumber, pin）
        phone_number = getattr(context.account, "bank_code1", None) or ""
        pin = getattr(context.account, "bank_code2", None) or ""
        device_id = ''.join(__import__("random").choices('0123456789abcdef', k=16))
        await context.log_manager.log(f"生成设备ID: {device_id}")
        await self._update_login_data(context, {"device_id": device_id})
        # 生成设备指纹
        X_KM_DEVICE_FGP = self._generate_device_fingerprint(device_id, device_id)

        # 先尝试二次登录
        login_data = await self._read_login_data(context)
        if login_data.get("mpaId"):
            await context.log_manager.log("===== 尝试二次登录（mpaId） =====")
            login_result = await self._login_with_mpa(context, phone_number, pin, X_KM_DEVICE_FGP)
            if "error" not in (login_result or {}):
                await self._update_login_data(context, {"login_valid": True})
                await context.log_manager.log("✅ 二次登录成功")
                return ActionResult.success_result()
            if self._is_atc_inactive_error(login_result):
                await context.log_manager.log("二次登录命中ATC未激活/过期错误，尝试补卡刷新后再重试一次二次登录")
                login_data_for_replenish = await self._read_login_data(context)
                last_api = self._get_last_account_parameter_index(login_data_for_replenish)
                replenish_result = await self._replenish_card(context, last_api=last_api)
                if "error" in replenish_result:
                    await context.log_manager.log(f"二次登录补卡刷新失败: {replenish_result.get('error')}")
                else:
                    await context.log_manager.log("二次登录补卡刷新成功，开始重试二次登录")
                    retry_login_result = await self._login_with_mpa(context, phone_number, pin, X_KM_DEVICE_FGP)
                    if "error" not in (retry_login_result or {}):
                        await self._update_login_data(context, {"login_valid": True})
                        await context.log_manager.log("✅ 二次登录重试成功")
                        return ActionResult.success_result()
                    await context.log_manager.log(f"二次登录重试失败: {retry_login_result.get('error')}")
            await context.log_manager.log(f"二次登录失败，将回退OTP绑定：{login_result.get('error')}")

        # ═══ 首次登录流程（OTP绑定设备）═══
            
            # TODO-①: 第1步：检查用户状态
        await context.log_manager.log("第1步：检查用户状态")
        result = await self._check_user_status(context, phone_number, X_KM_DEVICE_FGP)
            
            # TODO-②: ⚠️ 错误检查1（fallback）
        if "error" in result:
            await context.log_manager.log("检查用户状态失败")
            return ActionResult().fail(f"检查用户状态失败: {result.get('error')}")
            
            # 提取userId
        user_id = result.get("userId")
        if not user_id:
            return ActionResult().fail("检查用户状态成功但未返回userId")

        if result.get("verificationStatus") == "VERIFICATION_REQUIRED":
            await context.log_manager.log("检测到 VERIFICATION_REQUIRED，等待USSD换绑令牌")
            raw1 = await context.credential.get_next_otp(
                wait_seconds=300,
                credential_round_index=1,
                value_key="otp",
            )
            if not raw1:
                return ActionResult().fail("第一轮凭证获取超时")

            ussd_otp = self._extract_credential_value(raw1, "otp")
            if not ussd_otp:
                ussd_otp = self._extract_credential_value(raw1, "verifyCode")
            if not ussd_otp:
                return ActionResult().fail("第一轮未获取到 otp（checkUssdOtp）")
            ussd_result = await self._check_ussd_otp(context, phone_number, ussd_otp, X_KM_DEVICE_FGP)
            exec_status = (ussd_result or {}).get("executionStatus")
            if not isinstance(exec_status, dict) or exec_status.get("statusType") != "EXECUTED_SUCCESS":
                return ActionResult().fail(
                    f"USSD换绑验证失败: {(ussd_result or {}).get('error') or (ussd_result or {}).get('reason') or ussd_result}"
                )

        # TODO-③: 在设备验证后，先等待一次新设备认证OTP（流程变更）
        await context.log_manager.log("第2步：设备验证后等待新设备认证OTP（首次）")

        # TODO-④: 第3步：登录（发送OTP）
        await context.log_manager.log("第3步：登录（发送OTP）")
        login_result = await self._login(context, phone_number, pin, user_id, X_KM_DEVICE_FGP)

        # TODO-⑤: ⚠️ 错误检查2（fallback）
        if "error" in login_result:
            await context.log_manager.log("登录失败")
            return ActionResult().fail(f"登录失败: {login_result.get('error')}")

        # TODO-⑥: 再次获取OTP用于校验（参考 ACB 多次调用 OTP 框架）
        await context.log_manager.log("第4步：等待校验OTP（第二次）")
        raw2 = await context.credential.get_next_otp(
            wait_seconds=300,
            credential_round_index=2,
        )
        if not raw2:
            return ActionResult().fail("第二轮凭证获取超时（需 otp+uid）")

        try:
            data2 = raw2 if isinstance(raw2, dict) else (json.loads(raw2) if isinstance(raw2, str) and raw2.strip().startswith("{") else None)
        except json.JSONDecodeError:
            data2 = None
        if not isinstance(data2, dict):
            return ActionResult().fail("第二轮返回格式错误，应为 JSON（otp+uid）")

        otp = str(data2.get("otp", "") or "").strip()
        if not otp:
            otp = str(data2.get("verifyCode", "") or "").strip()
        uid = str(data2.get("uid", "") or "").strip()
        if not uid:
            return ActionResult().fail("第二轮字段不完整，需包含 otp(或verifyCode) 与 uid")
            
            # TODO-⑦: 第5步：验证OTP
        await context.log_manager.log(f"验证OTP: {otp}, uid: {uid}")
        verify_result = await self._verify_otp(
            context,
            otp,
            uid,
            user_id,
            phone_number,
            X_KM_DEVICE_FGP,
        )
            
            # TODO-⑧: ⚠️ 错误检查3（fallback，检查error和reason）
        if "error" in verify_result or "reason" in verify_result:
            await context.log_manager.log("OTP验证失败")
            return ActionResult().fail(f"OTP验证失败: {verify_result.get('error') or verify_result.get('reason')}")
            
            # TODO-⑨: ⚠️ 保存1：基础数据（副作用）
        await context.log_manager.log("OTP验证成功，保存基础数据")
            # 注意：_verify_otp已经保存了authSession、authToken、refreshToken，这里无需额外保存
            
            # TODO-⑩: 第6步：初始化钱包
        await context.log_manager.log("第6步：初始化钱包")
        wallet_result = await self._initialize_wallet(context, user_id, X_KM_DEVICE_FGP, device_id)
            
            # TODO-⑪: ⚠️ 错误检查4（fallback）
        if "error" in wallet_result:
            await context.log_manager.log("钱包初始化失败")
            return ActionResult().fail(f"钱包初始化失败: {wallet_result.get('error')}")
            
            # TODO-⑫: ⚠️ 保存2：钱包数据（副作用）
        await context.log_manager.log("钱包初始化成功，数据已保存")
            # 注意：_initialize_wallet已经保存了walletKeys、mpaId、par，这里无需额外保存
            
            # 与原始脚本保持一致：仅在首次无ATC时初始化为0，不覆盖已有ATC
        login_data_after_wallet = await self._read_login_data(context)
        if "atc" not in login_data_after_wallet:
            await self._update_login_data(context, {"atc": 0})
            
            # TODO-⑬: 第7步：重新下载卡片
        await context.log_manager.log("第7步：重新下载卡片")
        redownload_result = await self._redownload_card(context, purpose="DEVICE_CHANGE")
            
            # TODO-⑭: ⚠️ 错误检查5（fallback）
        if "error" in redownload_result:
            await context.log_manager.log("重新下载卡片失败（钱包数据已保存）")
            return ActionResult().fail(f"重新下载卡片失败: {redownload_result.get('error')}")
            
            # TODO-⑮: ⚠️ 保存3：卡片数据（副作用）
        await self._update_login_data(context, {"reDownloadCard": redownload_result})
        await context.log_manager.log("卡片数据已保存（reDownloadCard已写入session）")
            
            # TODO-⑯: 返回成功结果
        await self._update_login_data(context, {"login_valid": True})
        login_data = await self._read_login_data(context)
        await context.log_manager.log("===== 登录成功 =====")
        await context.log_manager.log(f"用户ID: {login_data.get('userId')}")
        await context.log_manager.log(f"MPA ID: {login_data.get('mpaId')}")
        await context.log_manager.log(f"PAR: {login_data.get('par')}")
        return ActionResult.success_result()
    
    async def do_check_balance(self, context: PayContext) -> ActionResult:
        """查询账户余额
        
        Returns:
            ActionResult，data中包含余额信息
        """
        result = await self._get_balance(context)
        
        if "error" in result:
            return ActionResult().fail(f"查询余额失败: {result.get('error')}")
        
        # 提取余额
        balance_list = result.get("balanceList", [])
        if balance_list and len(balance_list) > 0:
            balance_info = balance_list[0]
            balance = balance_info.get("balance", 0)
            available_balance = balance_info.get("availableBalance", 0)
            
            await context.log_manager.log(f"账户余额: {balance} BDT")
            await context.log_manager.log(f"可用余额: {available_balance} BDT")

            # ⚠️ 兼容：部分版本 ActionResult.success_result 不支持 data= 形参
            ok_res = ActionResult.success_result()
            payload = {
                "balance": balance,
                "available_balance": available_balance,
                "currency": "BDT",
            }
            try:
                setattr(ok_res, "data", payload)
            except Exception:
                try:
                    setattr(ok_res, "result", payload)
                except Exception:
                    pass
            return ok_res
        else:
            return ActionResult().fail("未获取到余额信息")
    
    async def do_lookup_transactions(self, context: PayContext, days: int = 7) -> ActionResultT[List[ParsedTransaction]]:
        """查询交易流水
        
        Args:
            context: 支付上下文
            days: 查询最近N天的交易
            
        Returns:
            ActionResultT[List[ParsedTransaction]]
        """
        transactions = []
        
        # 计算时间范围（毫秒时间戳）
        now_ms = int(datetime.now().timestamp() * 1000)
        days_ago_ms = int((datetime.now().timestamp() - days * 24 * 3600) * 1000)
        
        # 查询第一页
        result = await self._get_transaction_history_page(
            context,
            page=0,
            page_size=20,
            date_from=days_ago_ms,
            date_to=now_ms
        )
        
        if "error" in result:
            return ActionResultT[List[ParsedTransaction]]().fail(f"查询交易流水失败: {result.get('error')}")
        
        # 解析交易记录
        content = result.get("content", [])
        for tx_data in content:
            if isinstance(tx_data, dict):
                tx_category = str(tx_data.get("transactionCategory") or "").strip().upper()
                if tx_category == "COMMISSION":
                    continue
            tx = self._parse_transaction(tx_data)
            if tx:
                transactions.append(tx)
        
        await context.log_manager.log(f"查询到 {len(transactions)} 条交易记录")
        
        return ActionResultT[List[ParsedTransaction]]().ok(transactions)

    async def do_lookup_transactions_by_utr(
        self,
        context: PayContext,
        utrs: List[str],
    ) -> ActionResultT[List[ParsedTransaction]]:
        """按UTR查询交易流水：逐个UTR调用交易搜索接口并精确匹配。"""
        try:
            normalized: List[str] = []
            seen = set()
            for utr in (utrs or []):
                key = str(utr or "").strip()
                if not key or key in seen:
                    continue
                seen.add(key)
                normalized.append(key)

            if not normalized:
                return ActionResultT[List[ParsedTransaction]]().ok([])

            matched: List[ParsedTransaction] = []
            for utr in normalized:
                result = await self._get_transaction_history_page(
                    context,
                    page=0,
                    page_size=20,
                    txn_id=utr,
                )
                if "error" in result:
                    return ActionResultT[List[ParsedTransaction]]().fail(
                        f"按UTR查询失败: {result.get('error')}"
                    )

                content = result.get("content", [])
                for tx_data in content:
                    if isinstance(tx_data, dict):
                        tx_category = str(tx_data.get("transactionCategory") or "").strip().upper()
                        if tx_category == "COMMISSION":
                            continue
                    tx = self._parse_transaction(tx_data)
                    if not tx:
                        continue
                    tx_code = str(getattr(tx, "transaction_code", "") or "").strip()
                    if tx_code == utr:
                        matched.append(tx)

            return ActionResultT[List[ParsedTransaction]]().ok(matched)
        except Exception as e:
            return ActionResultT[List[ParsedTransaction]]().fail(str(e))
    
    async def do_transfer(
        self,
        context: PayContext,
        to_account: str,
        amount: float,
        note: str = "",
        *,
        is_b2b: bool = False,
        from_account: str = "",
    ) -> ActionResult:
        """转账（Cash-In / B2B）
        
        Args:
            context: 支付上下文
            to_account: 收款账号
            amount: 转账金额
            note: 转账备注（未使用）
            
        Returns:
            ActionResult
        """
        tx_name = "B2B" if is_b2b else "Cash-In"
        result: Dict[str, Any] = {}
        use_rp_key = False
        atc_replenish_retried = False
        ssl_cert_replenish_retried = False
        http417_replenish_retried = False
        for attempt in range(3):
            encrypted_data = await self._prepare_transfer_params(
                context,
                to_account,
                amount,
                is_b2b=is_b2b,
                from_account=from_account,
                use_rp_key=use_rp_key,
            )
            if not encrypted_data:
                return ActionResult().fail("准备转账参数失败")

            if is_b2b:
                result = await self._b2b_to_parent(context, encrypted_data)
            else:
                result = await self._cash_in(context, encrypted_data)

            if "error" not in result:
                break

            # 会话超时：只二次登录恢复会话，绝不重试转账，避免重复代付造成损失
            if self._is_auth_session_timeout(result):
                await context.log_manager.log(
                    f"{tx_name}转账返回会话超时，尝试二次登录恢复会话（不重试转账，避免重复代付）..."
                )
                try:
                    relog = await self._relogin_after_session_timeout(context)
                    if isinstance(relog, dict) and relog.get("error"):
                        if self._is_atc_inactive_error(relog):
                            await context.log_manager.log(
                                f"{tx_name}二次登录命中ATC未激活/过期错误，尝试补卡刷新后重试二次登录"
                            )
                            latest_login_data = await self._read_login_data(context)
                            last_api = self._get_last_account_parameter_index(latest_login_data)
                            replenish_result = await self._replenish_card(context, last_api=last_api)
                            if "error" not in replenish_result:
                                await self._relogin_after_session_timeout(context)
                except Exception as e:
                    await context.log_manager.log(f"{tx_name}会话超时二次登录异常: {str(e)}")
                return ActionResult().fail(
                    f"{tx_name}转账失败: 会话超时，已尝试二次登录，需人工确认是否已到账"
                )

            # 命中 SSL 证书过期错误时，补卡刷新后重试一次
            if self._is_ssl_cert_expired_error(result) and not ssl_cert_replenish_retried:
                await context.log_manager.log(f"{tx_name}命中SSL证书过期错误，尝试补卡刷新后重试一次")
                latest_login_data = await self._read_login_data(context)
                last_api = self._get_last_account_parameter_index(latest_login_data)
                replenish_result = await self._replenish_card(context, last_api=last_api)
                ssl_cert_replenish_retried = True
                if "error" in replenish_result:
                    await context.log_manager.log(f"{tx_name}证书错误场景补卡刷新失败: {replenish_result.get('error')}")
                else:
                    await context.log_manager.log(f"{tx_name}证书错误场景补卡刷新成功，继续重试转账")
                use_rp_key = False
                continue

            # 命中 ATC 未激活/过期错误时，补卡刷新后重试一次
            if self._is_atc_inactive_error(result) and not atc_replenish_retried:
                await context.log_manager.log(f"{tx_name}命中ATC未激活/过期错误，尝试补卡刷新后重试一次")
                latest_login_data = await self._read_login_data(context)
                last_api = self._get_last_account_parameter_index(latest_login_data)
                replenish_result = await self._replenish_card(context, last_api=last_api)
                atc_replenish_retried = True
                if "error" in replenish_result:
                    await context.log_manager.log(f"{tx_name}补卡刷新失败: {replenish_result.get('error')}")
                else:
                    await context.log_manager.log(f"{tx_name}补卡刷新成功，继续重试转账")
                use_rp_key = False
                continue

            # 命中 HTTP 417「某处出错，请致电 16167」时，补卡刷新后重试一次
            if self._is_http_417_support_error(result) and not http417_replenish_retried:
                await context.log_manager.log(f"{tx_name}命中HTTP 417通用故障，尝试补卡刷新后重试一次")
                latest_login_data = await self._read_login_data(context)
                last_api = self._get_last_account_parameter_index(latest_login_data)
                replenish_result = await self._replenish_card(context, last_api=last_api)
                http417_replenish_retried = True
                if "error" in replenish_result:
                    await context.log_manager.log(f"{tx_name}HTTP 417场景补卡刷新失败: {replenish_result.get('error')}")
                else:
                    await context.log_manager.log(f"{tx_name}HTTP 417场景补卡刷新成功，继续重试转账")
                use_rp_key = False
                continue

            # 命中 ISO 报文格式错误时，分阶段重试：
            # 1) B2B 场景先尝试 lukRp（ATC 会自动+1）
            # 2) 再尝试 reDownloadCard 刷新卡参数（ATC 自动+1）
            if self._is_iso_format_error(result):
                if is_b2b and attempt == 0:
                    await context.log_manager.log(
                        f"{tx_name}命中ISO格式错误，切换lukRp密钥再重试一次"
                    )
                    use_rp_key = True
                    continue
                if attempt <= 1:
                    await context.log_manager.log(
                        f"{tx_name}命中ISO格式错误，尝试reDownloadCard刷新卡参数后重试一次"
                    )
                    redownload = await self._redownload_card(context, purpose="DEVICE_CHANGE")
                    if "error" in redownload:
                        await context.log_manager.log(
                            f"reDownloadCard刷新失败: {redownload.get('error')}，继续尝试仅递增ATC重试"
                        )
                        use_rp_key = False
                        continue
                    await self._update_login_data(context, {"reDownloadCard": redownload})
                    use_rp_key = False
                    continue
            break

        if "error" in result:
            return ActionResult().fail(f"{tx_name}转账失败: {result.get('error')}")
        
        # 提取转账结果
        tx_ref = result.get("trxReferenceNumber", "")
        response_msg = result.get("responseMessage", "")
        other_amounts = result.get("otherAmounts", {})
        
        await context.log_manager.log(f"{tx_name}转账成功，参考号: {tx_ref}")
        await context.log_manager.log(f"响应消息: {response_msg}")

        # ⚠️ 兼容：部分版本 ActionResult.success_result 不支持 data= 形参
        ok_res = ActionResult.success_result()
        payload = {
            "transaction_id": tx_ref,
            "response_message": response_msg,
            "other_amounts": other_amounts
        }
        try:
            setattr(ok_res, "data", payload)
        except Exception:
            try:
                setattr(ok_res, "result", payload)
            except Exception:
                pass
        return ok_res
    
    async def do_is_webpage_session_alive(self, context: PayContext) -> bool:
        """检查会话是否存活（对齐线上 pay_function：返回 bool）"""
        try:
            login_data = await self._read_login_data(context)
            auth_token = login_data.get("authToken")
            if not auth_token:
                return False
            # 轻量判断：有token即认为存活；需要更严格可在这里调用 _get_balance + refresh
            return True
        except Exception:
            return False
        
    async def support_choose_upi(self, context: PayContext) -> bool:
        return False
    
    async def support_query_balance(self, context: PayContext) -> bool:
        # await context.log_manager.log("Uddokta支持余额查询")
        return True

    async def support_lookup_transactions_by_utr(self, context: PayContext) -> bool:
        return True

    async def support_payout(self, context: PayContext) -> bool:
        # await context.log_manager.log("Nagad启用代付功能")
        return True

    async def do_payout_items(self, context: PayContext, items: List[PayoutItem]) -> Dict[str, PayoutResult]:
        """代付（转账）：对齐 bKashAgent 的框架用法，返回 PayoutResult 字典。

        说明：
        - Nagad 的实际转账实现复用 do_transfer（Cash-In）
        - PayoutItem.amount 约定为“分”（与测试/其他渠道一致），这里转换为主币种 float
        """
        results: Dict[str, PayoutResult] = {}
        await context.log_manager.log(f"【代付】开始处理 {len(items)} 个代付项")

        login_data = await self._read_login_data(context)
        auth_token = login_data.get("authToken")
        if not auth_token or not bool(login_data.get("login_valid")):
            await context.log_manager.log("【代付】检测到登录失效，尝试自动二次登录")
            relogin_res = await self.do_login(context)
            relogin_ok = bool(
                getattr(relogin_res, "is_success", False)
                or getattr(relogin_res, "success", False)
            )
            if not relogin_ok:
                relogin_msg = (
                    getattr(relogin_res, "error_message", None)
                    or getattr(relogin_res, "message", None)
                    or "自动二次登录失败"
                )
                for item in items:
                    results[item.item_id] = PayoutResult.uncertain_failure(
                        f"登录失效且自动重登失败: {relogin_msg}"
                    )
                return results
            login_data = await self._read_login_data(context)
            auth_token = login_data.get("authToken")
            if not auth_token or not bool(login_data.get("login_valid")):
                for item in items:
                    results[item.item_id] = PayoutResult.uncertain_failure(
                        "登录失效且自动重登后仍无有效会话"
                    )
                return results
            await context.log_manager.log("【代付】自动二次登录成功，继续处理代付")

        # 获取转账PIN：与 do_transfer 内部读取保持一致，仅用于提前校验缺失场景
        transfer_pin = (
            getattr(getattr(context, "account", None), "bank_code2", None)
            or getattr(getattr(context, "account", None), "password", None)
            or ""
        )
        if not str(transfer_pin).strip():
            for item in items:
                results[item.item_id] = PayoutResult.uncertain_failure("缺少转账PIN/密码")
            return results

        for item in items:
            try:
                is_b2b = bool(getattr(item, "is_b2b", False))
                receiver_phone = getattr(item, "account_number", None)
                raw_amount = getattr(item, "amount", None)
                if raw_amount is None:
                    amount_main = 0.0
                else:
                    amount_main = float((Decimal(str(raw_amount)) / Decimal("100")).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))

                # 与 Uddokta20260601.py 保持一致：
                # - Cash-In 需要 destination_account（收款号码）
                # - B2B 只需要 from_account（本机号码），不依赖收款号码
                if (not is_b2b) and (not receiver_phone):
                    results[item.item_id] = PayoutResult.definite_failure("收款号码为空（cash-in 必填）")
                    continue
                if amount_main <= 0:
                    results[item.item_id] = PayoutResult.definite_failure("金额无效")
                    continue

                await context.log_manager.log(
                    f"【代付】开始转账: itemId={item.item_id}, is_b2b={is_b2b}, phone={receiver_phone}, amount={amount_main}"
                )

                # note 当前在 do_transfer 中未使用；保留透传以便后续扩展/排查
                note = getattr(item, "order_code", None) or ""
                sender_phone = str(getattr(getattr(context, "account", None), "bank_code1", "") or "").strip()
                transfer_res = await self.do_transfer(
                    context,
                    str(receiver_phone or ""),
                    float(amount_main),
                    note=str(note),
                    is_b2b=is_b2b,
                    from_account=sender_phone,
                )

                ok_flag = bool(getattr(transfer_res, "is_success", False) or getattr(transfer_res, "success", False))
                if ok_flag:
                    data = getattr(transfer_res, "data", None) or {}
                    tx_id = data.get("transaction_id") or ""
                    if tx_id:
                        results[item.item_id] = PayoutResult.success(utr=str(tx_id))
                    else:
                        results[item.item_id] = PayoutResult.uncertain_failure("转账成功但未获取到transaction_id")
                else:
                    msg = getattr(transfer_res, "error_message", None) or getattr(transfer_res, "message", None) or ""
                    results[item.item_id] = self._build_payout_failure_result(
                        f"转账失败: {msg or '未知错误'}",
                        "转账失败，需人工确认",
                    )
            except Exception as e:
                results[item.item_id] = PayoutResult.uncertain_failure(f"异常: {str(e)}")

        await context.log_manager.log(f"【代付】处理完成，共 {len(results)} 个结果")
        return results

    async def query_balance(self, context: PayContext) -> ActionResultT[int]:
        """返回单位：分（*100）"""
        try:
            bal_res = await self.do_check_balance(context)
            ok_flag = bool(getattr(bal_res, "is_success", False) or getattr(bal_res, "success", False))
            if not bal_res or not ok_flag:
                return ActionResultT[int]().fail(getattr(bal_res, "error_message", "") or "查询余额失败")
            data = getattr(bal_res, "data", None) or {}
            val = data.get("available_balance")
            if val is None:
                val = data.get("balance")
            if val is None:
                return ActionResultT[int]().fail("查询余额失败")
            cents = int(float(val) * 100)
            return ActionResultT[int]().ok(cents)
        except Exception as e:
            return ActionResultT[int]().fail(str(e))


_EMBEDDED_SIGN_CERT_DER_CACHE: Optional[bytes] = None


def _get_embedded_sign_cert_der() -> bytes:
    global _EMBEDDED_SIGN_CERT_DER_CACHE
    if _EMBEDDED_SIGN_CERT_DER_CACHE is None:
        _EMBEDDED_SIGN_CERT_DER_CACHE = binascii.a2b_base64(_EMBEDDED_SIGN_CERT_DER_B64)
    return _EMBEDDED_SIGN_CERT_DER_CACHE


def _generate_prime(bits: int) -> int:
    def _is_prime(n: int, k: int = 10) -> bool:
        if n < 2:
            return False
        if n in (2, 3):
            return True
        if n % 2 == 0:
            return False
        r, d = 0, n - 1
        while d % 2 == 0:
            r += 1
            d //= 2
        for _ in range(k):
            a = random.randrange(2, n - 1)
            x = pow(a, d, n)
            if x == 1 or x == n - 1:
                continue
            for _ in range(r - 1):
                x = pow(x, 2, n)
                if x == n - 1:
                    break
            else:
                return False
        return True

    while True:
        p = random.getrandbits(bits)
        p |= (1 << bits - 1) | 1
        if _is_prime(p):
            return p


def _extended_gcd(a: int, b: int) -> tuple:
    if a == 0:
        return b, 0, 1
    gcd, x1, y1 = _extended_gcd(b % a, a)
    x = y1 - (b // a) * x1
    y = x1
    return gcd, x, y


def _mod_inverse(e: int, phi: int) -> int:
    gcd, x, _ = _extended_gcd(e % phi, phi)
    if gcd != 1:
        raise Exception("模逆元不存在")
    return (x % phi + phi) % phi


class _RSAKeyPure:
    def __init__(self, n: int, e: int, d: Optional[int] = None, p: Optional[int] = None, q: Optional[int] = None):
        self.n = n
        self.e = e
        self.d = d
        self.p = p
        self.q = q

    @classmethod
    def generate(cls, bits: int = 512):
        e = 65537
        while True:
            p = _generate_prime(bits // 2)
            q = _generate_prime(bits // 2)
            if p != q:
                n = p * q
                phi = (p - 1) * (q - 1)
                if phi % e != 0:
                    d = _mod_inverse(e, phi)
                    return cls(n, e, d, p, q)

    def decrypt(self, ciphertext: bytes) -> bytes:
        c = int.from_bytes(ciphertext, "big")
        m = pow(c, self.d, self.n)
        em = m.to_bytes((self.n.bit_length() + 7) // 8, "big")
        if em[0:2] != b"\x00\x02":
            raise ValueError("解密失败: 无效的 PKCS1 padding")
        sep_idx = em.index(b"\x00", 2)
        return em[sep_idx + 1:]


def _generate_rsa_keypair(key_size: int = 512):
    key = _RSAKeyPure.generate(key_size)
    return key, key


def _int_to_bytes(n: int, length: Optional[int] = None) -> bytes:
    if length is None:
        length = (n.bit_length() + 7) // 8
    return n.to_bytes(length, "big")


def _encode_der_integer(value: int) -> bytes:
    if value == 0:
        return b"\x02\x01\x00"
    value_bytes = _int_to_bytes(value)
    if value_bytes[0] & 0x80:
        value_bytes = b"\x00" + value_bytes
    return b"\x02" + _encode_der_length(len(value_bytes)) + value_bytes


def _encode_der_length(length: int) -> bytes:
    if length < 128:
        return bytes([length])
    if length < 256:
        return bytes([0x81, length])
    return bytes([0x82, (length >> 8) & 0xFF, length & 0xFF])


def _encode_der_sequence(contents: bytes) -> bytes:
    return b"\x30" + _encode_der_length(len(contents)) + contents


def _encode_der_bitstring(contents: bytes) -> bytes:
    data = b"\x00" + contents
    return b"\x03" + _encode_der_length(len(data)) + data


def _encode_der_oid(oid_str: str) -> bytes:
    parts = [int(x) for x in oid_str.split(".")]
    encoded = bytes([parts[0] * 40 + parts[1]])
    for part in parts[2:]:
        if part < 128:
            encoded += bytes([part])
        else:
            octets = []
            while part > 0:
                octets.insert(0, (part & 0x7F) | (0x80 if octets else 0))
                part >>= 7
            encoded += bytes(octets)
    return b"\x06" + _encode_der_length(len(encoded)) + encoded


def _encode_der_printable_string(s: str) -> bytes:
    data = s.encode("ascii")
    return b"\x13" + _encode_der_length(len(data)) + data


def _encode_der_ia5_string(s: str) -> bytes:
    data = s.encode("ascii")
    return b"\x16" + _encode_der_length(len(data)) + data


def _encode_der_set(contents: bytes) -> bytes:
    return b"\x31" + _encode_der_length(len(contents)) + contents


def _encode_der_null() -> bytes:
    return b"\x05\x00"


def _build_rdn(oid: str, value: str, string_type: str = "printable") -> bytes:
    oid_encoded = _encode_der_oid(oid)
    value_encoded = _encode_der_ia5_string(value) if string_type == "ia5" else _encode_der_printable_string(value)
    attr_seq = _encode_der_sequence(oid_encoded + value_encoded)
    return _encode_der_set(attr_seq)


def _generate_csr(private_key: _RSAKeyPure, user_id: str) -> bytes:
    oid_rsa = "1.2.840.113549.1.1.1"
    oid_sha1_rsa = "1.2.840.113549.1.1.5"
    oid_email = "1.2.840.113549.1.9.1"
    oid_cn = "2.5.4.3"
    oid_c = "2.5.4.6"
    oid_l = "2.5.4.7"
    oid_st = "2.5.4.8"
    oid_o = "2.5.4.10"
    oid_ou = "2.5.4.11"

    subject = b""
    subject += _build_rdn(oid_email, str(user_id), "ia5")
    subject += _build_rdn(oid_c, "test")
    subject += _build_rdn(oid_st, "test")
    subject += _build_rdn(oid_l, "test")
    subject += _build_rdn(oid_o, "KonaWallet")
    subject += _build_rdn(oid_ou, "KonaWallet")
    subject += _build_rdn(oid_cn, "KonaWallet")
    subject_seq = _encode_der_sequence(subject)

    n = private_key.n
    e = private_key.e
    rsa_public_key = _encode_der_sequence(_encode_der_integer(n) + _encode_der_integer(e))
    alg_id = _encode_der_sequence(_encode_der_oid(oid_rsa) + _encode_der_null())
    spki = _encode_der_sequence(alg_id + _encode_der_bitstring(rsa_public_key))

    version = b"\x02\x01\x00"
    attributes = b"\xA0\x00"
    cert_req_info = _encode_der_sequence(version + subject_seq + spki + attributes)

    hash_value = hashlib.sha1(cert_req_info).digest()
    digest_info = bytes.fromhex("3021300906052b0e03021a05000414") + hash_value

    key_len = (n.bit_length() + 7) // 8
    padding_len = key_len - len(digest_info) - 3
    padded = b"\x00\x01" + (b"\xFF" * padding_len) + b"\x00" + digest_info
    m = int.from_bytes(padded, "big")
    s = pow(m, private_key.d, n)
    signature = _int_to_bytes(s, key_len)

    sig_alg_id = _encode_der_sequence(_encode_der_oid(oid_sha1_rsa) + _encode_der_null())
    return _encode_der_sequence(cert_req_info + sig_alg_id + _encode_der_bitstring(signature))


def _get_private_key_hex(private_key: _RSAKeyPure) -> str:
    oid_rsa = "1.2.840.113549.1.1.1"
    n, e, d = private_key.n, private_key.e, private_key.d
    p, q = private_key.p, private_key.q
    dp = d % (p - 1)
    dq = d % (q - 1)
    qinv = _mod_inverse(q, p)
    rsa_priv = _encode_der_sequence(
        _encode_der_integer(0)
        + _encode_der_integer(n)
        + _encode_der_integer(e)
        + _encode_der_integer(d)
        + _encode_der_integer(p)
        + _encode_der_integer(q)
        + _encode_der_integer(dp)
        + _encode_der_integer(dq)
        + _encode_der_integer(qinv)
    )
    alg_id = _encode_der_sequence(_encode_der_oid(oid_rsa) + _encode_der_null())
    priv_octet = b"\x04" + _encode_der_length(len(rsa_priv)) + rsa_priv
    private_bytes = _encode_der_sequence(_encode_der_integer(0) + alg_id + priv_octet)
    return binascii.hexlify(private_bytes).decode("utf-8").upper()


def _get_public_key_hex(private_key: _RSAKeyPure) -> str:
    oid_rsa = "1.2.840.113549.1.1.1"
    n, e = private_key.n, private_key.e
    rsa_public_key = _encode_der_sequence(_encode_der_integer(n) + _encode_der_integer(e))
    alg_id = _encode_der_sequence(_encode_der_oid(oid_rsa) + _encode_der_null())
    public_bytes = _encode_der_sequence(alg_id + _encode_der_bitstring(rsa_public_key))
    return binascii.hexlify(public_bytes).decode("utf-8").upper()


def _get_csr_hex(csr: bytes) -> str:
    return binascii.hexlify(csr).decode("utf-8").upper()


def decrypt_with_private_key(encrypted_hex: str, private_key: _RSAKeyPure):
    encrypted_bytes = binascii.unhexlify(encrypted_hex)
    return private_key.decrypt(encrypted_bytes)


def generate_wallet_keys(user_id: str, key_size: int = 512):
    private_key, _ = _generate_rsa_keypair(key_size)
    csr = _generate_csr(private_key, str(user_id))
    return {
        "private_key": private_key,
        "private_key_hex": _get_private_key_hex(private_key),
        "public_key_hex": _get_public_key_hex(private_key),
        "csr_hex": _get_csr_hex(csr),
        "user_id": str(user_id),
    }


def _shift_pin(pin: str) -> bytes:
    return bytes([(ord(c) << 1) & 0xFF for c in pin])


def _xor_pin_with_key(pin_shifted: bytes, key: bytes) -> bytes:
    result = bytearray(key)
    pin_len = min(8, len(pin_shifted))
    for i in range(pin_len):
        result[i] = key[i] ^ pin_shifted[i]
        result[i + 8] = key[i + 8] ^ pin_shifted[i]
    return bytes(result)


def _derive_key_with_pin(stored_key: str, pin: str) -> str:
    key_bytes = bytes.fromhex(stored_key)
    pin_shifted = _shift_pin(pin)
    derived = _xor_pin_with_key(pin_shifted, key_bytes)
    return derived.hex().upper()


class _NagadDCVVCalculator:
    def __init__(self, luk_cl_md: str, luk_cl_umd: Optional[str] = None):
        self.luk_cl_md = binascii.unhexlify(luk_cl_md)
        self.luk_cl_umd = binascii.unhexlify(luk_cl_umd) if luk_cl_umd else None
        if len(self.luk_cl_md) != 16:
            raise ValueError("lukClMd 必须是 16 字节")
        if self.luk_cl_umd and len(self.luk_cl_umd) != 16:
            raise ValueError("lukClUmd 必须是 16 字节")

    def _iso9797_1_mac(self, data: bytes, key: bytes) -> bytes:
        if len(data) % 8 != 0:
            raise ValueError(f"数据长度必须是 8 字节倍数, 当前: {len(data)}")
        ka, kb = key[:8], key[8:]
        des_ka = DES.new(ka, DES.MODE_ECB)
        des_kb = DES.new(kb, DES.MODE_ECB)
        result = des_ka.encrypt(data[:8])
        for i in range(8, len(data), 8):
            block = data[i : i + 8]
            xored = bytes(a ^ b for a, b in zip(result, block))
            result = des_ka.encrypt(xored)
        return des_ka.encrypt(des_kb.decrypt(result))

    @staticmethod
    def _decimalize(mac_hex: str) -> str:
        digits, letters = "", ""
        for c in mac_hex.upper():
            if "0" <= c <= "9":
                digits += c
            elif "A" <= c <= "F":
                letters += str(ord(c) - ord("A"))
        return digits + letters

    @staticmethod
    def _build_dcvv_input(
        pan: str, atc: str, expiry: str, api: str, amount: str, other_amount: str, country: str, currency: str, date: str, tx_type: str, un: str
    ) -> str:
        pan = pan[:16].zfill(16)
        atc = atc[-4:].zfill(4)
        expiry = expiry[:4]
        api = api[-6:]
        amount = amount[-12:].zfill(12)
        other_amount = other_amount[-12:].zfill(12)
        country = country[-4:].zfill(4)
        currency = currency[-4:].zfill(4)
        date = date[-6:].zfill(6)
        tx_type = tx_type[-2:].zfill(2)
        un = un[-8:].zfill(8)
        input_str = pan + atc + expiry + "201" + api + amount + other_amount + country + currency + date + tx_type + un
        return input_str + ("0" * max(0, 96 - len(input_str)))

    def _calculate(self, key: bytes, **kwargs) -> str:
        input_str = self._build_dcvv_input(**kwargs)
        input_bytes = binascii.unhexlify(input_str)
        mac = self._iso9797_1_mac(input_bytes, key)
        return self._decimalize(mac.hex().upper())[:3]

    def generate_track2(
        self, pan: str, atc: str, expiry: str, api: str, amount: str, other_amount: str = "000000000000", country: str = "0050",
        currency: str = "0050", date: Optional[str] = None, tx_type: str = "60", un: Optional[str] = None
    ):
        if un is None:
            un = secrets.token_hex(4).upper()
        if date is None:
            date = datetime.now().strftime("%y%m%d")
        kwargs = {
            "pan": pan,
            "atc": atc,
            "expiry": expiry,
            "api": api,
            "amount": amount,
            "other_amount": other_amount,
            "country": country,
            "currency": currency,
            "date": date,
            "tx_type": tx_type,
            "un": un,
        }
        dcvv1 = self._calculate(self.luk_cl_md, **kwargs)
        if not self.luk_cl_umd:
            raise ValueError("lukClUmd 未设置")
        dcvv2 = self._calculate(self.luk_cl_umd, **kwargs)
        track2 = f"{pan}D{expiry[:4]}{dcvv1}{api[-6:]}{dcvv2}{atc[-4:].zfill(4)}F"
        return track2, un


class _CashInDataGeneratorV2:
    def __init__(self, pan: str, expiry: str, psn: str, aip: str, token_requester_id: str, account_parameter_index: str, luk_cl_md: str, luk_cl_umd: str, pin: str):
        self._pan = pan
        self._expiry = expiry
        self._psn = psn
        self._aip = aip
        self._token_requester_id = token_requester_id
        self._api = account_parameter_index
        derived = _derive_key_with_pin(luk_cl_umd, pin)
        self._dcvv = _NagadDCVVCalculator(luk_cl_md, derived)

    def get_pan(self) -> str:
        return self._pan

    def get_expiry_yymm(self) -> str:
        return self._expiry[:4] if len(self._expiry) >= 4 else self._expiry

    def get_psn(self) -> str:
        return self._psn.zfill(3) if self._psn else "001"

    def get_token_requester_id(self) -> str:
        return self._token_requester_id

    def format_amount(self, amount: float) -> str:
        return str(int(float(amount) * 100)).zfill(12)

    def get_aip(self) -> str:
        return self._aip if self._aip else "00C0"

    def generate_track2(self, atc: str, amount: float, tx_date: Optional[str] = None, tx_type: str = "60", un: Optional[str] = None):
        return self._dcvv.generate_track2(
            pan=self.get_pan(),
            atc=atc,
            expiry=self.get_expiry_yymm(),
            api=self._api,
            amount=self.format_amount(amount),
            date=tx_date,
            tx_type=tx_type,
            un=un,
        )

    def generate_de55(self, amount: float, un: str) -> str:
        tx_date = datetime.now().strftime("%y%m%d")
        return (
            f"9F0206{self.format_amount(amount)}"
            "9F0306000000000000"
            "9C0160"
            f"9F3704{un}"
            f"9A03{tx_date}"
            "5F2A020050"
            "9F1A020050"
            f"8202{self.get_aip()}"
        )


def _asn1_encode_length(length: int) -> bytes:
    if length < 0x80:
        return bytes([length])
    if length < 0x100:
        return bytes([0x81, length])
    if length < 0x10000:
        return bytes([0x82, (length >> 8) & 0xFF, length & 0xFF])
    if length < 0x1000000:
        return bytes([0x83, (length >> 16) & 0xFF, (length >> 8) & 0xFF, length & 0xFF])
    return bytes([0x84, (length >> 24) & 0xFF, (length >> 16) & 0xFF, (length >> 8) & 0xFF, length & 0xFF])


def _asn1_encode_integer(value: int) -> bytes:
    if value == 0:
        content = b"\x00"
    else:
        hx = format(value, "x")
        if len(hx) % 2:
            hx = "0" + hx
        content = bytes.fromhex(hx)
        if content[0] & 0x80:
            content = b"\x00" + content
    return b"\x02" + _asn1_encode_length(len(content)) + content


def _asn1_encode_octet_string(data: bytes) -> bytes:
    return b"\x04" + _asn1_encode_length(len(data)) + data


def _asn1_encode_sequence(contents: bytes) -> bytes:
    return b"\x30" + _asn1_encode_length(len(contents)) + contents


def _asn1_encode_set(contents: bytes) -> bytes:
    return b"\x31" + _asn1_encode_length(len(contents)) + contents


def _asn1_encode_oid(oid_tuple: tuple) -> bytes:
    result = [oid_tuple[0] * 40 + oid_tuple[1]]
    for component in oid_tuple[2:]:
        if component < 128:
            result.append(component)
        else:
            temp = []
            while component > 0:
                temp.insert(0, (component & 0x7F) | 0x80)
                component >>= 7
            temp[-1] &= 0x7F
            result.extend(temp)
    content = bytes(result)
    return b"\x06" + _asn1_encode_length(len(content)) + content


def _asn1_encode_explicit_tag(tag_num: int, content: bytes) -> bytes:
    return bytes([0xA0 | tag_num]) + _asn1_encode_length(len(content)) + content


def _asn1_encode_implicit_tag(tag_num: int, content: bytes) -> bytes:
    return bytes([0x80 | tag_num]) + _asn1_encode_length(len(content)) + content


class _Pkcs7Encryptor:
    OID_ENVELOPED_DATA = (1, 2, 840, 113549, 1, 7, 3)
    OID_ENCRYPTED_DATA = (1, 2, 840, 113549, 1, 7, 6)
    OID_RSA_ENCRYPTION = (1, 2, 840, 113549, 1, 1, 1)
    OID_DES_EDE3_CBC = (1, 2, 840, 113549, 3, 7)

    def __init__(self, cert_der: bytes):
        self.cert_der = cert_der
        self.certificate = x509.load_der_x509_certificate(cert_der, default_backend())

    @staticmethod
    def _generate_3des_key() -> bytes:
        return secrets.token_bytes(16)

    @staticmethod
    def _expand_key_to_24bytes(key_16: bytes) -> bytes:
        return key_16 + key_16[:8]

    def _3des_encrypt(self, plaintext: bytes, key: bytes) -> bytes:
        key_24 = self._expand_key_to_24bytes(key)
        iv = bytes(8)
        padder = padding.PKCS7(64).padder()
        padded = padder.update(plaintext) + padder.finalize()
        cipher = Cipher(TripleDES(key_24), modes.CBC(iv), backend=default_backend())
        encryptor = cipher.encryptor()
        return encryptor.update(padded) + encryptor.finalize()

    def _rsa_encrypt(self, data: bytes) -> bytes:
        public_key = self.certificate.public_key()
        return public_key.encrypt(data, asym_padding.PKCS1v15())

    def _get_issuer_der(self) -> bytes:
        cert_der = self.cert_der
        pos = 0
        if cert_der[pos] != 0x30:
            raise ValueError("Invalid certificate")
        pos += 1
        if cert_der[pos] & 0x80:
            len_bytes = cert_der[pos] & 0x7F
            pos += 1 + len_bytes
        else:
            pos += 1
        if cert_der[pos] != 0x30:
            raise ValueError("Invalid TBSCertificate")
        pos += 1
        if cert_der[pos] & 0x80:
            len_bytes = cert_der[pos] & 0x7F
            pos += 1 + len_bytes
        else:
            pos += 1
        if cert_der[pos] == 0xA0:
            pos += 1
            if cert_der[pos] & 0x80:
                len_bytes = cert_der[pos] & 0x7F
                length = int.from_bytes(cert_der[pos + 1 : pos + 1 + len_bytes], "big")
                pos += 1 + len_bytes + length
            else:
                length = cert_der[pos]
                pos += 1 + length
        if cert_der[pos] != 0x02:
            raise ValueError("Expected INTEGER for serialNumber")
        pos += 1
        if cert_der[pos] & 0x80:
            len_bytes = cert_der[pos] & 0x7F
            length = int.from_bytes(cert_der[pos + 1 : pos + 1 + len_bytes], "big")
            pos += 1 + len_bytes + length
        else:
            length = cert_der[pos]
            pos += 1 + length
        if cert_der[pos] != 0x30:
            raise ValueError("Expected SEQUENCE for signature algorithm")
        pos += 1
        if cert_der[pos] & 0x80:
            len_bytes = cert_der[pos] & 0x7F
            length = int.from_bytes(cert_der[pos + 1 : pos + 1 + len_bytes], "big")
            pos += 1 + len_bytes + length
        else:
            length = cert_der[pos]
            pos += 1 + length
        issuer_start = pos
        if cert_der[pos] != 0x30:
            raise ValueError("Expected SEQUENCE for issuer")
        pos += 1
        if cert_der[pos] & 0x80:
            len_bytes = cert_der[pos] & 0x7F
            length = int.from_bytes(cert_der[pos + 1 : pos + 1 + len_bytes], "big")
            issuer_end = pos + 1 + len_bytes + length
        else:
            length = cert_der[pos]
            issuer_end = pos + 1 + length
        return cert_der[issuer_start:issuer_end]

    def _build_algorithm_identifier(self, oid: tuple, params: bytes = b"") -> bytes:
        return _asn1_encode_sequence(_asn1_encode_oid(oid) + params)

    def _build_recipient_info(self, encrypted_key: bytes) -> bytes:
        issuer_der = self._get_issuer_der()
        serial_der = _asn1_encode_integer(self.certificate.serial_number)
        issuer_serial = _asn1_encode_sequence(issuer_der + serial_der)
        key_enc_alg = self._build_algorithm_identifier(self.OID_RSA_ENCRYPTION)
        enc_key = _asn1_encode_octet_string(encrypted_key)
        return _asn1_encode_sequence(_asn1_encode_integer(0) + issuer_serial + key_enc_alg + enc_key)

    def _build_encrypted_content_info(self, encrypted_content: bytes) -> bytes:
        content_type = _asn1_encode_oid(self.OID_ENCRYPTED_DATA)
        content_enc_alg = self._build_algorithm_identifier(self.OID_DES_EDE3_CBC)
        enc_content = _asn1_encode_implicit_tag(0, encrypted_content)
        return _asn1_encode_sequence(content_type + content_enc_alg + enc_content)

    def _build_content_info(self, encrypted_key: bytes, encrypted_content: bytes) -> bytes:
        recipient_infos = _asn1_encode_set(self._build_recipient_info(encrypted_key))
        enc_content_info = self._build_encrypted_content_info(encrypted_content)
        enveloped_data = _asn1_encode_sequence(_asn1_encode_integer(0) + recipient_infos + enc_content_info)
        content = _asn1_encode_explicit_tag(0, enveloped_data)
        return _asn1_encode_sequence(_asn1_encode_oid(self.OID_ENVELOPED_DATA) + content)

    def encrypt_to_hex(self, plaintext: bytes) -> str:
        des_key = self._generate_3des_key()
        encrypted_content = self._3des_encrypt(plaintext, des_key)
        encrypted_key = self._rsa_encrypt(des_key)
        return self._build_content_info(encrypted_key, encrypted_content).hex().upper()


class NagadCashInEncryptor:
    def __init__(
        self, cert_der: bytes, pan: str, expiry: str, psn: str, aip: str, token_requester_id: str,
        account_parameter_index: str, luk_cl_md: str, luk_cl_umd: str, pin: str, mpa_id: str = ""
    ):
        self.mpa_id = mpa_id
        self.data_generator = _CashInDataGeneratorV2(
            pan=pan,
            expiry=expiry,
            psn=psn,
            aip=aip,
            token_requester_id=token_requester_id,
            account_parameter_index=account_parameter_index,
            luk_cl_md=luk_cl_md,
            luk_cl_umd=luk_cl_umd,
            pin=pin,
        )
        self.pkcs7_encryptor = _Pkcs7Encryptor(cert_der=cert_der)

    def generate_encrypted_request(
        self, destination_account: str, amount: float, atc: int = 1, unpredictable_number: Optional[str] = None,
        tx_date: Optional[str] = None, tx_type: str = "60"
    ) -> dict:
        atc_str = f"{atc:04d}"
        if tx_date is None:
            tx_date = datetime.now().strftime("%y%m%d")
        track2, un = self.data_generator.generate_track2(
            atc=atc_str, amount=amount, tx_date=tx_date, tx_type=tx_type, un=unpredictable_number
        )
        de55 = self.data_generator.generate_de55(amount=amount, un=un)
        cash_in_data = {
            "destinationAccountNo": destination_account,
            "txData": {
                "de2": self.data_generator.get_pan(),
                "de4": self.data_generator.format_amount(amount),
                "de23": self.data_generator.get_psn(),
                "de14": self.data_generator.get_expiry_yymm(),
                "de35": track2,
                "de48": self.data_generator.get_token_requester_id(),
                "de55": de55,
                "de49": "0050",
            },
        }
        json_str = json.dumps(cash_in_data, ensure_ascii=False, separators=(",", ":"))
        encrypted_hex = self.pkcs7_encryptor.encrypt_to_hex(json_str.encode("utf-8"))
        return {"data": encrypted_hex, "mpaId": self.mpa_id}

    def generate_encrypted_b2b_request(
        self, from_account: str, amount: float, atc: int = 1, unpredictable_number: Optional[str] = None,
        tx_date: Optional[str] = None, tx_type: str = "60", level: int = 1
    ) -> dict:
        atc_str = f"{atc:04d}"
        if tx_date is None:
            tx_date = datetime.now().strftime("%y%m%d")
        track2, un = self.data_generator.generate_track2(
            atc=atc_str, amount=amount, tx_date=tx_date, tx_type=tx_type, un=unpredictable_number
        )
        de55 = self.data_generator.generate_de55(amount=amount, un=un)
        b2b_data = {
            "destinationAccountNo": None,
            "fromAccountNo": from_account,
            "level": level,
            "txData": {
                "de2": self.data_generator.get_pan(),
                "de4": self.data_generator.format_amount(amount),
                "de23": self.data_generator.get_psn(),
                "de14": self.data_generator.get_expiry_yymm(),
                "de35": track2,
                "de48": self.data_generator.get_token_requester_id(),
                "de55": de55,
                "de49": "0050",
            },
        }
        json_str = json.dumps(b2b_data, ensure_ascii=False, separators=(",", ":"))
        encrypted_hex = self.pkcs7_encryptor.encrypt_to_hex(json_str.encode("utf-8"))
        return {"data": encrypted_hex, "mpaId": self.mpa_id}

