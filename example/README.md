# react-native-menus example

An [Expo](https://docs.expo.dev/) app that exercises every `MenuView` feature: theming, SF
Symbols, subtitles, destructive items, the Android tooltip/dialog display modes, and the
imperative `open()` / `close()` ref methods.

> **This app cannot run in Expo Go.** `react-native-menus` ships custom Fabric native code, so it
> needs a development build. The commands below produce one.

## Running

From the **repository root** (the example is a Yarn workspace):

```sh
yarn                  # install dependencies
yarn example ios      # build and run on an iOS simulator
yarn example android  # build and run on an Android emulator
```

Each of those runs `expo prebuild` first, so the native projects are generated for you.

To start only the dev server against an already-installed build:

```sh
yarn example start
```

## Continuous native generation

`example/ios` and `example/android` are **not** checked into git. They are generated from
`example/app.json` by [`expo prebuild`](https://docs.expo.dev/workflow/prebuild/) and are safe to
delete at any time.

Edit native configuration through `example/app.json` (or an Expo config plugin) rather than by
hand-editing the generated projects — `expo prebuild --clean` will overwrite manual changes.

To regenerate them from scratch:

```sh
yarn example prebuild
```

## Using the local library

`example/react-native.config.js` points `react-native-menus` at the repository root, so the
example always builds against the library source in `../src`, `../ios` and `../android` — no
publishing or linking step required. Metro is configured for the monorepo in
`example/metro.config.js`.
