# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

KStore is a Kotlin Multiplatform library for persisting serializable objects, published to Maven Central under `io.github.xxfast`. Three modules:
- `kstore` — platform-agnostic core: `KStore<T>` (concurrency + caching) and the `Codec<T>` persistence interface. New backends are added by implementing a `Codec` + a `storeOf(...)` factory; the core doesn't change.
- `kstore-file` — file-system stores (Android, Apple, JVM "desktop", JS/Node, Linux, Windows).
- `kstore-storage` — browser `localStorage` stores (jsBrowser, wasmJsBrowser only).

## Commands

Use the Gradle wrapper (`./gradlew`). Build requires JDK 17.

```bash
# Per-platform test suites (mirror the CI matrix)
./gradlew desktopTest                              # JVM
./gradlew testDebugUnitTest                        # Android (AGP 9 dropped testReleaseUnitTest)
./gradlew jsTest / wasmJsTest / linuxX64Test
./gradlew macosArm64Test iosSimulatorArm64Test     # Apple (on macOS)

# Single test
./gradlew :kstore:desktopTest --tests "io.github.xxfast.kstore.KStoreTests"

# Public API surface
./gradlew apiCheck      # CI runs this first; fails if public API drifts from checked-in *.api files
./gradlew apiDump       # regenerate *.api after an INTENTIONAL public API change, then commit it
```

## Gotchas

- **`explicitApi()` is on** in every module — public declarations need explicit visibility + return types or compilation fails.
- **Binary-compatibility validator**: any public signature change requires `./gradlew apiDump` and committing the updated `*.api` files, or `apiCheck` fails in CI.
- The **Wasm Node.js canary pin** (`21.0.0-v8-canary...`, `--ignore-engines`) in the build files is intentional — don't "fix" it.
- `sample/` is a **git submodule** (NYTimes-KMP app), not part of the library build.
- Tests are written once in each module's `commonTest`; platform source sets mostly supply `actual` declarations.
- Docs live in `docs/` (Writerside), published to https://xxfast.github.io/KStore.
