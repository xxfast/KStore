# Using Platform Specific Paths

## Defining Your Own Directories

KStore does not provide a way to create directories for you. You will need to provide the directories where you want to save your files.
```kotlin
var files: Path
var cache: Path
```

```kotlin
val files: KStore<Pet> = storeOf(file = Path("$files/my_cats.json"))
val caches: KStore<Pet> = storeOf(file = Path("$cache/my_cats.json"))
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
    files = Path(context.filesDir)
    cache = Path(context.cacheDir)
  }
}
```

### On Desktop (JVM)

This depends on where you want to save your files, but generally you should save your files in a user data directory.
Recommending to use [harawata's appdirs](https://github.com/harawata/appdirs) to get the platform specific app dir

```kotlin
import java.nio.file.FileSystem

const val PACKAGE_NAME = "io.github.xxfast.kstore"
const val VERSION = "1.0"
const val ORGANISATION = "xxfast"

val filesDir = AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, VERSION, ORGANISATION)
val cacheDir = AppDirsFactory.getInstance().getUserCacheDir(PACKAGE_NAME, VERSION, ORGANISATION)
```

> Make sure to create those directories if they don't already exist. The store won't create them for you
> { style="note" }

```kotlin
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

with(SystemFileSystem) { if(!exists(files)) createDirectories(files) }

files = Path(filesDir)
cache = Path(cacheDir)
```

### On iOS & other Apple platforms
This depends on where you want to place your files. For most common use-cases, you will want either `NSDocumentDirectory` or `NSCachesDirectory`

KStore provides you a convenience extensions to resolve these for you

```kotlin
val fileManager:NSFileManager = NSFileManager.defaultManager
val documentsUrl: NSURL = fileManager.URLForDirectory(
  directory = NSDocumentDirectory,
  appropriateForURL = null,
  create = false,
  inDomain = NSUserDomainMask,
  error = null
)!!

val cachesUrl:NSURL = fileManager.URLForDirectory(
  directory = NSCachesDirectory,
  appropriateForURL = null,
  create = false,
  inDomain = NSUserDomainMask,
  error = null
)!!

files = Path(documentsUrl.path)
caches = Path(cachesUrl.path)
```

> `NSHomeDirectory()` _(though it works on the simulator)_ is **not** suitable for physical devices as the security policies on physical devices does not permit read/writes to this directory
> {style="warning"}


<seealso style="cards">
  <category ref="external">
    <a href="https://tanaschita.com/20221010-quick-guide-on-the-ios-file-system/">Learn how to work with files and directories when developing iOS applications</a>
  </category>
</seealso>
