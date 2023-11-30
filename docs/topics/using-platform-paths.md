# Using Platform Specific Paths

Getting a path to a file is different for each platform, and you will need to define how this works for each platform
```kotlin
var appDir: String
```
> For this example, we are keeping this as a top level variable. 
{ style="note" }

## On Android
Getting a path on android involves invoking from `filesDir` from a `Context`.
```kotlin
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appDir = filesDir.path
  }
}
```

## On iOS & other Apple platforms
To get a path on iOS, you can use `NSHomeDirectory`.
```kotlin
appDir = "${NSHomeDirectory()}/$id.json"
```

## On Desktop (JVM)

This depends on where you want to save your files, but generally you should save your files in a user data directory.
Recommending to use [harawata's appdirs](https://github.com/harawata/appdirs) to get the platform specific app dir

```kotlin
appDir = AppDirsFactory.getInstance()
  .getUserDataDir(PACKAGE_NAME, VERSION, ORGANISATION)
```
