---
name: run-arch-tests
description: Run Konsist architecture tests and Harmonize iOS architecture tests. Use to verify code follows project conventions after structural changes.
---

# Run Architecture Tests

## Reference Standards

- Verification details and failure interpretation: `.agents/standards/verification.md`

## When to Use

After any changes that affect:
- Module structure or dependencies
- Class/function naming
- DI annotations (`@ContributesBinding`, `@CircuitInject`, `@Inject`)
- New files (verify they follow naming and placement conventions)
- Test files (verify they follow test conventions)

## Konsist (Kotlin)

```bash
./gradlew :konsist:test
```

17 test classes organized in 5 categories:

| Category | Tests | What They Enforce |
|----------|-------|-------------------|
| `architecture/` | LayerDependency, CircularDependency, ForbiddenPatterns | Import direction, no circular deps, banned API usage |
| `circuit/` | CircuitConventions, NamingConventions | Screen/Presenter/Event naming, sealed class (not interface), placement |
| `core/` | CodeHygiene, DependencyInjection, PackageConventions, VisibilityConventions | No wildcards, @ContributesBinding coverage, package naming, minimal visibility |
| `layers/` | ApiLayer, DataLayer, DomainLayer, PresentationLayer | Immutable models, serialization placement, DI wiring, UiState conventions |
| `testing/` | TestDoubleConventions, TestFileNaming, TestModuleCoverage, UiTestConventions | Fake naming, test file placement, Robot pattern compliance |

## Harmonize (iOS/Swift)

```bash
swift test --package-path iosApp/ArchitectureCheck
```

29 tests enforce Swift view conventions, test module organization, and iOS architectural patterns.

## Interpreting Failures

Konsist failures include:
- The rule name (e.g., "Presenters must not depend on repositories directly")
- The violating class/file path
- A message explaining what needs to change

Common fixes:
- **Layer dependency violation**: Move the import or the class to the correct module
- **Naming violation**: Rename to follow the `{Feature}{Type}` pattern
- **Missing annotation**: Add `@ContributesBinding`, `@CircuitInject`, or `@Inject`
- **Test coverage gap**: Create the missing test file following Robot pattern
