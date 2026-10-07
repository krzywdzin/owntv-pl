#!/usr/bin/env python3
"""Minimal activation API for development and early deployments."""

from __future__ import annotations

import json
import os
import threading
import time
from collections import defaultdict, deque
from datetime import datetime, timezone
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import unquote, urlparse

ROOT = Path(__file__).resolve().parent
DATA_FILE = Path(os.getenv("ACTIVATION_DATA_FILE", ROOT / "activations.json"))
BINDINGS_FILE = Path(os.getenv("ACTIVATION_BINDINGS_FILE", ROOT / "bindings.json"))
HOST = os.getenv("ACTIVATION_HOST", "0.0.0.0")
PORT = int(os.getenv("ACTIVATION_PORT", "8787"))
MAX_ATTEMPTS = int(os.getenv("ACTIVATION_RATE_LIMIT", "10"))
WINDOW_SECONDS = int(os.getenv("ACTIVATION_RATE_WINDOW", "60"))

_lock = threading.Lock()
_attempts: dict[str, deque[float]] = defaultdict(deque)


def _load_json(path: Path, default):
    if not path.exists():
        return default
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def _save_json(path: Path, payload) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    tmp = path.with_suffix(path.suffix + ".tmp")
    with tmp.open("w", encoding="utf-8") as handle:
        json.dump(payload, handle, ensure_ascii=False, indent=2, sort_keys=True)
    tmp.replace(path)


def _normalize_code(value: str) -> str:
    return "".join(ch for ch in value.upper() if ch.isalnum())


def _expired(value: str | None) -> bool:
    if not value:
        return False
    normalized = value.replace("Z", "+00:00")
    try:
        moment = datetime.fromisoformat(normalized)
    except ValueError:
        return False
    if moment.tzinfo is None:
        moment = moment.replace(tzinfo=timezone.utc)
    return moment <= datetime.now(timezone.utc)


def _rate_limited(key: str) -> bool:
    now = time.monotonic()
    bucket = _attempts[key]
    while bucket and now - bucket[0] > WINDOW_SECONDS:
        bucket.popleft()
    if len(bucket) >= MAX_ATTEMPTS:
        return True
    bucket.append(now)
    return False


class ActivationHandler(BaseHTTPRequestHandler):
    server_version = "ActivationService/1"

    def log_message(self, fmt, *args):
        print("%s - %s" % (self.address_string(), fmt % args))

    def _json(self, status: HTTPStatus, payload: dict) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status.value)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        if not parsed.path.startswith("/activate/"):
            self._json(HTTPStatus.NOT_FOUND, {"ok": False, "error": "not_found"})
            return

        device_id = (self.headers.get("X-Device-ID") or "").strip()
        if not device_id:
            self._json(HTTPStatus.BAD_REQUEST, {"ok": False, "error": "missing_device_id"})
            return

        code = _normalize_code(unquote(parsed.path.removeprefix("/activate/")))
        rate_key = f"{self.client_address[0]}:{device_id}"

        with _lock:
            if _rate_limited(rate_key):
                self._json(HTTPStatus.TOO_MANY_REQUESTS, {"ok": False, "error": "rate_limited"})
                return

            activations = _load_json(DATA_FILE, {})
            entry = activations.get(code)
            if not isinstance(entry, dict):
                self._json(HTTPStatus.NOT_FOUND, {"ok": False, "error": "bad_code"})
                return

            if _expired(entry.get("expires")):
                self._json(HTTPStatus.GONE, {"ok": False, "error": "code_expired"})
                return

            bindings = _load_json(BINDINGS_FILE, {})
            bound_to = bindings.get(code)
            if bound_to is None:
                bindings[code] = device_id
                _save_json(BINDINGS_FILE, bindings)
            elif bound_to != device_id:
                self._json(HTTPStatus.CONFLICT, {"ok": False, "error": "device_bound"})
                return

            self._json(
                HTTPStatus.OK,
                {
                    "ok": True,
                    "server": entry["server"],
                    "port": entry.get("port"),
                    "https": bool(entry.get("https", False)),
                    "username": entry["username"],
                    "password": entry["password"],
                    "expires": entry.get("expires"),
                    "support_phone": entry.get("support_phone"),
                },
            )


def main() -> None:
    if not DATA_FILE.exists():
        raise SystemExit(
            f"Missing {DATA_FILE}. Copy activations.example.json to activations.json and edit it first."
        )
    server = ThreadingHTTPServer((HOST, PORT), ActivationHandler)
    print(f"Activation service listening on http://{HOST}:{PORT}")
    server.serve_forever()


if __name__ == "__main__":
    main()
