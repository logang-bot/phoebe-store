# Permission Handling

## Overview

Runtime permission requests follow an MVVM pattern: the **ViewModel** owns the permission dialog queue state, and the **Composable screen** handles the Android system interactions (launching the permission request and showing the dialog).

Two permissions are requested:
- `CAMERA` — triggered inside specific screens when the user taps "Take photo".
- `POST_NOTIFICATIONS` — requested once on app start via a global handler.

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

---

## POST_NOTIFICATIONS (app-start, global)

`POST_NOTIFICATIONS` is required on Android 13+ (API 33) to show sync notifications. It is requested once on app start via two classes in `presentation/navigation/`:

### `NotificationPermissionViewModel`

Owns all persisted decision state via `SharedPreferences("notification_prefs")`:

| Key | Purpose |
|---|---|
| `notification_requested` | Set to `true` after the first system dialog is launched |
| `notification_skipped` | Set to `true` when the user explicitly taps Skip |

**Decision logic (`onStart`):**

```
Permission already granted  →  do nothing
User previously skipped     →  do nothing
Never requested before      →  triggerRequest = true (launch system dialog)
canShowRationale = true     →  triggerRequest = true (user denied once, try again)
Permanently declined        →  showSettingsDialog = true (custom dialog)
```

Permanent denial is identified by: `hasRequested = true` AND `canShowRationale = false` AND permission not granted.

### `NotificationPermissionHandler` composable

Handles all Android interactions — placed at the root of `AppNavigation` so it runs on every app start:

```
LaunchedEffect(Unit)
  ├── checkSelfPermission(POST_NOTIFICATIONS)
  ├── shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)
  └── viewModel.onStart(isGranted, canShowRationale)

LaunchedEffect(triggerRequest)
  └── if true → permissionLauncher.launch(POST_NOTIFICATIONS)
        └── result → viewModel.onPermissionResult(isGranted, canShowRationale)

if (showSettingsDialog)
  └── NotificationPermissionDialog(onSkip, onGoToSettings)
```

### `NotificationPermissionDialog`

Custom dialog shown only in the permanently-declined case. Two buttons:

| Button | Action |
|---|---|
| Skip | `viewModel.onSkip()` — sets `notification_skipped = true`, not shown again |
| Open Settings | `viewModel.onSettingsDismissed()` + `activity.openAppSettings()` |

`onDismissRequest` maps to Skip so pressing back or tapping outside behaves the same as Skip.

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
