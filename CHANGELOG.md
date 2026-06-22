# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- GitHub templates and community files
- Comprehensive documentation
- Enhanced README with visual elements
- Web-driven back press handling via `AndroidBridge.setBackHandled()` so pages can intercept back to close dialogs/drawers/sheets before WebView history or app exit
- System bar light/dark icon contrast now follows the device's day/night setting

### Changed
- Replaced `ThemeManager`-based auto status bar theme adaptation with transparent system bars styled via `DayNight` theme + `WindowInsetsControllerCompat`
- Upgraded Android Gradle Plugin to 9.2.1 and Gradle wrapper to 9.4.1

### Removed
- `ThemeManager` and `ENABLE_AUTO_THEME_ADAPTATION` config (status bar theming is now handled by the system DayNight theme instead of JS-driven color detection)
- Legacy `GITHUB_OPTIMIZATION.md`, `GITHUB_TAGS.md`, `GITIGNORE.md` docs

## [1.0.0] - 2025-01-XX

### Added
- 🎨 **Auto Theme Adaptation** - Status bar automatically adapts to website theme colors
- 🌐 **Smart URL Configuration** - One-line website setup with automatic host generation
- 📁 **Asset File Support** - Load local HTML files from app assets
- 🔗 **Chrome Custom Tabs** - External links open in beautiful custom tabs
- 📱 **JavaScript Bridge** - Full native-web communication API
- 🔐 **Biometric Authentication** - Fingerprint security integration
- 📂 **Advanced File Management** - Background downloads with progress tracking
- 🔔 **Smart Notifications** - System notifications with media controls
- 🎭 **Material Design UI** - Clean, modern interface
- ⚡ **Performance Optimized** - Hardware acceleration and efficient memory management
- 🛡️ **Enterprise Security** - Network security config and permission management
- 🧪 **Interactive Demo** - Built-in demo pages showcasing features

### Technical Features
- **Modular Architecture** - Clean separation of concerns
- **WebView 2025 Standards** - Latest WebView APIs and features
- **Android 10+ Support** - Modern Android compatibility
- **Java Implementation** - Maximum compatibility and performance
- **Edge-to-Edge Display** - Modern full-screen experience
- **Scoped Storage Compliance** - Android 10+ storage best practices
- **WorkManager Integration** - Reliable background task processing
- **Service Workers Support** - PWA compatibility
- **WebRTC Support** - Camera and microphone access
- **Geolocation API** - Location services integration

### Configuration Options
- Auto theme adaptation toggle
- JavaScript bridge enable/disable
- File operations control
- Biometric authentication settings
- Media notifications preferences
- Browser notifications support
- Splash screen customization

### Demo Features
- Theme color adaptation testing
- JavaScript bridge API showcase
- File upload/download examples
- Biometric authentication demo
- Device information access
- Notification system testing
- External link handling
- Media playback controls

### Security Features
- Network Security Config
- Runtime permission management
- Secure file sharing with FileProvider
- Biometric prompt integration
- Safe browsing enabled
- HTTPS enforcement

## [0.9.0] - 2025-01-XX (Beta)

### Added
- Initial WebView wrapper implementation
- Basic configuration system
- File management capabilities
- Permission handling system

### Fixed
- WebView loading issues
- Permission request handling
- File access permissions

## [0.1.0] - 2025-01-XX (Alpha)

### Added
- Project initialization
- Basic WebView setup
- Android Studio project structure
- Initial README documentation

---

## Legend

- 🎨 **UI/UX**: User interface and experience improvements
- 🔧 **Configuration**: Setup and configuration changes
- 🐛 **Bug Fix**: Bug fixes and error corrections
- ✨ **Feature**: New features and functionality
- 📚 **Documentation**: Documentation updates
- ⚡ **Performance**: Performance improvements
- 🔐 **Security**: Security enhancements
- 🧪 **Testing**: Testing improvements
- 🏗️ **Architecture**: Code structure changes
- 📱 **Platform**: Platform-specific updates

## Contributing

Found an issue or have a feature request? Please check our [Contributing Guidelines](CONTRIBUTING.md) and open an issue or pull request.

## Support

For support, please visit our [GitHub Discussions](https://github.com/MonsterTechnoGits/android-webview-wrapper/discussions) or check out the [documentation](README.md).

---

**Built with ❤️ by [Suman Dey](https://www.sumandey.com)**