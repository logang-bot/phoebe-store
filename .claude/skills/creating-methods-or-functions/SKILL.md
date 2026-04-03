---
name: creating-methods-or-functions
description: When creating new methods or functions follow the guidelines described here
---

When create a method, function take into consideration these guidelines

1. **The method or function shouldn't be too big**: Limit the method growth in the range of ~10 lines (with a maximum spare of +5 lines)
2. **The number of parameters should not be too big**: Limit the number of parameters to a maximum of three, if you consider it needs more consider creating data classes to hold all the rest of the parameters
3. **Group the parameters in a semantic way**: If a new data class is needed to hold the rest of the parameters please group them in a way they are related.
4. **Make method's or function's names clear in its purpose** Since the size of each method or function is limited its function must be explained by its name rather than by any comment
5. **Document the method or function only if it is too complex to understand** If the method contains various calls to other methods or if the parameters are a bit tricky to understand then document the method or function

All these rules doesn't apply to composables