import React from 'react';
import type { ReactNode } from 'react';
import NativeMenuView from './MenuViewNativeComponent';
import type {
  MenuViewProps as NativeMenuViewProps,
  MenuItem,
  MenuSelectEvent,
} from './MenuViewNativeComponent';

export type { MenuItem, MenuSelectEvent };

export interface MenuViewProps extends Omit<NativeMenuViewProps, 'children'> {
  children?: ReactNode;
}

export const MenuView = React.forwardRef<any, MenuViewProps>((props, ref) => {
  const { children, ...nativeProps } = props;

  return (
    <NativeMenuView ref={ref} {...nativeProps}>
      {children}
    </NativeMenuView>
  );
});

MenuView.displayName = 'MenuView';

export default MenuView;
