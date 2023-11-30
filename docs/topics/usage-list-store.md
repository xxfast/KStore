# Using List Store
> This is experimental API and may be removed in future releases
{style="warning"}

KStore provides you with some convenient extensions to manage stores that contain lists.

## Create a list store
`listStoreOf` is the same as `storeOf`, but defaults to empty list instead of `null`
```kotlin
val listStore: KStore<List<Pet>> = listStoreOf("path/to/file") 
```

## Get values
```kotlin
val pets: List<Cat> = listStore.getOrEmpty()
val pet: Cat = store.get(0)
```
or observe values
```kotlin
val pets: Flow<List<Cat>> = listStore.updatesOrEmpty
```

## Add or remove elements
```kotlin
listStore.plus(cat)
listStore.minus(cat)
```

## Map elements
```kotlin
listStore.map { cat -> cat.copy(cat.age = cat.age + 1) }
listStore.mapIndexed { index, cat -> cat.copy(cat.age = index) }
```

