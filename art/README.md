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
with the file's name, so draw with fills, or flatten those in the source first.

## Where each picture comes from

| Folder   | Pictures                                                                                         | Source and licence                                                                                                                                                                                                                                     |
| -------- | ------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `noto/`  | peas (U+1FADB), ginger (U+1FADA), beetroot (U+1FADC), kidney beans (U+1FAD8)                     | [Noto Emoji](https://github.com/googlefonts/noto-emoji) `2D/svg/`, © 2013 Google LLC. Licence text in `noto/LICENSE` (SIL Open Font License 1.1); Noto's README puts its images under Apache 2.0. Either way: keep the notice, credit it (About does). |
| `drawn/` | okra, bottle gourd, bitter gourd, cauliflower, radish, spinach, pumpkin, the child in `body.svg` | Drawn for Kid Reels, MIT like the rest of the app.                                                                                                                                                                                                     |

The Noto pictures are emoji newer than Emoji 13.1, which Android 12's font cannot draw,
in the same style as the emoji around them. Nothing freely licensed showed Indian
vegetables (bhindi, lauki, karela) or a whole child with every part where it really is,
so those are drawn here in the same flat style.

## Adding a picture

- Licences that fit: public domain, CC0, MIT, Apache 2.0, OFL, CC BY (with a credit on
  About). Not CC BY-NC or ND, and not sites that need a paid plan or an attribution
  link on every screen.
- Add a row above with where it came from, then run the script and add the word.
- Keep it small: a few KB of path data each. A detailed illustration belongs in a
  simpler redraw, not in the APK.
