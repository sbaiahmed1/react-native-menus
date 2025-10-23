import type { ReactNode } from 'react';
import React from 'react';
import { Platform, StyleSheet, View } from 'react-native';
import type {
  MenuItem,
  MenuSelectEvent,
  MenuViewProps as NativeMenuViewProps,
} from './MenuViewNativeComponent';
import NativeMenuView from './MenuViewNativeComponent';

export type { MenuItem, MenuSelectEvent };

export interface MenuViewProps extends Omit<NativeMenuViewProps, 'children'> {
  children?: ReactNode;
}

export const MenuView = React.forwardRef<any, MenuViewProps>((props, ref) => {
  const { children, ...nativeProps } = props;

  if (Platform.OS === 'ios') {
    return (
      <NativeMenuView ref={ref} {...nativeProps}>
        {children}
      </NativeMenuView>
    );
  }

  return (
    <View style={styles.relative}>
      <NativeMenuView
        style={[
          StyleSheet.absoluteFill,
          {
            zIndex: 1,
          },
        ]}
        ref={ref}
        {...nativeProps}
      />
      <View>{children}</View>
    </View>
  );
});

MenuView.displayName = 'MenuView';

const styles = StyleSheet.create({
  relative: {
    position: 'relative',
  },
});

export default MenuView;
