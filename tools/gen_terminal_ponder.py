"""Generate void_node_terminal ponder schematic (5x3x5 multiblock)."""
import os

import nbtlib
from nbtlib import Compound, File, Int, List, String

PONDER_DIR = os.path.join(
    os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "createvoidway", "ponder"
)


def palette_entry(name: str, **props: str) -> Compound:
    entry = Compound({"Name": String(name)})
    if props:
        entry["Properties"] = Compound({k: String(v) for k, v in props.items()})
    return entry


def block(pos: tuple[int, int, int], state: int) -> Compound:
    return Compound({"pos": List[Int]([Int(p) for p in pos]), "state": Int(state)})


def main() -> None:
    P_CONCRETE = 0
    P_SNOW = 1
    P_SHAFT = 2
    P_TERMINAL = 3
    P_TERMINAL_TOP = 4

    palette = List[Compound]([
        palette_entry("minecraft:white_concrete"),
        palette_entry("minecraft:snow_block"),
        palette_entry("create:shaft", axis="y", waterlogged="false"),
        palette_entry("createvoidway:void_node_terminal", facing="north", waterlogged="false"),
        palette_entry("createvoidway:void_node_terminal_top", facing="north"),
    ])

    blocks = List[Compound]([])
    for x in range(5):
        for z in range(5):
            blocks.append(block((x, 0, z), P_CONCRETE if (x + z) % 2 == 0 else P_SNOW))

    # Primary terminal (center)
    blocks.append(block((2, 0, 2), P_SHAFT))
    blocks.append(block((2, 1, 2), P_TERMINAL))
    blocks.append(block((2, 2, 2), P_TERMINAL_TOP))

    # Paired terminal on the same frequency (right)
    blocks.append(block((4, 0, 2), P_SHAFT))
    blocks.append(block((4, 1, 2), P_TERMINAL))
    blocks.append(block((4, 2, 2), P_TERMINAL_TOP))

    nbt = File({
        "DataVersion": Int(3955),
        "size": List[Int]([Int(5), Int(3), Int(5)]),
        "palette": palette,
        "blocks": blocks,
        "entities": List[Compound]([]),
    })

    out = os.path.join(PONDER_DIR, "void_node_terminal.nbt")
    nbt.save(out, gzipped=True)
    print(f"wrote {out} ({len(blocks)} blocks, size 5x3x5)")


if __name__ == "__main__":
    main()
