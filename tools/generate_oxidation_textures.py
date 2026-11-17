#!/usr/bin/env python3
"""Generate Create oxidation textures from Minecraft's four copper palettes."""

from __future__ import annotations

import argparse
import colorsys
import json
import shutil
from pathlib import Path

from PIL import Image


STAGES = ("exposed", "weathered", "oxidized")
TEXTURE_HINTS = (
    "copper",
    "fluid_tank",
    "encased_pipe",
    "glass_fluid_pipe",
    "pipes",
    "pump",
    "smart_pipe",
    "fluid_valve",
    "valve_open",
    "valve_closed",
    "valve_handle_copper",
    "hose",
    "item_drain",
    "spout",
    "portable_fluid_interface",
    "engine",
    "boiler_gauge",
)

EXCLUDED_PREFIXES = ("copper/",)
EXCLUDED_NAMES = {
    "copper_backtank.png",
    "whistle.png",
    "whistle_particle.png",
    "cam_linkage.png",
    "hose_pulley_coil_scroll.png",
    "hose_pulley_coil.png",
}
INNER_PIPE_FROM = (0x3D, 0x16, 0x1E)
INNER_PIPE_TO = (0x1A, 0x39, 0x2D)

PARTIAL_MODELS = {
    "steam_engine/gauge": (
        "create:block/steam_engine/gauge",
        {"0": "boiler_gauge", "particle": "boiler_gauge"},
    ),
    "steam_engine/gauge_dial": (
        "create:block/steam_engine/gauge_dial",
        {"0": "boiler_gauge", "particle": "boiler_gauge"},
    ),
    "portable_fluid_interface/block_top": (
        "create:block/portable_fluid_interface/block_top",
        {"0": "portable_fluid_interface", "particle": "portable_fluid_interface"},
    ),
    "portable_fluid_interface/block_middle": (
        "create:block/portable_fluid_interface/block_middle",
        {"2": "portable_fluid_interface", "particle": "portable_fluid_interface"},
    ),
    "portable_fluid_interface/block_middle_powered": (
        "create:block/portable_fluid_interface/block_middle_powered",
        {"2": "portable_fluid_interface", "particle": "portable_fluid_interface"},
    ),
    "fluid_valve/pointer": (
        "create:block/fluid_valve/pointer",
        {"2": "fluid_valve", "particle": "fluid_valve"},
    ),
    "valve_handle": (
        "create:block/valve_handle",
        {
            "3": "valve_handle/valve_handle_copper",
            "particle": "valve_handle/valve_handle_copper",
        },
    ),
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--create-root", type=Path, required=True)
    parser.add_argument("--palette-root", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    return parser.parse_args()


def load_rgba(path: Path) -> Image.Image:
    with Image.open(path) as image:
        return image.convert("RGBA")


def copper_colored(red: int, green: int, blue: int, alpha: int) -> bool:
    if alpha == 0:
        return False
    hue, saturation, value = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)
    return (hue <= 0.145 or hue >= 0.97) and saturation >= 0.16 and value >= 0.12


def transfer_pixel(
    source: tuple[int, int, int, int],
    base: tuple[int, int, int, int],
    target: tuple[int, int, int, int],
) -> tuple[int, int, int, int]:
    red, green, blue, alpha = source
    if alpha != 0 and (red, green, blue) == INNER_PIPE_FROM:
        return *INNER_PIPE_TO, alpha
    if not copper_colored(red, green, blue, alpha):
        return source

    source_hsv = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)
    base_hsv = colorsys.rgb_to_hsv(base[0] / 255, base[1] / 255, base[2] / 255)
    target_hsv = colorsys.rgb_to_hsv(target[0] / 255, target[1] / 255, target[2] / 255)

    value = max(0.0, min(1.0, target_hsv[2] + (source_hsv[2] - base_hsv[2]) * 0.72))
    saturation = max(
        0.0,
        min(1.0, target_hsv[1] + (source_hsv[1] - base_hsv[1]) * 0.35),
    )
    out_red, out_green, out_blue = colorsys.hsv_to_rgb(target_hsv[0], saturation, value)
    return round(out_red * 255), round(out_green * 255), round(out_blue * 255), alpha


def oxidize(source: Image.Image, base: Image.Image, target: Image.Image) -> tuple[Image.Image, int]:
    output = Image.new("RGBA", source.size)
    source_pixels = source.load()
    base_pixels = base.load()
    target_pixels = target.load()
    output_pixels = output.load()
    changed = 0

    for y in range(source.height):
        for x in range(source.width):
            source_pixel = source_pixels[x, y]
            mapped = transfer_pixel(
                source_pixel,
                base_pixels[x % base.width, y % base.height],
                target_pixels[x % target.width, y % target.height],
            )
            output_pixels[x, y] = mapped
            changed += mapped != source_pixel
    return output, changed


def candidate(path: Path, texture_root: Path) -> bool:
    relative = path.relative_to(texture_root).as_posix().lower()
    if relative.startswith(EXCLUDED_PREFIXES) or path.name.lower() in EXCLUDED_NAMES:
        return False
    return any(hint in relative for hint in TEXTURE_HINTS)


def generate_partial_models(output: Path) -> None:
    model_root = output / "assets" / "createoxidized" / "models" / "block"
    if model_root.exists():
        shutil.rmtree(model_root)
    for stage in STAGES:
        for relative, (parent, texture_paths) in PARTIAL_MODELS.items():
            textures = {
                key: f"createoxidized:block/{stage}/{path}"
                for key, path in texture_paths.items()
            }
            destination = model_root / stage / f"{relative}.json"
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_text(
                json.dumps({"parent": parent, "textures": textures}, indent=2) + "\n",
                encoding="utf-8",
            )


def main() -> None:
    args = parse_args()
    texture_root = args.create_root / "assets" / "create" / "textures" / "block"
    if not texture_root.is_dir():
        raise SystemExit(f"Create block textures were not found at {texture_root}")

    palette_paths = {
        "unaffected": args.palette_root / "copper_block.png",
        "exposed": args.palette_root / "exposed_copper.png",
        "weathered": args.palette_root / "weathered_copper.png",
        "oxidized": args.palette_root / "oxidized_copper.png",
    }
    missing = [str(path) for path in palette_paths.values() if not path.is_file()]
    if missing:
        raise SystemExit("Missing palette textures: " + ", ".join(missing))

    palettes = {name: load_rgba(path) for name, path in palette_paths.items()}
    output_texture_root = args.output / "assets" / "createoxidized" / "textures" / "block"
    if output_texture_root.exists():
        shutil.rmtree(output_texture_root)
    output_texture_root.mkdir(parents=True, exist_ok=True)
    manifest: list[str] = []

    for source_path in sorted(texture_root.rglob("*.png")):
        if not candidate(source_path, texture_root):
            continue
        relative = source_path.relative_to(texture_root)
        source = load_rgba(source_path)
        generated: dict[str, Image.Image] = {}
        changed_pixels = 0
        for stage in STAGES:
            image, changed = oxidize(source, palettes["unaffected"], palettes[stage])
            generated[stage] = image
            changed_pixels = max(changed_pixels, changed)
        if changed_pixels == 0:
            continue

        manifest.append("create:block/" + relative.with_suffix("").as_posix())
        for stage, image in generated.items():
            destination = output_texture_root / stage / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            image.save(destination)
            metadata = source_path.with_name(source_path.name + ".mcmeta")
            if metadata.is_file():
                shutil.copy2(metadata, destination.with_name(destination.name + ".mcmeta"))

    manifest_path = args.output / "assets" / "createoxidized" / "oxidizable_textures.txt"
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text("\n".join(manifest) + "\n", encoding="utf-8")
    generate_partial_models(args.output)
    print(f"Generated {len(manifest)} texture sets in {args.output}")


if __name__ == "__main__":
    main()

