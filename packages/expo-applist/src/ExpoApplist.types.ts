export interface AppInfo {
  name: string;
  packageName: string;
  iconPath?: string;
  isSystemApp: boolean;
}

export interface AppListError {
  code: string;
  message: string;
  nativeError?: any;
}