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
import SharedGroupPreferences from 'rn-group-preferences'
";

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

## Android Prep Work (incomplete)

#### External Storage (public storage, any app can access/modify)

You need Android Permissions for READ & WRITE External Storage. You can get permission using React Native's `PermissionsAndroid` module. How you ask for Permissions is up to you, but can be accomplished like in the example above. Android API 23+ needs you to ask for permissions within the app itself. Below 23 and you can just add these permissions your `AndroidManifest.xml` file. For all versions, you will still need to add these to your manifest. Just you will also need to ask for it in 23+.

```
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

#### External Storage _GOTCHA_

Since writing this module in maybe 2018, and only testing it with SDK Version 28 and below, it has come to my attention that `Public External Storage` is deprecated in version 29. Nevertheless, if your device targets 29, you can still use external storage my modifying your `AndroidManifest.xml` file. This is a temporary work around so that all users (even those with 30+) can use this library's ability to access/write public external storage. However, I believe in August of 2021, all new apps submitted to Google Play must target SDK 30 or above to upload new builds. So for now, I am giving this temporary work around until I find a solution for SDK30+.

1. Open `../android/app/src/main/AndroidManifest.xml` and add `android:requestLegacyExternalStorage="true"`

```xml
<!-- example -->
<manifest ...>
  <application
    ...
    android:requestLegacyExternalStorage="true">
```

2. Open `../android/build.gradle` and make sure targetSdkVersion is 29 or below. If you target 30+, the added `android:requestLegacyExternalStorage="true"` will be ignored. So public external storage will only work for 28 and below. But if you target 29, public external storage will work for all devices.

```gradle
buildscript {
    ext {
        ...
        targetSdkVersion = 29
    }
```

_Note_ Since public external storage is being deprecated, if you use this library, you may have to find a work around to transitioning the public storage file(s) you create using this library somewhere else. I am currently working on (as of Feb 2021) on using `Content Providers`. I am not sure if this solution will work, but I figure I can support my buggy apps for the next few months by requesting legacy external storage for now. If this solution works, I hope to create a solution where data instantly will be copied from public external storage if there is no data in the content provider, and then use content provider exclusively after that.

#### Shared Preferences (internal app storage)

Some users may want to use Android `SharedPreferences` instead of Public External Storage. This has the benefit of not having to add the above Permissions prep work. For instance, if you use an extension, you may prefer this. Or maybe you add some settings that I don't know about where SharedPreferences will work for you. If this is the case, just add an optional Options object to the end of your calls like this:

```javascript
try {
  const loadedData = await SharedGroupPreferences.getItem(
    "savedData",
    appGroupIdentifier,
    { useAndroidSharedPreferences: true }
  );
  this.setState({ username: loadedData.name });
} catch (errorCode) {
  console.log(errorCode);
}
```

or

```javascript
try {
  await SharedGroupPreferences.setItem("savedData", data, appGroupIdentifier, {
    useAndroidSharedPreferences: true,
  });
  this.loadUsernameFromSharedStorage();
} catch (errorCode) {
  // errorCode 0 = There is no suite with that name
  console.log(errorCode);
}
```

Options are optional and currently only affect Android. No changes are needed to your code if you want your code to keep working as it did before updating to the current version.

## Extras because I'm Lazy

I've added extra functionality to this module that isn't related because it's it's a pain creating a new npm module and setting everything up.

```javascript
// This Android only script lets you check if another app is installed based on package name. The example below is for Facebook.
const facebookPackageName = "com.facebook.android";
try {
  const installed = await SharedGroupPreferences.isAppInstalledAndroid(
    facebookPackageName
  );
  console.log("Facebook is installed on this device");
} catch (err) {
  console.log("Facebook is not installed");
}
```
