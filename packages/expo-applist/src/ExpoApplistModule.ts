import { NativeModule, requireNativeModule } from 'expo';
import { AppInfo } from './ExpoApplist.types';

declare class ExpoApplistModule extends NativeModule {
  getApplist: () => AppInfo[];
  getApplistAsync: () => Promise<AppInfo[]>;
  getUserApps: () => AppInfo[];
  getUserAppsAsync: () => Promise<AppInfo[]>;
}

export default requireNativeModule<ExpoApplistModule>('ExpoApplist');