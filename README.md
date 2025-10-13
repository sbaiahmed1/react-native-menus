# React Native Menus

A native menu component for React Native that provides platform-specific context menus for both Android and iOS. Pass any custom component as a child to trigger native menus.

## Screenshots

<table>
  <tr>
    <td align="center">
      <img src="ios-image.png" alt="iOS Menu" height="600" width="400"/>
      <br />
      <strong>iOS (Native UIMenu)</strong>
    </td>
    <td align="center">
      <img src="android-image.png" alt="Android Menu" height="600" width="400"/>
      <br />
      <strong>Android (Modal Dialog)</strong>
    </td>
  </tr>
</table>

## Features

- ✅ Native context menu implementation (UIMenu on iOS, Modal on Android)
- ✅ Custom trigger components - pass any React Native component as a child
- ✅ Customizable colors for menu items
- ✅ Checkmark support with custom colors
- ✅ Scrollable menus for long lists
- ✅ Event handling for menu item selection
- ✅ TypeScript support
- ✅ Fabric (New Architecture) compatible

## Installation

```bash
npm install react-native-menus
# or
yarn add react-native-menus
```

### iOS Setup

For iOS, run:

```bash
cd ios && pod install
```

### Android Setup

No additional setup required for Android.

## Usage

### Basic Example

```tsx
import React, { useState } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { MenuView } from 'react-native-menus';

const App = () => {
  const [selectedTheme, setSelectedTheme] = useState('system');

  const handleMenuSelect = (event: {
    nativeEvent: { identifier: string; title: string };
  }) => {
    setSelectedTheme(event.nativeEvent.identifier);
    console.log('Selected:', event.nativeEvent.title);
  };

  return (
    <View style={styles.container}>
      <MenuView
        checkedColor="#007AFF"
        uncheckedColor="#8E8E93"
        menuItems={[
          { identifier: 'light', title: 'Light Mode' },
          { identifier: 'dark', title: 'Dark Mode' },
          { identifier: 'system', title: 'System Default' },
        ]}
        onMenuSelect={handleMenuSelect}
      >
        <View style={styles.menuButton}>
          <Text style={styles.menuButtonText}>
            🌓 Theme: {selectedTheme}
          </Text>
        </View>
      </MenuView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  menuButton: {
    backgroundColor: '#fff',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  menuButtonText: {
    fontSize: 16,
    color: '#333',
  },
});

export default App;
```

### Custom Styled Trigger

```tsx
<MenuView
  menuItems={[
    { identifier: 'profile', title: 'View Profile' },
    { identifier: 'settings', title: 'Settings' },
    { identifier: 'logout', title: 'Logout' },
  ]}
  onMenuSelect={handleMenuSelect}
>
  <View style={styles.customButton}>
    <Text style={styles.customButtonText}>👤 Account Menu</Text>
  </View>
</MenuView>
```

### Long Scrollable List

```tsx
<MenuView
  checkedColor="#5856D6"
  menuItems={[
    { identifier: 'opt1', title: 'Option 1' },
    { identifier: 'opt2', title: 'Option 2' },
    // ... many more items
    { identifier: 'opt20', title: 'Option 20' },
  ]}
  onMenuSelect={handleMenuSelect}
>
  <View style={styles.menuButton}>
    <Text>📋 Select Option</Text>
  </View>
</MenuView>
```

## API Reference

### Props

| Prop | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| `children` | `ReactNode` | **Yes** | - | The trigger component that opens the menu when tapped |
| `menuItems` | `MenuItem[]` | Yes | `[]` | Array of menu items to display |
| `onMenuSelect` | `(event: MenuSelectEvent) => void` | No | - | Callback fired when a menu item is selected |
| `checkedColor` | `string` | No | `#007AFF` | Color for checked/selected menu items (Android only) |
| `uncheckedColor` | `string` | No | `#8E8E93` | Color for unchecked/unselected menu items (Android only) |
| `color` | `string` | No | - | Reserved for future use |
| `style` | `ViewStyle` | No | - | Style applied to the container view |

### Types

#### MenuItem

```typescript
interface MenuItem {
  identifier: string; // Unique identifier for the menu item
  title: string;      // Display text for the menu item
}
```

#### MenuSelectEvent

```typescript
interface MenuSelectEvent {
  identifier: string; // The identifier of the selected menu item
  title: string;      // The title of the selected menu item
}
```

## How It Works

### Architecture

The MenuView component accepts any React Native component as a child, which becomes the trigger for opening the menu. When tapped, a native context menu appears with the specified menu items.

### Android Implementation

**File:** `android/src/main/java/com/menu/MenuView.kt`

- **Container:** `FrameLayout` that accepts child views from React Native
- **Touch Handling:** Intercepts all touch events at the parent level using `onInterceptTouchEvent()`
- **UI:** Modal dialog with white background, rounded corners, and bottom positioning
- **Menu Items:** Implemented as `RadioButton` elements with custom styling
- **Colors:** Full support for `checkedColor` and `uncheckedColor` customization
- **Dividers:** 0.5px light gray dividers between menu items
- **Scrolling:** Automatic scrolling for long lists (max 90% of screen height)
- **Selection:** Visual feedback with radio button selection states

**Key Implementation Details:**
- Uses `ViewGroupManager` to support child views
- Touch events are intercepted to ensure child views don't block menu opening
- Modal appears at bottom with horizontal margins for mobile-friendly UX

### iOS Implementation

**File:** `ios/MenuView.mm`

- **Container:** `RCTViewComponentView` (Fabric architecture)
- **Child Mounting:** Uses `mountChildComponentView` and `unmountChildComponentView` for Fabric compatibility
- **UI:** Native `UIMenu` attached to an invisible `UIButton` overlay
- **Menu Items:** Native `UIAction` elements with system styling
- **Trigger:** Creates transparent button overlay on top of child view to show native menu
- **Checkmarks:** Native system checkmarks with `UIMenuElementStateOn`
- **Selection:** Native iOS menu behavior with smooth animations

**Key Implementation Details:**
- Disables user interaction on child views recursively
- Creates invisible button overlay that shows `UIMenu` on tap
- Fully native iOS 14+ context menu appearance
- Supports both button and non-button child components

### Platform Differences

| Feature | iOS | Android |
|---------|-----|---------|
| Menu Style | Native UIMenu popover | Modal dialog at bottom |
| Checkmark Color | System default (not customizable) | Fully customizable |
| Unchecked Color | System default | Fully customizable |
| Animation | Native iOS animation | Slide up animation |
| Scrolling | Native UIMenu scrolling | Custom ScrollView (max 40% screen) |
| Appearance | iOS system theme | White background with rounded corners |

## Example Project

The repository includes a complete example project with 6 different use cases:

1. **Theme Selector** - Shows state management with current selection
2. **Sort Options** - Demonstrates sorting menu with dynamic labels
3. **File Actions** - Common file operations menu
4. **Priority Selector** - Priority level selection
5. **Custom Trigger** - Fully custom styled button
6. **Long List** - Scrollable menu with many items

```bash
cd example
yarn install
# For iOS
cd ios && pod install && cd ..
yarn ios
# For Android
yarn android
```

## Requirements

- React Native >= 0.68.0
- iOS >= 14.0 (for UIMenu support)
- Android API >= 21

## Troubleshooting

### Menu not opening on tap

**iOS:** Make sure you're running iOS 14 or later, as UIMenu is only available from iOS 14+.

**Android:** Ensure your child component doesn't have `onPress` or other touch handlers that might interfere. The MenuView intercepts all touch events at the parent level.

### Children prop is required

The MenuView component requires a child component to act as the trigger. Always wrap your trigger in the MenuView:

```tsx
// ✅ Correct
<MenuView menuItems={items}>
  <View><Text>Open Menu</Text></View>
</MenuView>

// ❌ Wrong - no children
<MenuView menuItems={items} />
```

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
