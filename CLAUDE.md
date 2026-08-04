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
./gradlew testAndroidHostTest                      # Android (com.android.kotlin.multiplatform.library host tests)
./gradlew jsTest / wasmJsTest / linuxX64Test
./gradlew macosArm64Test iosSimulatorArm64Test     # Apple (on macOS)

# Single test
./gradlew :kstore:desktopTest --tests "io.github.xxfast.kstore.KStoreTests"

# Public API surface
./gradlew apiCheck      # CI runs this first; fails if public API drifts from checked-in *.api files
./gradlew apiDump       # regenerate *.api after an INTENTIONAL public API change, then commit it
```

## Publishing

Published to **Maven Central via the Sonatype Central Portal** (central.sonatype.com) using the `com.vanniktech.maven.publish` plugin, configured once for all modules in the root `build.gradle.kts` `subprojects {}` block. OSSRH (`s01.oss.sonatype.org`) is dead — do not reintroduce it.

- CI `release` job (on push to `master`, macOS runner) runs `./gradlew publishToMavenCentral`, uploading all three modules as a **single Central Portal deployment**.
- Release is **manual**: the upload waits for a human to click *Publish* at central.sonatype.com. (Swap `publishToMavenCentral()` → `publishAndReleaseToMavenCentral()` in the root build to automate.)
- Credentials are Gradle properties, passed in CI as `ORG_GRADLE_PROJECT_*` env vars: `mavenCentralUsername`/`mavenCentralPassword` (Central Portal user token → GitHub secrets `MAVEN_CENTRAL_USERNAME`/`MAVEN_CENTRAL_PASSWORD`) and `signingInMemoryKey`/`signingInMemoryKeyPassword` (reuses the `GPG_KEY_SECRET`/`GPG_KEY_PASSWORD` secrets). For local publishing put these in `~/.gradle/gradle.properties`.
- Central versions are **immutable** — bump `version` in the root `build.gradle.kts` before re-releasing.
- `./gradlew publishToMavenLocal` (with a signing key set) is the dry-run; it stages full artifacts to `~/.m2`.

## Gotchas

- **`explicitApi()` is on** in every module — public declarations need explicit visibility + return types or compilation fails.
- **Binary-compatibility validator**: any public signature change requires `./gradlew apiDump` and committing the updated `*.api` files, or `apiCheck` fails in CI.
- The **Wasm Node.js canary pin** (`21.0.0-v8-canary...`, `--ignore-engines`) in the build files is intentional — don't "fix" it.
- `sample/` is a **git submodule** (NYTimes-KMP app), not part of the library build.
- Tests are written once in each module's `commonTest`; platform source sets mostly supply `actual` declarations.
- Docs live in `docs/` (Writerside), published to https://xxfast.github.io/KStore.
