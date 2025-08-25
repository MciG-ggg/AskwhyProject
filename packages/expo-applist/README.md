# expo-applist

A powerful Expo native module for retrieving comprehensive Android app lists, overcoming the standard 200-app limitation.

## 🚀 Key Features

- **Breakthrough Capability**: Retrieves 400+ apps (vs standard 200 limit)
- **Smart Filtering**: Automatically distinguishes system vs user apps
- **Multiple Retrieval Methods**: Uses 5 different Android APIs for maximum coverage
- **Icon Support**: Extracts and caches app icons
- **Performance Optimized**: Native Kotlin implementation for speed
- **TypeScript Support**: Full type definitions included

## 📊 Performance Metrics

- **Total Apps Retrieved**: 400+ applications
- **System Apps Filtered**: 250+ automatically excluded
- **User Apps Displayed**: 150+ user-installed applications
- **API Coverage**: 5 different Android methods for comprehensive discovery

## 🛠 Technical Implementation

### Multiple Discovery Methods
1. `PackageManager.getInstalledPackages()` - Standard package discovery
2. `PackageManager.getInstalledApplications()` - Application-focused discovery
3. `MATCH_UNINSTALLED_PACKAGES` - Include disabled packages
4. `MATCH_ALL` (API 23+) - Comprehensive package matching
5. Intent-based launcher discovery - Find launchable apps

### Smart System App Detection
Uses multiple criteria to identify system apps:
- `ApplicationInfo.FLAG_SYSTEM`
- `ApplicationInfo.FLAG_UPDATED_SYSTEM_APP`
- Package installation source analysis

## 📱 API Reference

### Functions

#### `getApplistAsync(): Promise<AppInfo[]>`
Retrieves all installed applications (system + user apps).

```typescript
import { getApplistAsync } from 'expo-applist';

const allApps = await getApplistAsync();
console.log(`Found ${allApps.length} total apps`);
```

#### `getUserAppsAsync(): Promise<AppInfo[]>`
Retrieves only user-installed applications (system apps excluded).

```typescript
import { getUserAppsAsync } from 'expo-applist';

const userApps = await getUserAppsAsync();
console.log(`Found ${userApps.length} user apps`);
```

#### `getApplist(): AppInfo[]`
Synchronous version of `getApplistAsync()`.

#### `getUserApps(): AppInfo[]`
Synchronous version of `getUserAppsAsync()`.

### Types

#### `AppInfo`
```typescript
interface AppInfo {
  name: string;           // Display name of the app
  packageName: string;    // Android package name
  iconPath?: string;      // Path to cached icon file
  isSystemApp: boolean;   // Whether this is a system app
}
```

## 🔧 Installation

### Prerequisites
- Expo SDK 50+
- Development build (custom native modules not supported in Expo Go)

### Add to your project

```bash
npm install expo-applist
```

### Android Configuration

Add required permission to your `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"/>
```

### Build Configuration

This module requires a development build:

```bash
npx expo run:android
```

## 💡 Usage Examples

### Basic App List
```typescript
import React, { useEffect, useState } from 'react';
import { getApplistAsync, AppInfo } from 'expo-applist';

export function AppList() {
  const [apps, setApps] = useState<AppInfo[]>([]);

  useEffect(() => {
    loadApps();
  }, []);

  const loadApps = async () => {
    try {
      const allApps = await getApplistAsync();
      const userApps = allApps.filter(app => !app.isSystemApp);
      setApps(userApps);
    } catch (error) {
      console.error('Failed to load apps:', error);
    }
  };

  return (
    // Your UI implementation
  );
}
```

### Advanced Filtering
```typescript
import { getApplistAsync } from 'expo-applist';

const loadAppsWithStats = async () => {
  const allApps = await getApplistAsync();
  
  const userApps = allApps.filter(app => !app.isSystemApp);
  const systemApps = allApps.filter(app => app.isSystemApp);
  
  console.log(`Total: ${allApps.length}`);
  console.log(`User: ${userApps.length}`);
  console.log(`System: ${systemApps.length}`);
  
  return {
    all: allApps,
    user: userApps,
    system: systemApps
  };
};
```

## 🔍 Troubleshooting

### Common Issues

#### "Property 'getApplistAsync' doesn't exist"
- Ensure you're using a development build, not Expo Go
- Verify the module is properly installed: `npm list expo-applist`
- Clean and rebuild: `npx expo run:android --clear`

#### Limited app results
- Add `QUERY_ALL_PACKAGES` permission to AndroidManifest.xml
- Ensure target SDK is properly configured
- Check device Android version compatibility

#### Permission denied errors
- Verify `QUERY_ALL_PACKAGES` permission is declared
- For Android 11+, this permission may require special approval for Play Store apps

## 🏗 Development

### Building the Module

```bash
# Install dependencies
npm install

# Build TypeScript
npm run build

# Prepare module
npm run prepare
```

### Testing

```bash
# Run in example app
cd example
npx expo run:android
```

## 📋 Requirements

- **Android**: API 21+ (Android 5.0+)
- **Expo**: SDK 50+
- **React Native**: 0.70+
- **Development Build**: Required (not compatible with Expo Go)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with the example app
5. Submit a pull request

## 📄 License

MIT License - see LICENSE file for details.

## 🙏 Acknowledgments

- Built with [Expo Modules API](https://docs.expo.dev/modules/overview/)
- Inspired by the need to overcome Android's app discovery limitations
- Thanks to the React Native and Expo communities
