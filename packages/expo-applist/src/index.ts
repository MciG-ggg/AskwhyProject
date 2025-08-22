// Reexport the native module. On web, it will be resolved to ExpoApplistModule.web.ts
// and on native platforms to ExpoApplistModule.ts
export { default } from './ExpoApplistModule';
export { default as ExpoApplistView } from './ExpoApplistView';
export * from  './ExpoApplist.types';
