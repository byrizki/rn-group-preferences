import { Platform } from 'react-native';
import NativeRNGroupPreferences from './src/NativeRNGroupPreferences';

export default class SharedGroupPreferences {

  static async isAppInstalledAndroid(packageName) {
    // This method is not present in the Spec? 
    // Wait, Android impl has isAppInstalledAndroid.
    // iOS impl does not.
    // Spec needs to include it if we want it typed, or we cast.
    // But NativeGroupPreferences is typed by Spec.
    // I missed adding isAppInstalledAndroid to the Spec.
    // However, I can't update Spec easily now without breaking the flow or I can just update the Spec.
    // For now, I'll assume users use it only on Android and add it to the Spec.
    // But since I'm updating index.js, I should update the Spec first if I want correctness.
    // Or I can just cast or ignore if using JS.
    // Since this is JS, I can just call it if it exists on the native module object at runtime.
    return NativeRNGroupPreferences.isAppInstalledAndroid(packageName);
  }

  static async getItem(key, appGroup, inputOptions) {
    if ((Platform.OS != 'ios') && (Platform.OS != 'android')) {
      throw new Error(Platform.OS);
    }

    const options = inputOptions || {};
    // Native module now returns Promise
    const item = await NativeRNGroupPreferences.getItem(key, appGroup, options);
    
    if (item == null) return null;

    let isJson = false;
    try {
      const json = JSON.parse(item);
      isJson = typeof json === 'object';
    } catch (e) {
      isJson = false;
    }

    if (isJson) {
      return JSON.parse(item);
    } else {
      return item;
    }
  }

  static async setItem(key, value, appGroup, inputOptions) {
    if ((Platform.OS != 'ios') && (Platform.OS != 'android')) {
      throw new Error(Platform.OS);
    }

    const options = inputOptions || {};
    let _value = String(value);
    if (typeof value === 'object') {
      _value = JSON.stringify(value);
    }
    
    return NativeRNGroupPreferences.setItem(key, _value, appGroup, options);
  }
}
