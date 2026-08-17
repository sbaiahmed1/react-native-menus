import type { ViewProps, HostComponent, HostInstance } from 'react-native';
import { codegenNativeComponent, codegenNativeCommands } from 'react-native';
import type { BubblingEventHandler } from 'react-native/Libraries/Types/CodegenTypes';
import type { WithDefault } from 'react-native/Libraries/Types/CodegenTypesNamespace';

export interface MenuItem {
  identifier: string;
  title: string;
  subtitle?: string;
  destructive?: boolean;
  // iOS-only: SF Symbol name to show beside the title
  iosSymbol?: string;
  /**
   * What a screen reader announces for this item, replacing the visible text.
   * Defaults to `title`, followed by `subtitle` when present.
   */
  accessibilityLabel?: string;
  /**
   * Extra guidance announced after the label, e.g. "Permanently deletes the item".
   * Destructive items already announce as destructive without this.
   */
  accessibilityHint?: string;
}

export interface MenuSelectEvent {
  identifier: string;
  title: string;
}

export interface NativeProps extends ViewProps {
  title?: string;
  themeVariant?: WithDefault<'light' | 'dark' | 'system', 'system'>;
  color?: string;
  checkedColor?: string;
  uncheckedColor?: string;
  menuItems?: ReadonlyArray<MenuItem>;
  selectedIdentifier?: string;
  disabled?: boolean;
  androidDisplayMode?: WithDefault<'dialog' | 'tooltip', 'dialog'>;
  onMenuSelect?: BubblingEventHandler<MenuSelectEvent>;
  /**
   * Expands the trigger's *hit area* to at least 44pt (iOS) / 48dp (Android) when the
   * rendered view is smaller, satisfying WCAG 2.2 target-size guidance.
   *
   * This only affects touch and accessibility hit-testing — it never changes layout, so
   * nothing moves on screen. Set to `false` to opt out.
   */
  enforceMinimumTouchTarget?: WithDefault<boolean, true>;
  /**
   * Announced after the trigger's label to explain what activating it does.
   * Defaults to a platform-appropriate phrase such as "Opens a menu".
   */
  menuAccessibilityHint?: string;
}

// The ref a host component hands back.
type MenuViewRef = HostInstance;

type ComponentType = HostComponent<NativeProps>;

/**
 * Two incompatible requirements meet here, so read before changing:
 *
 *  - React Native's codegen parser checks this annotation *syntactically* and rejects
 *    anything that is not literally `React.ElementRef<>` or `React.ComponentRef<>`. Using
 *    an alias makes codegen fail with "The first argument of method open must be of type
 *    React.ElementRef<> or React.ComponentRef<>" and emit an EMPTY props struct — every
 *    prop silently disappears from the native side.
 *  - TypeScript resolves `React.ComponentRef<HostComponent<...>>` to `never` under the
 *    `react-native-strict-api` condition this project compiles with (see tsconfig.json),
 *    where `HostComponent` is a function type. That makes every command call a type error.
 *
 * So: keep the codegen-required syntax here, verbatim. Do NOT add an `as` cast to the
 * export either — that wraps the call in an expression codegen cannot parse, and commands
 * stop being generated (props still are, so the breakage is easy to miss). Callers
 * re-narrow the ref type instead; see `MenuViewRef` use in ./index.tsx.
 */
interface NativeCommands {
  open: (viewRef: React.ComponentRef<ComponentType>) => void;
  close: (viewRef: React.ComponentRef<ComponentType>) => void;
}

export const Commands: NativeCommands = codegenNativeCommands<NativeCommands>({
  supportedCommands: ['open', 'close'],
});

// Must stay a direct `export default codegenNativeComponent<...>(...)` call. React Native's
// codegen parser looks for exactly this shape; assigning it to a const first and exporting
// the const makes codegen emit an EMPTY props struct, silently dropping every prop.
export default codegenNativeComponent<NativeProps>('MenuView');
export type { NativeProps as MenuViewProps, MenuViewRef };
