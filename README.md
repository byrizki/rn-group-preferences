# rn-group-preferences

React Native TurboModule for accessing Shared Preferences (Android) and App Groups (iOS). Fully compatible with the New Architecture.

## Features

- **New Architecture Support**: Full TurboModule implementation.
- **Shared Data**: Share data between your app and extensions (iOS App Groups) or other apps (Android ContentProvider/Shared Storage).
- **Simple API**: Promise-based API for easy async/await usage.

## Installation

```bash
yarn add rn-group-preferences
# or
npm install rn-group-preferences
```

### iOS Setup

1. Enable **App Groups** in your Xcode project capabilities.
2. Run standard pod install:

```bash
cd ios && pod install
```

### Android Setup

No manual linking required.

### Expo Setup

This package includes a [Config Plugin](https://docs.expo.dev/config-plugins/introduction/) to automate the iOS App Group entitlement setup.

Ad to your `app.json` or `app.config.js`:

```json
{
  "expo": {
    "plugins": [
      [
        "rn-group-preferences",
        {
          "appGroup": "group.com.your.app.group"
        }
      ]
    ]
  }
}
```

**Note on AGP 8**: This library is compatible with Android Gradle Plugin 8+.
**Note on New Architecture**: This library is designed for the New Architecture (TurboModules) and requires it to be enabled in your app.

## Credits

This package is a modernized fork of [react-native-shared-group-preferences](https://github.com/KjellConnelly/react-native-shared-group-preferences) by Kjell Connelly.

## API

_set/get basic key/value pairs_

- SharedGroupPreferences.setItem(string:key, any:value, string:appGroupIdentifier, (optional)object:options)
- SharedGroupPreferences.getItem(string:key, string:appGroupIdentifier, (optional)object:options)

## Usage

```javascript
import SharedGroupPreferences from "rn-group-preferences";

const appGroupIdentifier = "group.com.mytest";
const userData = {
  name: "Vin Diesel",
  age: 34,
  friends: ["Lara Croft", "Mike Meyers"],
};

export default class app extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      username: undefined,
    };

    // Not the most professional way to ask for permissions: Just ask when the app loads.
    // But for brevity, we do this here.
    if (Platform.OS == "android") {
      this.dealWithPermissions();
    } else {
      this.saveUserDataToSharedStorage(userData);
    }
  }

  async dealWithPermissions() {
    try {
      const grantedStatus = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
        PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
      ]);
      const writeGranted =
        grantedStatus["android.permission.WRITE_EXTERNAL_STORAGE"] ===
        PermissionsAndroid.RESULTS.GRANTED;
      const readGranted =
        grantedStatus["android.permission.READ_EXTERNAL_STORAGE"] ===
        PermissionsAndroid.RESULTS.GRANTED;
      if (writeGranted && readGranted) {
        this.saveUserDataToSharedStorage(userData);
      } else {
        // You can either limit the user in access to the app's content,
        // or do a workaround where the user's data is saved using only
        // within the user's local app storage using something like AsyncStorage
        // instead. This is only an android issue since it uses read/write external storage.
      }
    } catch (err) {
      console.warn(err);
    }
  }

  async saveUserDataToSharedStorage(data) {
    try {
      await SharedGroupPreferences.setItem(
        "savedData",
        data,
        appGroupIdentifier
      );
      this.loadUsernameFromSharedStorage();
    } catch (errorCode) {
      // errorCode 0 = There is no suite with that name
      console.log(errorCode);
    }
  }

  async loadUsernameFromSharedStorage() {
    try {
      const loadedData = await SharedGroupPreferences.getItem(
        "savedData",
        appGroupIdentifier
      );
      this.setState({ username: loadedData.name });
    } catch (errorCode) {
      // errorCode 0 = no group name exists. You probably need to setup your Xcode Project properly.
      // errorCode 1 = there is no value for that key
      console.log(errorCode);
    }
  }

  render() {
    return (
      <View>
        <Text>
          {this.state.username
            ? "Loading..."
            : "Welcome back " + this.state.username}
        </Text>
      </View>
    );
  }
}
```

## iOS Xcode Prep Work

In Xcode, open your Target and click the `Capabilities` tab. Go down to `App Groups`. Add a preexisting identifier or create a new one. Do the same for all the apps that you plan to have a shared container for. Use this identifier for `appGroupIdentifier` when you call the javascript functions.

## Android Configuration

### External Storage Permissions

To share data between different applications on Android, this library uses External Storage. You must request proper permissions:

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

For Android 11+ (SDK 30+), Scoped Storage rules apply. If you are targeting SDK 30+, consider using `SharedPreferences` option if data sharing between distinct apps is not required, or ensure your app has the necessary entitlements for broader storage access if needed.

**Note**: For legacy compatibility (SDK 29), you might need `android:requestLegacyExternalStorage="true"` in your `AndroidManifest.xml`.

## Android Storage Options

### Shared Preferences (Internal)

By default, this library uses External Storage to share data between apps (mimicking iOS App Groups). If you prefer to use standard Android `SharedPreferences` (e.g., for internal app use or extensions), you can pass an option:

```javascript
await SharedGroupPreferences.setItem("key", "value", appGroup, {
  useAndroidSharedPreferences: true,
});
```

## Utilities

### Check if App is Installed (Android)

Check if an application is installed on the device by its package name.

```javascript
const facebookPackageName = "com.facebook.android";
try {
  const installed = await SharedGroupPreferences.isAppInstalledAndroid(
    facebookPackageName
  );
  console.log("Facebook is installed");
} catch (err) {
  console.log("Facebook is not installed");
}
```
