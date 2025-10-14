import type { ViewProps } from 'react-native';
import { codegenNativeComponent } from 'react-native';
import type { BubblingEventHandler } from 'react-native/Libraries/Types/CodegenTypes';

export interface MenuItem {
  identifier: string;
  title: string;
}

export interface MenuSelectEvent {
  identifier: string;
  title: string;
}

export interface NativeProps extends ViewProps {
  color?: string;
  checkedColor?: string;
  uncheckedColor?: string;
  menuItems?: ReadonlyArray<MenuItem>;
  selectedIdentifier?: string;
  onMenuSelect?: BubblingEventHandler<MenuSelectEvent>;
}

export default codegenNativeComponent<NativeProps>('MenuView');
export type { NativeProps as MenuViewProps };
