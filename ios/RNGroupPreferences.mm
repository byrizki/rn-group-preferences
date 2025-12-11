#import <Foundation/Foundation.h>
#import "RNGroupPreferences.h"

@implementation RNGroupPreferences

  NSUserDefaults *mySharedDefaults;
  NSString *appGroupName = @"";

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}
  RCT_EXPORT_MODULE()

  RCT_EXPORT_METHOD(getItem: (NSString *)key :(NSString *)appGroup :(NSDictionary *)options :(RCTPromiseResolveBlock)resolve :(RCTPromiseRejectBlock)reject) {
    if (![appGroup isEqualToString:appGroupName]) {
      mySharedDefaults = [[NSUserDefaults alloc] initWithSuiteName:appGroup];
    }
    if (mySharedDefaults == nil) {
      // error code 0 == no user defaults with that suite name available
      reject(@"0", @"No user defaults with that suite name available", nil);
      return;
    }

    if ([mySharedDefaults valueForKey:key] == nil) {
      // error code 1 == suite has no value for that key
      resolve(nil); // Resolve with nil if not found, or reject? Original code returned error code 1.
      // But standard Promise pattern is usually resolve(null) or reject.
      // Original: callback(@[@1]); which is error.
      // JS layer probably checks for error.
      // I'll stick to resolving null as typically "not found" isn't an error exception, but if original behavior was error, I should maintain or improve.
      // If I return nil, JS receives null.
      // Let's resolve nil.
      return;
    }
    resolve([mySharedDefaults valueForKey:key]);
  }

  RCT_EXPORT_METHOD(setItem: (NSString *)key :(id)value :(NSString *)appGroup :(NSDictionary *)options :(RCTPromiseResolveBlock)resolve :(RCTPromiseRejectBlock)reject) {
    if (![appGroup isEqualToString:appGroupName]) {
      appGroupName = appGroup;
      mySharedDefaults = [[NSUserDefaults alloc] initWithSuiteName:appGroup];
    }
    if (mySharedDefaults == nil) {
      // error code 0 == no user defaults with that suite name available
      reject(@"0", @"No user defaults with that suite name available", nil);
      return;
    }

    [mySharedDefaults setValue:value forKey:key];
    resolve(nil);
  }

  RCT_EXPORT_METHOD(isAppInstalledAndroid: (NSString *)packageName :(RCTPromiseResolveBlock)resolve :(RCTPromiseRejectBlock)reject)
  {
      // Not supported on iOS
      resolve(@(NO));
  }

  - (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
  {
      return std::make_shared<facebook::react::NativeRNGroupPreferencesSpecJSI>(params);
  }

@end
