import { AppInfo } from './ExpoApplist.types';
import ExpoApplistModule from "./ExpoApplistModule";

export { AppInfo, AppListError } from './ExpoApplist.types';

export function getApplist(): AppInfo[] {
  return ExpoApplistModule.getApplist();
}

export function getApplistAsync(): Promise<AppInfo[]> {
  return ExpoApplistModule.getApplistAsync();
}

export function getUserApps(): AppInfo[] {
  return ExpoApplistModule.getUserApps();
}

export function getUserAppsAsync(): Promise<AppInfo[]> {
  return ExpoApplistModule.getUserAppsAsync();
}