// How scripts/art.py flattens an SVG before reading it: styles become attributes,
// shapes become paths, and group transforms are applied to the path data, so all that
// is left to read is paths with a fill and, now and then, a clip path.
export default {
  multipass: true,
  plugins: [
    "convertStyleToAttrs",
    {
      name: "preset-default",
      params: {
        overrides: {
          cleanupIds: false,
          removeUselessDefs: false,
          convertPathData: { floatPrecision: 2 },
        },
      },
    },
    { name: "convertShapeToPath", params: { convertArcs: true } },
  ],
};
