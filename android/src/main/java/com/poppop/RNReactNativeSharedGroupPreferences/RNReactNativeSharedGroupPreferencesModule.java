
package com.poppop.RNReactNativeSharedGroupPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.ReactApplicationContext;
// import com.facebook.react.bridge.ReactContextBaseJavaModule; // Removed
import com.facebook.react.bridge.ReactMethod;
// import com.facebook.react.bridge.Callback; // Removed, use Promise
import com.facebook.react.bridge.Promise; 
import com.facebook.react.bridge.ReadableMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.widget.Toast;



public class RNReactNativeSharedGroupPreferencesModule extends NativeRNGroupPreferencesSpec {

    public static final String NAME = "RNReactNativeSharedGroupPreferences";

  private final ReactApplicationContext reactContext;

  public RNReactNativeSharedGroupPreferencesModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  private SharedPreferences getSharedPreferences(String appGroup) {
    return reactContext.getApplicationContext().getSharedPreferences(appGroup, Context.MODE_PRIVATE);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void isAppInstalledAndroid(String packageName, final Callback callback) {
    PackageManager pm = reactContext.getPackageManager();
    try {
      pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
      callback.invoke(true);
    } catch (Exception e) {
      callback.invoke(false);
    }
  }

  @Override
  public void setItem(String key, String value, String appGroup, ReadableMap options, final Promise promise) {
    boolean useAndroidSharedPreferences = false;
    if (options.hasKey("useAndroidSharedPreferences")) {
      useAndroidSharedPreferences = options.getBoolean("useAndroidSharedPreferences");
    }

    if (useAndroidSharedPreferences) {
      SharedPreferences preferences = getSharedPreferences(appGroup);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(key, value);
      editor.apply();
      promise.resolve(null);
    } else {
        File extStore = Environment.getExternalStorageDirectory();
        String fileName = "data.json";

        try {
          File dir = new File(extStore.getAbsolutePath() + "/" + appGroup + "/");
          dir.mkdir();
          File myFile = new File(dir, fileName);
          myFile.createNewFile();
          FileOutputStream fOut = new FileOutputStream(myFile);
          OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
          myOutWriter.append(value);
          myOutWriter.close();
          fOut.close();
          promise.resolve(null);
        } catch (Exception e) {
          e.printStackTrace();
          promise.reject("0", e.getMessage());
        }
    }
  }

  @Override
  public void getItem(String key, String appGroup, ReadableMap options, final Promise promise) {
    boolean useAndroidSharedPreferences = false;
    if (options.hasKey("useAndroidSharedPreferences")) {
      useAndroidSharedPreferences = options.getBoolean("useAndroidSharedPreferences");
    }

    if (useAndroidSharedPreferences) {
      SharedPreferences preferences = getSharedPreferences(appGroup);
      String value = preferences.getString(key, null);
      promise.resolve(value);
    } else {
        File extStore = Environment.getExternalStorageDirectory();
        String fileName = "data.json";
        String path = extStore.getAbsolutePath() + "/" + appGroup + "/" + fileName;

        String s = "";
        String fileContent = "";
        try {
           File myFile = new File(path);
           FileInputStream fIn = new FileInputStream(myFile);
           BufferedReader myReader = new BufferedReader(
                   new InputStreamReader(fIn));

           while ((s = myReader.readLine()) != null) {
               fileContent += s + "";
           }
           myReader.close();
           promise.resolve(fileContent);
        } catch (IOException e) {
           promise.reject("0", e.getMessage());
        }
    }
  }
}
