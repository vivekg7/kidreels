#!/usr/bin/env python3
"""Turn the SVGs in art/ into Kotlin path data the app draws itself.

    python3 scripts/art.py

Each SVG is flattened by svgo (scripts/svgo.config.mjs: shapes become paths and
transforms are applied), then its paths are read with their fill, opacity, fill rule
and clip path, and written to ui/.../ui/art/Arts.kt as one `Art` per file, named after
the file: art/drawn/bottle-gourd.svg becomes `Arts.bottleGourd`, key "bottle-gourd".

No image library is needed and every platform draws the same thing. Only what the art
here uses is supported; anything else (a gradient, a leftover transform, a stroke, a clip
inside a clip, a see-through group) stops the script with the file and element, rather
than drawing something quietly wrong.
"""

import re
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCES = [ROOT / "art" / "noto", ROOT / "art" / "drawn"]
OUT = ROOT / "ui/src/commonMain/kotlin/com/crylo/kidreels/ui/art/Arts.kt"
SVGO = "svgo@3.3.2"
NS = "{http://www.w3.org/2000/svg}"
XLINK = "{http://www.w3.org/1999/xlink}href"

NAMED = {"white": "#ffffff", "black": "#000000", "red": "#ff0000", "none": "none"}


def fail(file: Path, msg: str):
    sys.exit(f"{file.relative_to(ROOT)}: {msg}")


def colour(file: Path, value: str) -> str | None:
    value = NAMED.get(value.strip().lower(), value.strip().lower())
    if value == "none":
        return None
    if value.startswith("url("):
        fail(file, f"gradients are not supported ({value})")
    if re.fullmatch(r"#[0-9a-f]{3}", value):
        value = "#" + "".join(c * 2 for c in value[1:])
    if not re.fullmatch(r"#[0-9a-f]{6}", value):
        fail(file, f"unknown colour {value!r}")
    return value[1:].upper()


def shape_path(el: ET.Element) -> str | None:
    """Path data for an element: a path's own, or a circle, ellipse or rect svgo left as a shape."""
    tag = el.tag.removeprefix(NS)
    g = lambda k, d="0": float(el.get(k, d))
    if tag == "circle":
        cx, cy, r = g("cx"), g("cy"), g("r")
        return f"M{cx - r:g},{cy:g}a{r:g},{r:g} 0 1,0 {2 * r:g},0a{r:g},{r:g} 0 1,0 {-2 * r:g},0z"
    if tag == "ellipse":
        cx, cy, rx, ry = g("cx"), g("cy"), g("rx"), g("ry")
        return f"M{cx - rx:g},{cy:g}a{rx:g},{ry:g} 0 1,0 {2 * rx:g},0a{rx:g},{ry:g} 0 1,0 {-2 * rx:g},0z"
    if tag == "rect":
        x, y, w, h = g("x"), g("y"), g("width"), g("height")
        r = min(g("rx", el.get("ry", "0")), w / 2, h / 2)
        if r == 0:
            return f"M{x:g},{y:g}h{w:g}v{h:g}h{-w:g}z"
        return (
            f"M{x + r:g},{y:g}h{w - 2 * r:g}a{r:g},{r:g} 0 0,1 {r:g},{r:g}v{h - 2 * r:g}"
            f"a{r:g},{r:g} 0 0,1 {-r:g},{r:g}h{-(w - 2 * r):g}a{r:g},{r:g} 0 0,1 {-r:g},{-r:g}"
            f"v{-(h - 2 * r):g}a{r:g},{r:g} 0 0,1 {r:g},{-r:g}z"
        )
    if tag == "path":
        return el.get("d")
    return None


def read(file: Path) -> tuple[float, float, list[dict]]:
    with tempfile.TemporaryDirectory() as tmp:
        flat = Path(tmp) / file.name
        subprocess.run(
            [
                "npx",
                "-y",
                SVGO,
                "--config",
                str(ROOT / "scripts/svgo.config.mjs"),
                "-i",
                str(file),
                "-o",
                str(flat),
                "--quiet",
            ],
            check=True,
        )
        root = ET.parse(flat).getroot()
    vb = [float(v) for v in root.get("viewBox", "").replace(",", " ").split()]
    if len(vb) != 4 or vb[0] or vb[1]:
        fail(file, f"needs a viewBox starting at 0 0, has {root.get('viewBox')!r}")
    by_id = {el.get("id"): el for el in root.iter() if el.get("id")}
    clips: dict[str, str] = {}

    def clip_data(ref: str) -> str:
        cid = re.fullmatch(r"url\(#(.+)\)", ref.strip())
        if not cid:
            fail(file, f"odd clip-path {ref!r}")
        cid = cid.group(1)
        if cid not in clips:
            box = by_id[cid]
            units = box.get("clipPathUnits", "userSpaceOnUse")
            if units != "userSpaceOnUse":
                fail(file, f"clip {cid!r}: clipPathUnits={units!r} is not supported")
            parts = []
            for child in box.iter():
                used = by_id.get(child.get(XLINK, child.get("href", "#")).lstrip("#"))
                for el in (child, used):
                    if el is not None and el.get("transform"):
                        tag = el.tag.removeprefix(NS)
                        fail(file, f"clip {cid!r}: a transform was left on <{tag}>")
                if child.tag == NS + "use":
                    child = by_id[child.get(XLINK, child.get("href", "#")).lstrip("#")]
                if child is box:
                    continue
                d = shape_path(child)
                if d:
                    parts.append(d)
            clips[cid] = " ".join(parts)
        return clips[cid]

    out: list[dict] = []

    def walk(el: ET.Element, fill: str | None, alpha: float, clip: str | None, evenodd: bool):
        tag = el.tag.removeprefix(NS)
        if tag in ("defs", "clipPath", "title", "desc", "metadata", "style"):
            return
        if el.get("transform"):
            fail(file, f"a transform was left on <{tag}>: flatten it in the source")
        if el.get("stroke") not in (None, "none"):
            fail(file, f"strokes are not supported (<{tag}>)")
        if el.get("fill") is not None:
            fill = colour(file, el.get("fill"))
        opacity = float(el.get("opacity", "1"))
        if opacity != 1 and tag in ("svg", "g") and sum(1 for c in el.iter() if shape_path(c)) > 1:
            # A see-through group is drawn whole, then faded: fading each shape instead
            # shows where they overlap. Put the opacity on each shape in the source.
            fail(file, f"opacity on a <{tag}> of several shapes is not supported")
        alpha *= opacity * float(el.get("fill-opacity", "1"))
        if el.get("clip-path"):
            if clip is not None:
                # Each path carries one clip, so an inner one would replace the outer.
                fail(file, f"a clip inside a clip is not supported (<{tag}>)")
            clip = clip_data(el.get("clip-path"))
        if el.get("fill-rule") is not None:
            evenodd = el.get("fill-rule") == "evenodd"
        if tag == "use":
            target = by_id[el.get(XLINK, el.get("href", "#")).lstrip("#")]
            if float(el.get("x", "0")) or float(el.get("y", "0")):
                fail(file, "<use> with an offset is not supported")
            walk(target, fill, alpha, clip, evenodd)
            return
        if tag in ("svg", "g"):
            for child in el:
                walk(child, fill, alpha, clip, evenodd)
            return
        d = shape_path(el)
        if d is None:
            fail(file, f"<{tag}> is not supported")
        if fill is None or alpha == 0:
            return
        out.append(
            {
                "fill": fill,
                "alpha": alpha,
                "d": " ".join(d.split()),
                "evenodd": evenodd,
                "clip": clip,
                "id": el.get("id"),
            }
        )

    walk(root, "000000", 1.0, None, False)
    return vb[2], vb[3], out


def camel(stem: str) -> str:
    head, *rest = stem.split("-")
    return head + "".join(p.capitalize() for p in rest)


def kotlin_string(s: str) -> str:
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'


def main():
    files = sorted(f for d in SOURCES for f in d.glob("*.svg"))
    names = [f.stem for f in files]
    if len(set(names)) != len(names):
        sys.exit(f"two SVGs share a name: {names}")
    lines = [
        "package com.crylo.kidreels.ui.art",
        "",
        "// Generated by scripts/art.py from the SVGs in art/. Do not edit: change the SVG and",
        "// run the script again. Sources and licences are in art/README.md.",
        "",
        '@Suppress("MaxLineLength")',
        "object Arts {",
    ]
    for file in files:
        w, h, parts = read(file)
        lines.append(f"    val {camel(file.stem)} = Art(")
        lines.append(f"        {w:g}f, {h:g}f,")
        lines.append("        listOf(")
        for p in parts:
            argb = f"0x{round(p['alpha'] * 255):02X}{p['fill']}"
            extra = ""
            if p["evenodd"]:
                extra += ", evenOdd = true"
            if p["clip"]:
                extra += f", clip = {kotlin_string(p['clip'])}"
            lines.append(f"            ArtPart({argb}, {kotlin_string(p['d'])}{extra}),")
        lines.append("        ),")
        lines.append("    )")
        lines.append("")
    lines.append('    /** Each picture by its file name, the key a word uses as "art:<name>". */')
    lines.append("    val byName: Map<String, Art> = mapOf(")
    for file in files:
        lines.append(f"        {kotlin_string(file.stem)} to {camel(file.stem)},")
    lines.append("    )")
    lines.append("}")
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(lines) + "\n")
    print(f"{len(files)} pictures -> {OUT.relative_to(ROOT)} ({OUT.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()
