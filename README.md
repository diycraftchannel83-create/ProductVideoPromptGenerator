# Product Video Prompt Generator

Android-native creative studio for generating professional AI video prompts from product photos.

## Project identity
- Package: `com.winsmediatama.productvideopromptgenerator`
- Kotlin + Jetpack Compose + Material 3
- Minimum Android: API 26
- Compile/Target SDK: 35
- JDK: 17

## Phase 1 foundation
- Dark Studio dashboard
- Eight video categories
- Product photo picker and preview
- Project name and category selection
- Navigation placeholders for Prompt Library and Settings
- Indonesian and English app-name resources
- GitHub Actions debug build workflow

## Build
Open in Android Studio and sync Gradle, or run `gradle assembleDebug` with Gradle 8.11.1 and JDK 17.

AI Vision is not connected in Phase 1. Offline prompt generation and optional online AI analysis will be implemented in subsequent phases.