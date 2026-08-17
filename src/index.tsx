import type { ReactNode } from 'react';
import React, { useRef, useImperativeHandle } from 'react';
import { Platform, StyleSheet, View } from 'react-native';
import type {
  MenuItem,
  MenuSelectEvent,
  MenuViewRef,
  MenuViewProps as NativeMenuViewProps,
} from './MenuViewNativeComponent';
import NativeMenuView, { Commands } from './MenuViewNativeComponent';

/**
 * `Commands` is declared with the ref type React Native's codegen parser demands
 * (`React.ComponentRef<HostComponent<...>>`), which TypeScript resolves to `never` under
 * the `react-native-strict-api` condition. Re-narrow it here to the instance type a host
 * ref actually holds. See the comment in ./MenuViewNativeComponent.ts before changing this.
 */
const MenuCommands = Commands as unknown as {
  open: (viewRef: MenuViewRef) => void;
  close: (viewRef: MenuViewRef) => void;
};

export type { MenuItem, MenuSelectEvent };

// Re-export SF Symbols helpers for consumers
export type { SFSymbol } from './sf-symbols';
export { asSFSymbol, isLikelySFSymbol, CommonSFSymbols } from './sf-symbols';

export interface MenuViewProps extends Omit<NativeMenuViewProps, 'children'> {
  children?: ReactNode;
  disabled?: boolean;
}

export interface NativeRef {
  /**
   * Opens the menu programmatically. No-op while `disabled`.
   *
   * On iOS this requires 17.4 or newer — older versions have no public API for
   * presenting a `UIButton`'s menu, so the call logs a warning and does nothing.
   */
  open: () => void;
  /** Dismisses the menu if it is currently open. */
  close: () => void;
}

export const MenuView = React.forwardRef<NativeRef, MenuViewProps>(
  (props, ref) => {
    const { children, ...nativeProps } = props;
    const nativeRef = useRef<MenuViewRef>(null);

    useImperativeHandle(
      ref,
      () => ({
        open: () => {
          if (nativeRef.current) {
            MenuCommands.open(nativeRef.current);
          }
        },
        close: () => {
          if (nativeRef.current) {
            MenuCommands.close(nativeRef.current);
          }
        },
      }),
      []
    );

    if (Platform.OS === 'ios') {
      return (
        <NativeMenuView ref={nativeRef} {...nativeProps}>
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
          ref={nativeRef}
          {...nativeProps}
        />
        <View>{children}</View>
      </View>
    );
  }
);

MenuView.displayName = 'MenuView';

const styles = StyleSheet.create({
  relative: {
    position: 'relative',
  },
});

export default MenuView;
