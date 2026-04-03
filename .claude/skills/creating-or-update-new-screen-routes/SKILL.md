---
name: creating-or-update-new-screen-routes
description: When creating or updating new screen routes in any navigation composable
---

When create a new route to a new screen or when an existing route is being created follow these guidelines 

1. **ViewModels initialization**: The initialization of viewModels for any screen needs to be placed inside the corresponding screen, no viewModel should be initialized in the navigation file
2. **LaunchedEffect or any state logic initialization**: The use of any state or launchedEffect logic should be placed also inside the corresponding screen if possible.