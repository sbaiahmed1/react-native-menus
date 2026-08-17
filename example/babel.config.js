/**
 * Note: this deliberately does NOT use `react-native-builder-bob/babel-config`.
 *
 * Bob's `getConfig` adds a Babel `overrides` entry with an `include` path pattern to apply its
 * preset to the library source. `@expo/metro-config`'s transformer calls `loadPartialConfigSync`
 * with no filename when computing its transform cache key, and Babel rejects any config
 * containing a path/RegExp pattern in that case ("Configuration contains string/RegExp pattern,
 * but no filename was passed to Babel"), which stops Metro from starting at all.
 *
 * `babel-preset-expo` already handles the library's TypeScript/JSX source, and resolving
 * `react-native-menus` to `../src` is done by Metro in `metro.config.js` (via
 * `react-native-monorepo-config`, which sets `extraNodeModules` and a `source`-condition
 * `resolveRequest`), not by Babel. So dropping the override costs nothing.
 */
module.exports = {
  presets: ['babel-preset-expo'],
};
