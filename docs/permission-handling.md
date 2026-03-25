# Permission Handling

## Overview

Runtime permission requests follow an MVVM pattern: the **ViewModel** owns the permission dialog queue state, and the **Composable screen** handles the Android system interactions (launching the permission request and showing the dialog).

Currently only `CAMERA` is requested. It is triggered in two screens:
- `CreateStoreScreen` — to take a logo or cover photo for a store.
- `CreateProductScreen` — to take a product image.

---

## Components

### `PermissionDialog` (`ui/common/PermissionDialog.kt`)

A reusable `@Composable` `AlertDialog` that renders a rationale or permanent-decline message for any permission.

| Prop | Type | Purpose |
|---|---|---|
| `permissionTextProvider` | `PermissionTextProvider` | Supplies the body text string resource ID |
| `isPermanentlyDeclined` | `Boolean` | Switches between rationale and "go to settings" copy |
| `onDismiss` | `() -> Unit` | Dismiss without action |
| `onOkClick` | `() -> Unit` | Re-request permission (shown when not permanently declined) |
| `onGoToAppSettingsClick` | `() -> Unit` | Open app settings (shown when permanently declined) |

Button label and body copy are resolved from string resources via `stringResource()`:

```
isPermanentlyDeclined = false  →  "OK" button  +  rationale copy
isPermanentlyDeclined = true   →  "Grant permission" button  +  settings-redirect copy
```

### `PermissionTextProvider` interface

```kotlin
interface PermissionTextProvider {
    @StringRes
    fun getDescription(isPermanentlyDeclined: Boolean): Int
}
```

Each permission has its own implementation returning the appropriate `@StringRes` ID. This keeps display logic out of the ViewModel and dialog composable.

| Implementation | Permission |
|---|---|
| `CameraPermissionTextProvider` | `Manifest.permission.CAMERA` |

String resources used:

| Resource | Value |
|---|---|
| `R.string.camera_permission_rationale` | Rationale shown on first/subsequent denials |
| `R.string.camera_permission_permanently_declined` | Copy shown after permanent denial |

### `openAppSettings()` (`ui/common/ActivityExtensions.kt`)

Extension on `Activity` that opens the app's system settings page so the user can manually grant a permanently declined permission.

### ViewModel queue state

Each screen that requires camera access owns its own `visiblePermissionDialogQueue`:

```kotlin
val visiblePermissionDialogQueue = mutableStateListOf<String>()
```

A `SnapshotStateList` of permission strings that currently need a dialog shown. Compose automatically recomposes when it changes.

| Method | Behaviour |
|---|---|
| `onPermissionResult(permission, isGranted)` | If denied and not already queued, adds the permission string to the queue |
| `dismissDialog()` | Removes the first item in the queue |

Implemented in: `CreateStoreViewModel`, `CreateProductViewModel`.

### Screen — system interactions

The screen owns the `ActivityResultLauncher`s that interact with the Android system:

```
cameraPermissionLauncher   →   RequestPermission contract
cameraLauncher             →   TakePicture contract
```

**Permission check flow (on "Take photo" tap):**

```
User taps "Take photo"
    │
    ├─ CAMERA granted?  ──Yes──►  create file URI via FileProvider
    │                              cameraLauncher.launch(uri)
    │
    └─ No  ──────────────────►  cameraPermissionLauncher.launch(CAMERA)
                                        │
                                        └─ Result callback
                                               │
                                               └─ viewModel.onPermissionResult(permission, isGranted)
                                                       │
                                                       └─ denied?  ──►  added to visiblePermissionDialogQueue
```

**Dialog rendering:**

```kotlin
dialogQueue.reversed().forEach { permission ->
    PermissionDialog(
        permissionTextProvider = when (permission) {
            Manifest.permission.CAMERA -> CameraPermissionTextProvider()
            else -> return@forEach
        },
        isPermanentlyDeclined = !activity.shouldShowRequestPermissionRationale(permission),
        onDismiss = viewModel::dismissDialog,
        onOkClick = {
            viewModel.dismissDialog()
            cameraPermissionLauncher.launch(permission)
        },
        onGoToAppSettingsClick = {
            viewModel.dismissDialog()
            activity.openAppSettings()
        }
    )
}
```

`shouldShowRequestPermissionRationale()` returns `false` both before the permission has ever been requested **and** after the user has permanently declined it. The dialog is only shown after a failed request (i.e., when the queue is non-empty), so `false` at that point reliably means permanent denial.

### Photo URI

The capture URI is created in the screen (not the ViewModel) since it requires a `Context`. All image files are stored in `filesDir` (not `cacheDir`) to prevent eviction:

```kotlin
val file = File(context.filesDir, "product_${System.currentTimeMillis()}.jpg")
val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
```

`FileProvider` is declared in `AndroidManifest.xml` with paths defined in `res/xml/file_paths.xml`. Both `cache-path` and `files-path` entries are present.

### Gallery URI persistence

Gallery `content://media/...` URIs from `PickVisualMedia` are temporary — the permission expires after the picker is dismissed. To avoid images disappearing after navigation, gallery picks are immediately copied to `filesDir` on `Dispatchers.IO`:

```kotlin
val galleryLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
    uri?.let { sourceUri ->
        scope.launch(Dispatchers.IO) {
            val dest = File(context.filesDir, "product_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { it.copyTo(dest.outputStream()) }
            viewModel.onImageCaptured(Uri.fromFile(dest).toString())
        }
    }
}
```

The stored URI is a `file://` path that Coil resolves natively, ensuring images remain visible after the app is backgrounded or the user navigates away and back.

---

## Adding a New Permission

1. Add the `<uses-permission>` declaration to `AndroidManifest.xml`.
2. Create a new `PermissionTextProvider` implementation returning rationale and permanent-decline `@StringRes` IDs.
3. Add the string resources to `res/values/strings.xml`.
4. In the screen that needs the permission, add a `RequestPermission` launcher and a check before the action.
5. Add a `when` branch in the dialog-rendering loop mapping the `Manifest.permission.*` constant to the new provider.

---

## String Resources

All user-visible strings for the permission UI live in `res/values/strings.xml`:

| Key | Usage |
|---|---|
| `permission_required` | Dialog title |
| `permission_grant` | Confirm button when permanently declined |
| `permission_ok` | Confirm button when rationale is shown |
| `camera_permission_rationale` | Camera body text — rationale case |
| `camera_permission_permanently_declined` | Camera body text — permanent-decline case |
