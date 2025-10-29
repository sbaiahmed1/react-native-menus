/**
 * Lightweight SF Symbols utilities for TypeScript.
 *
 * This module intentionally avoids bundling the full list of symbol names.
 * It provides a branded string type, simple validators, and a small set of
 * commonly used examples for IDE hinting. Use any valid SF Symbol name.
 */

// A nominal type to distinguish SF Symbol names from arbitrary strings.
declare const __sfSymbolBrand: unique symbol;
export type SFSymbol = string & { readonly [__sfSymbolBrand]: true };

// Known SF Symbols versions (for consumers who want to restrict usage).
export type SFSymbolVersion =
  | '1.0'
  | '1.1'
  | '2.0'
  | '2.1'
  | '2.2'
  | '3.0'
  | '3.1'
  | '3.2'
  | '3.3'
  | '4.0'
  | '4.1'
  | '4.2'
  | '5.0'
  | '5.1'
  | '5.2'
  | '5.3'
  | '6.0';

// Minimum platform support by SF Symbols version (reference only).
export const SFSymbolsVersionSupport = {
  '1.0': {
    ios: '13.0',
    macos: '10.15',
    tvos: '13.0',
    visionos: '1.0',
    watchos: '6.0',
  },
  '1.1': {
    ios: '13.1',
    macos: '11.0',
    tvos: '13.0',
    visionos: '1.0',
    watchos: '6.1',
  },
  '2.0': {
    ios: '14.0',
    macos: '11.0',
    tvos: '14.0',
    visionos: '1.0',
    watchos: '7.0',
  },
  '2.1': {
    ios: '14.2',
    macos: '11.0',
    tvos: '14.2',
    visionos: '1.0',
    watchos: '7.1',
  },
  '2.2': {
    ios: '14.5',
    macos: '11.3',
    tvos: '14.5',
    visionos: '1.0',
    watchos: '7.4',
  },
  '3.0': {
    ios: '15.0',
    macos: '12.0',
    tvos: '15.0',
    visionos: '1.0',
    watchos: '8.0',
  },
  '3.1': {
    ios: '15.1',
    macos: '12.0',
    tvos: '15.1',
    visionos: '1.0',
    watchos: '8.1',
  },
  '3.2': {
    ios: '15.2',
    macos: '12.1',
    tvos: '15.2',
    visionos: '1.0',
    watchos: '8.3',
  },
  '3.3': {
    ios: '15.4',
    macos: '12.3',
    tvos: '15.4',
    visionos: '1.0',
    watchos: '8.5',
  },
  '4.0': {
    ios: '16.0',
    macos: '13.0',
    tvos: '16.0',
    visionos: '1.0',
    watchos: '9.0',
  },
  '4.1': {
    ios: '16.1',
    macos: '13.0',
    tvos: '16.1',
    visionos: '1.0',
    watchos: '9.1',
  },
  '4.2': {
    ios: '16.4',
    macos: '13.3',
    tvos: '16.4',
    visionos: '1.0',
    watchos: '9.4',
  },
  '5.0': {
    ios: '17.0',
    macos: '14.0',
    tvos: '17.0',
    visionos: '1.0',
    watchos: '10.0',
  },
  '5.1': {
    ios: '17.2',
    macos: '14.2',
    tvos: '17.2',
    visionos: '1.1',
    watchos: '10.2',
  },
  '5.2': {
    ios: '17.4',
    macos: '14.24',
    tvos: '17.4',
    visionos: '1.1',
    watchos: '10.4',
  },
  '5.3': {
    ios: '17.6',
    macos: '14.6',
    tvos: '17.6',
    visionos: '1.3',
    watchos: '10.6',
  },
  '6.0': {
    ios: '18.0',
    macos: '15.0',
    tvos: '18.0',
    visionos: '2.0',
    watchos: '11.0',
  },
} as const;

/**
 * Heuristic validator for SF Symbol names.
 * Accepts dot-separated lowercase tokens with numbers (e.g. "arrow.up.circle.fill").
 * This is not exhaustive, but helps catch obvious mistakes without enumerating names.
 */
export function isLikelySFSymbol(name: string): boolean {
  if (name.length < 2 || name.length > 128) return false;
  return /^[a-z0-9]+(?:\.[a-z0-9]+)*$/.test(name);
}

/**
 * Cast any string as an `SFSymbol`. Optionally validates in development.
 */
export function asSFSymbol(name: string): SFSymbol {
  if (__DEV__) {
    if (!isLikelySFSymbol(name)) {
      // non-throwing hint; consumers can choose to throw if they prefer

      console.warn(
        `[menu] Provided iosSymbol "${name}" does not match common SF Symbol naming patterns.`
      );
    }
  }
  return name as SFSymbol;
}

/**
 * A small set of commonly used symbols for IDE hinting.
 * Extend locally if desired — values are const so TS will suggest them.
 */
export const CommonSFSymbols = [
  'arrow.up',
  'arrow.down',
  'arrow.left',
  'arrow.right',
  'gear',
  'heart',
  'trash',
  'checkmark',
  'xmark',
] as const;

export type CommonSFSymbol = (typeof CommonSFSymbols)[number] & SFSymbol;
