# Using Platform Specific Paths

## Using Default Directory Provider

> This is experimental API and may be removed in future releases
> {style="note"}

`DefaultDirectories` provides a path to directories where you can store your files for each platform.

```kotlin
// For files directory
val files: KStore<Pet> = storeOf(file = Path("${DefaultDirectories.files}/my_cats.json"))

// For caches directory
val caches: KStore<Pet> = storeOf(file = Path("${DefaultDirectories.caches}/my_cats.json"))
```

This will resolve to the appropriate directory for each platform
// Generate a table of 3 columns, 6 rows
| Platform | Files directory | Caches directory |
|----------|-----------------|------------------|
| Android  | `context.filesDir` | `context.cacheDir` |
| iOS      | `NSDocumentDirectory` | `NSCachesDirectory` |
| Desktop (Mac)* | `/Users/<Account>/Library/Application Support/io.github.xxfast.kstore` | `/Users/<Account>/Library/Caches/io.github.xxfast.kstore` |
| Desktop (Windows)* | `C:\<Account>\ave\AppData\Local\xxfast\io.github.xxfast.kstore` | `C:\Users\ave\AppData\Local\xxfast\myapp\Cache\io.github.xxfast.kstore` |
| Desktop (Linux)* | `home/<Account>/.local/share/io.github.xxfast.kstore` | `/home/<Account>/.cache/io.github.xxfast.kstore` |

> * - via [harawata/appdirs](https://github.com/harawata/appdirs)

## Defining Your Own Directory Provider

If you want to put your files elsewhere, define your own directory provider for each platform.

```kotlin
var directories: DirectoryProvider
val files: KStore<Pet> = storeOf(file = Path("${directories.files}/my_cats.json"))
val caches: KStore<Pet> = storeOf(file = Path("${directories.cache}/my_cats.json"))
```
> For this example, we are keeping this as a top level variable. But do use your favorite DI framework instead
> { style="note" }

### On Android
Getting a path on android involves invoking from `filesDir`/`cacheDir` from a `Context`.
```kotlin
import kotlin.io.path.Path

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    directories = DirectoryProvider(
      files = Path(context.filesDir),
      cache = Path(context.cacheDir),
    )
  }
}
```

### On Desktop (JVM)

This depends on where you want to save your files, but generally you should save your files in a user data directory.
Recommending to use [harawata's appdirs](https://github.com/harawata/appdirs) to get the platform specific app dir

```kotlin
const val PACKAGE_NAME = "io.github.xxfast.kstore"
const val VERSION = "1.0"
const val ORGANISATION = "xxfast"

directories = DirectoryProvider(
  files = AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, VERSION, ORGANISATION),
  cache = AppDirsFactory.getInstance().getUserCacheDir(PACKAGE_NAME, VERSION, ORGANISATION),
)
```

> Make sure to create those directories if they don't already exist. The store won't create them for you
> { style="note" }

### On iOS & other Apple platforms
This depends on where you want to place your files. For most common use-cases, you will want either `NSDocumentDirectory` or `NSCachesDirectory`

KStore provides you a convenience extensions to resolve these for you

```kotlin
val fileManager:NSFileManager = NSFileManager.defaultManager
val documentsUrl: NSURL? = fileManager.URLForDirectory(
  directory = NSDocumentDirectory,
  appropriateForURL = null,
  create = false,
  inDomain = NSUserDomainMask,
  error = null
)

val cachesUrl:NSURL? = fileManager.URLForDirectory(
  directory = NSCachesDirectory,
  appropriateForURL = null,
  create = false,
  inDomain = NSUserDomainMask,
  error = null
)

val files = requireNotNull(documentsUrl?.path)
val caches = requireNotNull(cachesUrl?.path)

directories = DirectoryProvider(
  files = Path(files),
  caches = Path(caches)
)
```

> `NSHomeDirectory()` _(though it works on the simulator)_ is **not** suitable for physical devices as the security policies on physical devices does not permit read/writes to this directory
> {style="warning"}


<seealso style="cards">
  <category ref="external">
    <a href="https://tanaschita.com/20221010-quick-guide-on-the-ios-file-system/">Learn how to work with files and directories when developing iOS applications</a>
  </category>
</seealso>
