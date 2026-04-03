---
name: creating-files-or-classes
description: When creating files or large classes follow the instructions explained here
---

When creating a class or file that will contain a composable, always take into account these parameters:

1. **The entire file shouldn't be too big**: Limit the file growth in the range of ~200 lines (with a maximum spare of +50 lines), don't take into account the import packages lines
2. **If the class is getting too big divide**: If the file is about to reach the limit then subdivide the class or composable in child classes or subfiles

Keep classes' names simple