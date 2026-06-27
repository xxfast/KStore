# Using List Store
> This is experimental API and may be removed in future releases
{style="note"}

KStore provides you with some convenient extensions to manage stores that contain lists.

## Create a list store
`listStoreOf` is the same as `storeOf`, but defaults to empty list instead of `null`
```kotlin
val listStore: KStore<List<Pet>> = listStoreOf(file = Path("path/to/file")) 
```

## Get values
```kotlin
val pets: List<Pet> = listStore.getOrEmpty()
val pet: Pet? = listStore.get(0)
```
or observe values
```kotlin
val pets: Flow<List<Pet>> = listStore.updatesOrEmpty
```

## Add or remove elements
```kotlin
listStore.plus(pet)
listStore.minus(pet)
```

## Map elements
```kotlin
listStore.map { pet -> pet.copy(age = pet.age + 1) }
listStore.mapIndexed { index, pet -> pet.copy(age = index) }
```

