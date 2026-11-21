# Pictures

Most pictures in Kid Reels are emoji from the phone's own font, which cost nothing in
the APK. Where no emoji shows a thing well (an elbow, a knee), or at all (bhindi, lauki),
the picture is an SVG here, turned into Kotlin path data by `scripts/art.py` and drawn by
the app itself (`ui/.../ui/art/`). A word uses one by its file name: `art:okra`.

```sh
python3 scripts/art.py   # after adding or changing an SVG; writes ui/.../ui/art/Arts.kt
```

The script flattens each SVG with svgo (shapes to paths, transforms applied) and reads
only filled paths and clip paths. A gradient, a stroke or a leftover transform stops it
with the file's name, so draw with fills, or flatten those in the source first. So do
three things it would otherwise draw subtly wrong: a clip inside another clip (each path
keeps one clip, so the inner would replace the outer), a clip in `objectBoundingBox`
units, and opacity on a group of several shapes (fading each shape on its own shows
where they overlap). Put the opacity on each shape instead.

## Where each picture comes from

| Folder   | Pictures                                                                                                                                   | Source and licence                                                                                                                                                                                                                                     |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `noto/`  | peas (U+1FADB), ginger (U+1FADA), beetroot (U+1FADC), kidney beans (U+1FAD8)                                                               | [Noto Emoji](https://github.com/googlefonts/noto-emoji) `2D/svg/`, © 2013 Google LLC. Licence text in `noto/LICENSE` (SIL Open Font License 1.1); Noto's README puts its images under Apache 2.0. Either way: keep the notice, credit it (About does). |
| `drawn/` | okra, bottle gourd, bitter gourd, cauliflower, radish, spinach, pumpkin, the child in `body.svg`, the plant in `plant.svg`                 | Drawn for Kid Reels, MIT like the rest of the app.                                                                                                                                                                                                     |
| `drawn/` | Gappu, the app's baby elephant: `gappu-happy` (waving), `-cheer`, `-surprised`, `-sneeze`, `-yawn`, `-asleep`, `-sad`, `-eating`, `-dance` | Generated with Google Gemini's image model for Kid Reels on 2026-11-21, from our own prompt for an original character, then traced to SVG with vtracer. MIT like the rest of the app.                                                                  |
| `drawn/` | Gappu's friends, the silly animals: `silly-cow`, `-lion`, `-pig`, `-mouse`, `-cat`, `-dog`                                                 | Generated with Google Gemini's image model in the same chat as Gappu, on 2026-11-21, then traced to SVG with vtracer. MIT like the rest of the app.                                                                                                    |

The Noto pictures are emoji newer than Emoji 13.1, which Android 12's font cannot draw,
in the same style as the emoji around them. Nothing freely licensed showed Indian
vegetables (bhindi, lauki, karela) or a whole child or plant with every part where it really is,
so those are drawn here in the same flat style.

## Adding a picture

- Licences that fit: public domain, CC0, MIT, Apache 2.0, OFL, CC BY (with a credit on
  About). Not CC BY-NC or ND, and not sites that need a paid plan or an attribution
  link on every screen.
- Add a row above with where it came from, then run the script and add the word.
- Keep it small: a few KB of path data each. A detailed illustration belongs in a
  simpler redraw, not in the APK.

Gappu and his friends are the one exception to keeping it small: about 20 to 65 KB of
SVG a drawing, with the grey ground shadow and outline the tracer kept. Gappu's 9 poses
took `Arts.kt` from 25 KB to 270 KB of path data, and the six silly animals to 470 KB.
Gappu is the character a child comes back to, on screen at every celebration and at the
end of each day, and the animals are his friends in the same style, so a flat redraw
would lose what makes them likeable; a vegetable or a knee is better small. A drawing
whose colours run together when traced (the puppy, the pig's tummy) is traced again with
finer colour layers (`layer_difference` 18 rather than 28). The source sheets the poses were cut
from stay out of the repo (they are only the generator's output, before tracing).
