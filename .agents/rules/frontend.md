# Frontend rules

- Put each React component in its own file by default. Small components that are implementation details of another component may share its file.
- Keep each component's styles in its own `stylex.create()` call, except for shared themes.
- Break components into logical subcomponents with focused responsibilities.
- After imports, order each component file as: styles, extra data structures, props, then component code.
