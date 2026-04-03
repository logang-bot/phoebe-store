---
name: creating-composables
description: When create a new function that is marked as composable
---

When create a composable take into consideration these guidelines

1. **Always put a modifier as the first optional parameter**: If an optional modifier parameter is needed always put as the first optional one
2. **Split into child composables**: The size of a composable function is not limited but try to split into small composables functions
3. **Identify business logic**: If you identify a business logic inside a composable function move it to a new ViewModel file and link it to the composable
