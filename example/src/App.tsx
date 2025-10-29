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

          {/* Example 5: Custom Trigger with Child View */}
          <View style={styles.menuContainer}>
            <Text style={styles.subtitle}>5. Custom Menu Trigger</Text>
            <Text style={styles.hint}>Using custom child component</Text>
            <MenuView
              menuItems={[
                {
                  identifier: 'profile',
                  title: 'View Profile',
                  iosSymbol: asSFSymbol('person.circle'),
                },
                {
                  identifier: 'settings',
                  title: 'Settings',
                  iosSymbol: asSFSymbol('gear'),
                },
                {
                  identifier: 'help',
                  title: 'Help & Support',
                  iosSymbol: asSFSymbol('questionmark.circle'),
                },
                {
                  identifier: 'logout',
                  title: 'Logout',
                  iosSymbol: asSFSymbol('rectangle.portrait.and.arrow.right'),
                },
              ]}
              onMenuSelect={handleMenuSelect}
            >
              <View style={styles.customButton}>
                <Text style={styles.customButtonText}>👤 Account Menu</Text>
              </View>
            </MenuView>
          </View>

          {/* Example 6: Long List with Scrolling */}
          <View style={styles.menuContainer}>
            <Text style={styles.subtitle}>6. Long List (Scrollable)</Text>
            <MenuView
              checkedColor="#5856D6"
              menuItems={[
                {
                  identifier: 'opt1',
                  title: 'Option 1',
                  iosSymbol: asSFSymbol('1.circle'),
                },
                {
                  identifier: 'opt2',
                  title: 'Option 2',
                  iosSymbol: asSFSymbol('2.circle'),
                },
                {
                  identifier: 'opt3',
                  title: 'Option 3',
                  iosSymbol: asSFSymbol('3.circle'),
                },
                {
                  identifier: 'opt4',
                  title: 'Option 4',
                  iosSymbol: asSFSymbol('4.circle'),
                },
                {
                  identifier: 'opt5',
                  title: 'Option 5',
                  iosSymbol: asSFSymbol('5.circle'),
                },
                {
                  identifier: 'opt6',
                  title: 'Option 6',
                  iosSymbol: asSFSymbol('6.circle'),
                },
                {
                  identifier: 'opt7',
                  title: 'Option 7',
                  iosSymbol: asSFSymbol('7.circle'),
                },
                {
                  identifier: 'opt8',
                  title: 'Option 8',
                  iosSymbol: asSFSymbol('8.circle'),
                },
                {
                  identifier: 'opt9',
                  title: 'Option 9',
                  iosSymbol: asSFSymbol('9.circle'),
                },
                {
                  identifier: 'opt10',
                  title: 'Option 10',
                  iosSymbol: asSFSymbol('10.circle'),
                },
                {
                  identifier: 'opt11',
                  title: 'Option 11',
                  iosSymbol: asSFSymbol('11.circle'),
                },
                {
                  identifier: 'opt12',
                  title: 'Option 12',
                  iosSymbol: asSFSymbol('12.circle'),
                },
                {
                  identifier: 'opt13',
                  title: 'Option 13',
                  iosSymbol: asSFSymbol('13.circle'),
                },
                {
                  identifier: 'opt14',
                  title: 'Option 14',
                  iosSymbol: asSFSymbol('14.circle'),
                },
                {
                  identifier: 'opt15',
                  title: 'Option 15',
                  iosSymbol: asSFSymbol('15.circle'),
                },
                {
                  identifier: 'opt16',
                  title: 'Option 16',
                  iosSymbol: asSFSymbol('16.circle'),
                },
                {
                  identifier: 'opt17',
                  title: 'Option 17',
                  iosSymbol: asSFSymbol('17.circle'),
                },
                {
                  identifier: 'opt18',
                  title: 'Option 18',
                  iosSymbol: asSFSymbol('18.circle'),
                },
                {
                  identifier: 'opt19',
                  title: 'Option 19',
                  iosSymbol: asSFSymbol('19.circle'),
                },
                {
                  identifier: 'opt20',
                  title: 'Option 20',
                  iosSymbol: asSFSymbol('20.circle'),
                },
              ]}
              onMenuSelect={handleMenuSelect}
            >
              <View style={styles.menuButton}>
                <Text style={styles.menuButtonText}>📋 Select Option</Text>
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
});

export default App;
