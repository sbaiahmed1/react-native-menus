import { useState } from 'react';
import {
  Alert,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { MenuView, asSFSymbol } from 'react-native-menus';

const App = () => {
  const [selectedTheme, setSelectedTheme] = useState('dark');
  const [selectedSort, setSelectedSort] = useState('date');

  const handleMenuSelect = (event: {
    nativeEvent: { identifier: string; title: string };
  }) => {
    const { identifier, title } = event.nativeEvent;
    console.log('Selected:', identifier, title);
    Alert.alert('Menu Selection', `You selected: ${title}`);
  };

  const handleThemeSelect = (event: {
    nativeEvent: { identifier: string; title: string };
  }) => {
    setSelectedTheme(event.nativeEvent.identifier);
  };

  const handleSortSelect = (event: {
    nativeEvent: { identifier: string; title: string };
  }) => {
    setSelectedSort(event.nativeEvent.identifier);
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView}>
        <View style={styles.content}>
          <Text style={styles.title}>React Native Menu Examples</Text>
          <Text style={styles.description}>
            Tap any menu to see native iOS/Android context menus
          </Text>

          {/* Example 1: Theme Selector */}
          <View style={styles.menuContainer}>
            <Text style={styles.subtitle}>1. Theme Selector</Text>
            <Text style={styles.hint}>Current: {selectedTheme}</Text>
            <MenuView
              checkedColor="#007AFF"
              uncheckedColor="#8E8E93"
              selectedIdentifier={selectedTheme}
              menuItems={[
                {
                  identifier: 'light',
                  title: 'Light Mode',
                  iosSymbol: asSFSymbol('sun.max'),
                },
                {
                  identifier: 'dark',
                  title: 'Dark Mode',
                  iosSymbol: asSFSymbol('moon.fill'),
                },
                {
                  identifier: 'system',
                  title: 'System Default',
                  iosSymbol: asSFSymbol('gearshape'),
                },
              ]}
              onMenuSelect={handleThemeSelect}
            >
              <View style={styles.menuButton}>
                <Text style={styles.menuButtonText}>
                  🌓{' '}
                  {selectedTheme === 'light'
                    ? 'Light Mode'
                    : selectedTheme === 'dark'
                      ? 'Dark Mode'
                      : 'System Default'}
                </Text>
              </View>
            </MenuView>
          </View>

          {/* Example 2: Sort Options */}
          <View style={styles.menuContainer}>
            <Text style={styles.subtitle}>2. Sort Options</Text>
            <Text style={styles.hint}>Current: {selectedSort}</Text>
            <MenuView
              menuItems={[
                {
                  identifier: 'date',
                  title: 'Date',
                  iosSymbol: asSFSymbol('calendar'),
                },
                {
                  identifier: 'name',
                  title: 'Name',
                  iosSymbol: asSFSymbol('textformat'),
                },
                {
                  identifier: 'rating',
                  title: 'Rating',
                  iosSymbol: asSFSymbol('star.fill'),
                },
                {
                  identifier: 'size',
                  title: 'Size',
                  iosSymbol: asSFSymbol('square.stack.3d.up'),
                },
              ]}
              selectedIdentifier={selectedSort}
              onMenuSelect={handleSortSelect}
            >
              <View style={styles.menuButton}>
                <Text style={styles.menuButtonText}>🔽 Sort</Text>
              </View>
            </MenuView>
          </View>

          {/* Example 3: Actions Menu */}
          <View style={styles.menuContainer}>
            <Text style={styles.subtitle}>3. Actions</Text>
            <MenuView
              menuItems={[
                {
                  identifier: 'reply',
                  title: 'Reply',
                  iosSymbol: asSFSymbol('arrowshape.turn.up.left'),
                },
                {
                  identifier: 'forward',
                  title: 'Forward',
                  iosSymbol: asSFSymbol('arrowshape.turn.up.right'),
                },
                {
                  identifier: 'archive',
                  title: 'Archive',
                  iosSymbol: asSFSymbol('archivebox'),
                },
                {
                  identifier: 'delete',
                  title: 'Delete',
                  iosSymbol: asSFSymbol('trash'),
                },
              ]}
              onMenuSelect={handleMenuSelect}
            >
              <View style={styles.menuButton}>
                <Text style={styles.menuButtonText}>✉️ Message Actions</Text>
              </View>
            </MenuView>
          </View>

          {/* Example 4: Priority Selector */}
          <View style={styles.menuContainer}>
            <Text style={styles.subtitle}>4. Priority Level</Text>
            <MenuView
              checkedColor="#FF3B30"
              menuItems={[
                {
                  identifier: 'low',
                  title: 'Low Priority',
                  iosSymbol: asSFSymbol('arrow.down'),
                },
                {
                  identifier: 'medium',
                  title: 'Medium Priority',
                  iosSymbol: asSFSymbol('equal'),
                },
                {
                  identifier: 'high',
                  title: 'High Priority',
                  iosSymbol: asSFSymbol('arrow.up'),
                },
                {
                  identifier: 'urgent',
                  title: 'Urgent',
                  iosSymbol: asSFSymbol('exclamationmark.triangle'),
                },
              ]}
              onMenuSelect={handleMenuSelect}
            >
              <View style={styles.menuButton}>
                <Text style={styles.menuButtonText}>⚡ Set Priority</Text>
              </View>
            </MenuView>
          </View>

          {/* Example 6: Complex Menu */}
          <View style={styles.menuContainer}>
            <Text style={styles.subtitle}>6. Complex Menu</Text>
            <Text style={styles.hint}>
              Title, Subtitles, Destructive Items, Theme
            </Text>
            <MenuView
              title="Advanced Options"
              themeVariant="dark"
              menuItems={[
                {
                  identifier: 'info',
                  title: 'Information',
                  subtitle: 'View details about this item',
                  iosSymbol: asSFSymbol('info.circle'),
                },
                {
                  identifier: 'share',
                  title: 'Share',
                  subtitle: 'Share with friends',
                  iosSymbol: asSFSymbol('square.and.arrow.up'),
                },
                {
                  identifier: 'delete',
                  title: 'Delete Item',
                  subtitle: 'This action cannot be undone',
                  iosSymbol: asSFSymbol('trash'),
                  destructive: true,
                },
              ]}
              onMenuSelect={handleMenuSelect}
            >
              <View style={[styles.menuButton, { backgroundColor: '#333' }]}>
                <Text style={[styles.menuButtonText, { color: '#fff' }]}>
                  🛠️ Advanced Menu (Dark)
                </Text>
              </View>
            </MenuView>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 16,
    gap: 16,
  },
  title: {
    fontSize: 24,
    fontWeight: '600',
  },
  description: {
    color: '#6b7280',
  },
  menuContainer: {
    gap: 12,
    paddingVertical: 8,
  },
  subtitle: {
    fontSize: 18,
    fontWeight: '500',
  },
  hint: {
    color: '#6b7280',
  },
  menuButton: {
    padding: 12,
    borderRadius: 8,
    backgroundColor: '#f3f4f6',
  },
  menuButtonText: {
    fontSize: 16,
  },
  customButton: {
    padding: 16,
    borderRadius: 12,
    backgroundColor: '#eef2ff',
  },
  customButtonText: {
    fontSize: 16,
    fontWeight: '500',
  },
  disabledButton: {
    backgroundColor: '#f3f4f6',
    opacity: 0.6,
  },
  disabledText: {
    color: '#9ca3af',
  },
});

export default App;
