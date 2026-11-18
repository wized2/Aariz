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

1. Bump `versionName` / `versionCode` in `app/build.gradle.kts`.
2. `git tag v<versionName> && git push origin v<versionName>`.

The **Release** workflow checks the tag matches the version, runs tests, builds the minified APK, verifies its signature, and publishes it with a SHA-256 file. Tags with a hyphen (for example `v1.1.0-beta.1`) become pre-releases. It needs the repository secrets `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS` and `RELEASE_KEY_PASSWORD`; without them it stops instead of publishing a debug-signed APK. Regular CI (`build.yml`) only tests and builds.

## License

MIT
