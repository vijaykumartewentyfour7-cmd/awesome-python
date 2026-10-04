# Awesome Python (Android)

An opinionated, beautifully designed native Android application bringing the definitive guide to the best Python frameworks, libraries, software, and tools to Android devices.

## Features

- **Comprehensive Library Directory**: Over 500+ curated Python libraries across 74 specialized categories and domains.
- **Fast Live Search & Filter**: Real-time searching across library names, descriptions, categories, subcategories, and PyPI package names.
- **Domain & Category Explorer**: Browse by domain (AI & ML, Web Development, Data & Science, DevOps, CLI & GUI, Security, etc.) with in-depth maintainer guides and curation notes.
- **One-Tap Installation Commands**: Instantly copy `pip install <package>` commands to your clipboard.
- **Offline Bookmarks (SQLite Persistence)**: Save favorite libraries locally for fast offline access.
- **Rich Project Details & Direct Links**: Access official documentation, GitHub repositories, PyPI package identifiers, and share libraries with peers.
- **Material 3 Design**: Fully adaptive layout, dark & light themes styled with Python's signature blue and gold palette, edge-to-edge support, and responsive components.

## Architecture

- **UI**: 100% Jetpack Compose with Material Design 3.
- **Architecture**: MVVM with Kotlin Coroutines and StateFlow.
- **Local Persistence**: Android SQLite database for saving bookmarked libraries.
- **Build System**: Android Gradle Plugin (AGP) with Kotlin DSL.
