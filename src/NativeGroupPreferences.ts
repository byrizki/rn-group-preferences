import type { TurboModule } from "react-native";
import { TurboModuleRegistry } from "react-native";

export interface Spec extends TurboModule {
  getItem(key: string, appGroup: string, options: Object): Promise<any>;
  setItem(
    key: string,
    value: string,
    appGroup: string,
    options: Object
  ): Promise<void>;
  isAppInstalledAndroid(packageName: string): Promise<boolean>;
}

export default TurboModuleRegistry.getEnforcing<Spec>("RNGroupPreferences");
