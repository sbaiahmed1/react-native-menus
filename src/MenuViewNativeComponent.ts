import type { ViewProps, HostInstance } from 'react-native';
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
}

const MenuViewNativeComponent = codegenNativeComponent<NativeProps>('MenuView');

// The ref a host component hands back. Use React Native's own `HostInstance` rather than
// `React.ElementRef`/`React.ComponentRef` of `HostComponent<NativeProps>`: under the
// `react-native-strict-api` condition this project compiles with (see tsconfig.json),
// `HostComponent` is a function type, and React's ref helpers resolve it to `never` —
// which silently turns every command call into a type error.
type MenuViewRef = HostInstance;

interface NativeCommands {
  open: (viewRef: MenuViewRef) => void;
  close: (viewRef: MenuViewRef) => void;
}

export const Commands: NativeCommands = codegenNativeCommands<NativeCommands>({
  supportedCommands: ['open', 'close'],
});

export default MenuViewNativeComponent;
export type { NativeProps as MenuViewProps, MenuViewRef };
