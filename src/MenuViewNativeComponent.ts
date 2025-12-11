import type { ViewProps, HostComponent } from 'react-native';
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

type ComponentType = HostComponent<NativeProps>;

interface NativeCommands {
  open: (viewRef: React.ElementRef<ComponentType>) => void;
  close: (viewRef: React.ElementRef<ComponentType>) => void;
}

export const Commands: NativeCommands = codegenNativeCommands<NativeCommands>({
  supportedCommands: ['open', 'close'],
});

export default codegenNativeComponent<NativeProps>('MenuView');
export type { NativeProps as MenuViewProps };
