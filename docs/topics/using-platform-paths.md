# Using Platform Specific Paths

Getting a path to a file is different for each platform, and you will need to define how this works for each platform

```kotlin
var storageDir: String
```
> For this example, we are keeping this as a top level variable. 
> { style="note" }

## On Android
Getting a path on android involves invoking from `filesDir`/`cacheDir` from a `Context`.
```kotlin
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // for documents directory
    storageDir = filesDir.path

    // or caches directory
    storageDir = cacheDir.path
  }
}
```

## On Desktop (JVM)

This depends on where you want to save your files, but generally you should save your files in a user data directory.
Recommending to use [harawata's appdirs](https://github.com/harawata/appdirs) to get the platform specific app dir

```kotlin
storageDir = AppDirsFactory.getInstance()
  .getUserDataDir(PACKAGE_NAME, VERSION, ORGANISATION)
```

## On iOS & other Apple platforms
This depends on where you want to place your files. For most common use-cases, you will want either `NSDocumentDirectory` or `NSCachesDirectory`

KStore provides you a convenience extensions to resolve these for you

```kotlin
// for documents directory
storageDir = NSFileManager.defaultManager.DocumentDirectory?.relativePath

// or caches directory
storageDir = NSFileManager.defaultManager.CachesDirectory?.relativePath
```

> This is experimental API and may be removed in future releases
> {style="note"}

> `NSHomeDirectory()` _(though it works on the simulator)_ is **not** suitable for physical devices as the security policies on physical devices does not permit read/writes to this directory
> {style="warning"}


<seealso style="cards">
  <category ref="external">
    <a href="https://tanaschita.com/20221010-quick-guide-on-the-ios-file-system/">Learn how to work with files and directories when developing iOS applications</a>
  </category>
</seealso>