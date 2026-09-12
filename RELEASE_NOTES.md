# What's New in v1.4.0

### New Features
- **Multi-Language Translation** - on-the-fly app translation via MyMemory API with 16 supported languages
- **Language Selector** - switch app language instantly from Settings
- **Complete Localizations** - French, German, Spanish, Japanese, Russian, and Chinese
- **Translation Disk Cache** - translated strings cached for offline use
- **Download Manager** - view, track, and cancel active downloads from a new bottom tab
- **Offline Detection** - animated banner warns when network is unavailable
- **Batch Update Tracking** - see succeeded/failed counts when updating all apps

### Improvements
- Polished top app bar and home bar layout
- Tab scroll content stays above bottom navigation
- Translation retry with corrected German and Spanish strings
- Support Us section hidden behind feature flag
- More Apps header links to darkmintis.dev

### Bug Fixes
- Fixed translation retry after failure
- Corrected German and Spanish format string escaping
- Hardened translation with disk cache fallback
- Language picker limited to supported locales

### Under the Hood
- TranslationRepository and TranslationService architecture
- LocalizationManager for dynamic string switching
- ContentLanguageDetector for automatic language detection
- Contributor credits script for GitHub releases
