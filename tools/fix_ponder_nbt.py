"""Patch CU ponder schematics with safe NBT string replacement."""
import gzip
import os
import struct

import nbtlib

PONDER_DIR = os.path.join(
    os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "createvoidway", "ponder"
)

REPLACEMENTS = [
    ("createutilities:void_motor", "createvoidway:void_motor_output"),
    ("createutilities:void_chest", "createvoidway:void_chest_output"),
    ("createutilities:void_tank", "createvoidway:void_tank_output"),
    ("createutilities:void_battery", "createvoidway:void_battery_output"),
]

CU_FILES = ["gearcube", "lshaped_gearbox", "void_battery", "void_chest", "void_motor", "void_tank"]


def replace_nbt_strings(data: bytes, old: str, new: str) -> tuple[bytes, int]:
    old_b = old.encode("utf-8")
    new_b = new.encode("utf-8")
    pattern = struct.pack(">H", len(old_b)) + old_b
    replacement = struct.pack(">H", len(new_b)) + new_b
    count = data.count(pattern)
    if count:
        data = data.replace(pattern, replacement)
    return data, count


def patch_file(path: str) -> int:
    raw = gzip.decompress(open(path, "rb").read())
    total = 0
    for old, new in REPLACEMENTS:
        raw, n = replace_nbt_strings(raw, old, new)
        total += n
    with open(path, "wb") as f:
        f.write(gzip.compress(raw))
    return total


def verify_all() -> None:
    for fn in sorted(os.listdir(PONDER_DIR)):
        if not fn.endswith(".nbt"):
            continue
        path = os.path.join(PONDER_DIR, fn)
        raw = gzip.decompress(open(path, "rb").read())
        if b"createutilities:" in raw:
            raise SystemExit(f"FAIL {fn}: still has createutilities")
        # Files with only palette (no custom BE tags) must parse
        if fn in ("gearcube.nbt", "lshaped_gearbox.nbt", "void_motor_io.nbt", "void_node_terminal.nbt",
                  "void_portal.nbt", "void_teleport.nbt", "void_transfer_fluid.nbt"):
            try:
                nbtlib.load(path)
            except Exception as exc:
                raise SystemExit(f"FAIL {fn}: nbtlib {exc}") from exc
        print(f"OK {fn} ({len(raw)} bytes decompressed)")


def rebuild_terminal() -> None:
    import subprocess
    import sys
    script = os.path.join(os.path.dirname(__file__), "gen_terminal_ponder.py")
    subprocess.check_call([sys.executable, script])


def main() -> None:
    for name in CU_FILES:
        path = os.path.join(PONDER_DIR, f"{name}.nbt")
        n = patch_file(path)
        print(f"patched {name}.nbt ({n} ids)")
    rebuild_terminal()
    verify_all()


if __name__ == "__main__":
    main()
