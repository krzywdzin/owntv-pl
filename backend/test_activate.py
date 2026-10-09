from __future__ import annotations

import json
import tempfile
import threading
import unittest
import urllib.error
import urllib.request
from pathlib import Path
from unittest import mock

import activate


class ActivationBackendTest(unittest.TestCase):
    def setUp(self) -> None:
        activate._attempts.clear()

    def test_normalize_code(self) -> None:
        self.assertEqual("AB12CD34", activate._normalize_code("ab-12 cd_34"))

    def test_expiry_parser(self) -> None:
        self.assertTrue(activate._expired("2000-01-01"))
        self.assertFalse(activate._expired("2099-01-01"))
        self.assertFalse(activate._expired(None))
        self.assertFalse(activate._expired("not-a-date"))

    def test_rate_limit_is_per_key(self) -> None:
        with mock.patch.object(activate, "MAX_ATTEMPTS", 2):
            self.assertFalse(activate._rate_limited("203.0.113.10"))
            self.assertFalse(activate._rate_limited("203.0.113.10"))
            self.assertTrue(activate._rate_limited("203.0.113.10"))
            self.assertFalse(activate._rate_limited("203.0.113.11"))

    def test_http_contract_and_device_binding(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            data = root / "activations.json"
            bindings = root / "bindings.json"
            data.write_text(
                json.dumps(
                    {
                        "DEMO2026": {
                            "server": "example.test",
                            "port": 443,
                            "https": True,
                            "username": "test_user",
                            "password": "test_pass",
                            "expires": "2099-01-01",
                            "support_phone": "+48123456789",
                        }
                    }
                ),
                encoding="utf-8",
            )

            with (
                mock.patch.object(activate, "DATA_FILE", data),
                mock.patch.object(activate, "BINDINGS_FILE", bindings),
                mock.patch.object(activate, "MAX_ATTEMPTS", 50),
            ):
                server = activate.ThreadingHTTPServer(("127.0.0.1", 0), activate.ActivationHandler)
                thread = threading.Thread(target=server.serve_forever, daemon=True)
                thread.start()
                base = f"http://127.0.0.1:{server.server_address[1]}"
                try:
                    status, body = self._get(base + "/activate/DEMO2026", "device-a")
                    self.assertEqual(200, status)
                    self.assertTrue(body["ok"])
                    self.assertEqual("test_user", body["username"])
                    self.assertEqual("+48123456789", body["support_phone"])

                    status, _ = self._get(base + "/activate/DEMO2026", "device-a")
                    self.assertEqual(200, status)

                    status, body = self._get(base + "/activate/DEMO2026", "device-b")
                    self.assertEqual(409, status)
                    self.assertEqual("device_bound", body["error"])

                    status, body = self._get(base + "/activate/SHORT", "device-a")
                    self.assertEqual(404, status)
                    self.assertEqual("bad_code", body["error"])
                finally:
                    server.shutdown()
                    server.server_close()
                    thread.join(timeout=2)

    @staticmethod
    def _get(url: str, device_id: str) -> tuple[int, dict]:
        request = urllib.request.Request(url, headers={"X-Device-ID": device_id})
        try:
            with urllib.request.urlopen(request, timeout=3) as response:
                return response.status, json.loads(response.read())
        except urllib.error.HTTPError as error:
            return error.code, json.loads(error.read())


if __name__ == "__main__":
    unittest.main()
