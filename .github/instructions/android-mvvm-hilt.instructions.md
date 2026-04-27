---
description: "Use when working on Android Kotlin features in TravelHub, including Compose UI, ViewModel, repository, use case, and dependency injection with Hilt and MVVM architecture."
name: "TravelHub Android MVVM + Hilt"
applyTo: "app/src/main/java/**/*.kt"
---
# TravelHub Android Structure And Architecture

Follow these rules when creating or editing Kotlin source files in this project.

## Package Structure

Use package root: `com.mobile.travelhub`.

Organize code by responsibility:
- `ui/screens`: Jetpack Compose screens and UI-only logic.
- `ui/theme`: Theme, typography, colors, and design tokens.
- `viewmodels`: `@HiltViewModel` classes, UI state, and UI events.
- `usecase`: Single-purpose business actions called by ViewModels.
- `data`: Repository and data-source implementations.
- `models`: DTOs, domain models, and serialization helpers.
- `di`: Hilt modules and providers (create this package when adding module bindings).

## MVVM Rules

- Keep UI state in ViewModel, exposed via `StateFlow` or immutable state objects.
- Keep Compose screens stateless where possible, with data and callbacks from ViewModel.
- Put business logic in `usecase` or `data` layers, not inside Compose screens.
- ViewModel can coordinate use cases and repositories, but UI should not call repositories directly.

## Hilt Rules

- Application class must stay annotated with `@HiltAndroidApp`.
- Android entry points (Activity/Fragment) must use `@AndroidEntryPoint` when injection is needed.
- ViewModels that need DI must use `@HiltViewModel` and constructor `@Inject`.
- Prefer constructor injection first.
- Add `@Module` + `@InstallIn(...)` bindings/providers in `di` package for interfaces or third-party dependencies.
- Scope dependencies intentionally (`@Singleton` only when truly app-wide).

## Implementation Checklist

- Place each new class in the correct package by layer.
- Wire dependencies through Hilt instead of manual object creation.
- Keep naming consistent: `XxxViewModel`, `XxxUseCase`, `XxxRepository`.
- Avoid leaking Android UI concerns into `data` and `usecase` layers.
