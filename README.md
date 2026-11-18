# Aariz

Native Android AI chat by **Project Endroid** — companion to the [web app](https://wized2.github.io/Ai/).

## Features

- Modern chat UI (Compose · Material 3 · Lucide)
- Chat · web search · image modes
- Local history · light / dark / system theme
- Release builds: **minify + shrink resources**

## Install

Grab the latest **release APK** from [Releases](https://github.com/wized2/Aariz/releases).

## Build

```bash
./gradlew assembleRelease   # minified
./gradlew assembleDebug
```

## Releasing

Releases are automatic, like Fern. Every push to `main` runs tests, builds the minified APK, signs it with the release key, verifies the signature, and creates or updates the GitHub Release `v<versionName>` with the APK and a SHA-256 file.

To ship a new version, bump `versionName` and `versionCode` in `app/build.gradle.kts` in your PR. Merging it publishes the new release. Pushing a `v*` tag also publishes.

The official repo needs the secrets `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS` and `RELEASE_KEY_PASSWORD`; without them the release step stops instead of publishing a debug-signed APK. Forks build with the debug key.

## License

MIT
