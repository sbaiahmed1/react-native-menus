import { useState } from 'react';
import {
  Alert,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { MenuView } from 'react-native-menus';

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
    console.log('Theme changed to:', event.nativeEvent.identifier);
  };

  const handleSortSelect = (event: {
    nativeEvent: { identifier: string; title: string };
  }) => {
    setSelectedSort(event.nativeEvent.identifier);
    console.log('Sort changed to:', event.nativeEvent.identifier);
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
                { identifier: 'light', title: 'Light Mode' },
                { identifier: 'dark', title: 'Dark Mode' },
                { identifier: 'system', title: 'System Default' },
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
            <Text style={styles.subtitle}>2. Sort By</Text>
            <Text style={styles.hint}>Current: {selectedSort}</Text>
            <MenuView
              checkedColor="#34C759"
              uncheckedColor="#8E8E93"
              selectedIdentifier={selectedSort}
              menuItems={[
                { identifier: 'date', title: 'Date' },
                { identifier: 'name', title: 'Name' },
                { identifier: 'size', title: 'Size' },
                { identifier: 'type', title: 'Type' },
              ]}
              onMenuSelect={handleSortSelect}
            >
              <View style={styles.menuButton}>
                <Text style={styles.menuButtonText}>
                  📊 Sort by:{' '}
                  {selectedSort.charAt(0).toUpperCase() + selectedSort.slice(1)}
                </Text>
              </View>
            </MenuView>
          </View>

          {/* Example 3: File Actions */}
          <View style={styles.menuContainer}>
            <Text style={styles.subtitle}>3. File Actions</Text>
            <MenuView
              checkedColor="#FF9500"
              menuItems={[
                { identifier: 'open', title: 'Open' },
                { identifier: 'share', title: 'Share' },
                { identifier: 'rename', title: 'Rename' },
                { identifier: 'duplicate', title: 'Duplicate' },
                { identifier: 'delete', title: 'Delete' },
              ]}
              onMenuSelect={handleMenuSelect}
            >
              <View style={styles.menuButton}>
                <Text style={styles.menuButtonText}>📁 File Options</Text>
              </View>
            </MenuView>
          </View>

          {/* Example 4: Priority Selector */}
          <View style={styles.menuContainer}>
            <Text style={styles.subtitle}>4. Priority Level</Text>
            <MenuView
              checkedColor="#FF3B30"
              menuItems={[
                { identifier: 'low', title: 'Low Priority' },
                { identifier: 'medium', title: 'Medium Priority' },
                { identifier: 'high', title: 'High Priority' },
                { identifier: 'urgent', title: 'Urgent' },
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
                { identifier: 'profile', title: 'View Profile' },
                { identifier: 'settings', title: 'Settings' },
                { identifier: 'help', title: 'Help & Support' },
                { identifier: 'logout', title: 'Logout' },
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
                { identifier: 'opt1', title: 'Option 1' },
                { identifier: 'opt2', title: 'Option 2' },
                { identifier: 'opt3', title: 'Option 3' },
                { identifier: 'opt4', title: 'Option 4' },
                { identifier: 'opt5', title: 'Option 5' },
                { identifier: 'opt6', title: 'Option 6' },
                { identifier: 'opt7', title: 'Option 7' },
                { identifier: 'opt8', title: 'Option 8' },
                { identifier: 'opt9', title: 'Option 9' },
                { identifier: 'opt10', title: 'Option 10' },
                { identifier: 'opt11', title: 'Option 11' },
                { identifier: 'opt12', title: 'Option 12' },
                { identifier: 'opt13', title: 'Option 13' },
                { identifier: 'opt14', title: 'Option 14' },
                { identifier: 'opt15', title: 'Option 15' },
                { identifier: 'opt16', title: 'Option 16' },
                { identifier: 'opt17', title: 'Option 17' },
                { identifier: 'opt18', title: 'Option 18' },
                { identifier: 'opt19', title: 'Option 19' },
                { identifier: 'opt20', title: 'Option 20' },
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
    backgroundColor: '#f5f5f5',
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 20,
    paddingBottom: 40,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 10,
    color: '#333',
  },
  description: {
    fontSize: 14,
    textAlign: 'center',
    marginBottom: 30,
    color: '#666',
  },
  subtitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
    color: '#333',
  },
  hint: {
    fontSize: 13,
    marginBottom: 8,
    color: '#999',
  },
  menuContainer: {
    marginBottom: 25,
  },
  menuButton: {
    height: 50,
    backgroundColor: '#fff',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#ddd',
    paddingHorizontal: 16,
    justifyContent: 'center',
  },
  menuButtonText: {
    fontSize: 15,
    color: '#333',
  },
  customButton: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 20,
    paddingVertical: 15,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  customButtonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default App;
