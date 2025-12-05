import type { ViewProps } from 'react-native';
import { codegenNativeComponent } from 'react-native';
import type { BubblingEventHandler } from 'react-native/Libraries/Types/CodegenTypes';

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
  themeVariant?: string;
  color?: string;
  checkedColor?: string;
  uncheckedColor?: string;
  menuItems?: ReadonlyArray<MenuItem>;
  selectedIdentifier?: string;
  disabled?: boolean;
  onMenuSelect?: BubblingEventHandler<MenuSelectEvent>;
}

export default codegenNativeComponent<NativeProps>('MenuView');
export type { NativeProps as MenuViewProps };
