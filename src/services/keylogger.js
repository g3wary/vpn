import { NativeModules } from 'react-native';

export const startKeylogger = async () => {
  // В реальности тут запуск AccessibilityService через Intent
  console.log('Keylogger started');
};
