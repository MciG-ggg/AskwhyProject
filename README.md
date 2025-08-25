# AskWhy Project

A comprehensive Android app monitoring system built with React Native and Expo, featuring a custom native module for enhanced app list management.

## 🚀 Features

- **Enhanced App List Management**: Custom `expo-applist` module that overcomes Android's 200-app limit
- **Smart System App Filtering**: Automatically excludes system apps, showing only user-installed applications
- **App Monitoring System**: Track and monitor user-installed applications
- **Modern UI**: Clean, flat design with React Native components
- **Cross-Platform**: Built with Expo for Android (iOS support ready)

## 📁 Project Structure

```
AskwhyProject/
├── apps/
│   └── AskWhy/                 # Main React Native app
├── packages/
│   └── expo-applist/           # Custom native module for app listing
└── README.md
```

## 🛠 Technical Highlights

### Custom Native Module (expo-applist)
- **Breakthrough Achievement**: Overcomes Android's default 200-app limitation
- **Multiple Retrieval Methods**: Uses 5 different Android APIs for comprehensive app discovery
- **Smart Filtering**: Distinguishes between system and user apps
- **Icon Support**: Extracts and caches app icons
- **Performance Optimized**: Efficient native implementation in Kotlin

### App Features
- Real-time app list with 400+ apps support
- User app filtering (excludes 250+ system apps automatically)
- App monitoring toggle functionality
- Flat design UI components
- Error handling and retry mechanisms

## 🚀 Getting Started

### Prerequisites
- Node.js (v16 or higher)
- npm or yarn
- Android Studio and Android SDK
- Expo CLI

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd AskwhyProject
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Build the native module**
   ```bash
   cd packages/expo-applist
   npm run build
   cd ../..
   ```

4. **Run the app**
   ```bash
   cd apps/AskWhy
   npx expo run:android
   ```

### Development Build Required
This project uses a custom native module, so you need to use a development build rather than Expo Go.

## 📱 App Capabilities

### App List Management
- Retrieves 400+ installed applications (vs standard 200 limit)
- Filters out 250+ system apps automatically
- Displays app names, package names, and icons
- Alphabetical sorting for better UX

### Monitoring System
- Toggle monitoring status for individual apps
- Persistent monitoring state management
- Visual indicators for monitored apps
- Bulk monitoring operations

## 🔧 Technical Implementation

### Native Module Architecture
The `expo-applist` module uses multiple Android APIs to ensure comprehensive app discovery:

1. `PackageManager.getInstalledPackages()`
2. `PackageManager.getInstalledApplications()`
3. `MATCH_UNINSTALLED_PACKAGES` flag
4. `MATCH_ALL` flag (API 23+)
5. Intent-based launcher app discovery

### Permissions
Required Android permissions:
- `QUERY_ALL_PACKAGES` - For comprehensive app list access

## 📊 Performance Metrics

- **App Discovery**: 400+ apps (2x improvement over standard limit)
- **System App Filtering**: 250+ system apps automatically excluded
- **User Apps**: 150+ user-installed apps displayed
- **Load Time**: Optimized native implementation for fast retrieval

## 🏗 Development

### Project Commands

```bash
# Install dependencies
npm install

# Build native module
cd packages/expo-applist && npm run build

# Run Android app
cd apps/AskWhy && npx expo run:android

# Run tests
npm test

# Lint code
npm run lint
```

### Module Development
The `expo-applist` module is built with:
- **Kotlin** for Android native implementation
- **TypeScript** for JavaScript interface
- **Expo Modules API** for seamless integration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- Built with [Expo](https://expo.dev)
- Uses [React Native](https://reactnative.dev)
- Custom native module development with Expo Modules API