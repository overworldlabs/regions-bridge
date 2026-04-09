# Regions Bridge

A decoupled, lightweight bridge module for Hytale server plugins to interact with region protection systems (like Plots) without direct dependencies.

## Overview

Regions Bridge provides a reflection-based hook system. It allows low-level modules (like Mixins or native server components) to query protection rules stored in high-level plugins.

## How it Works

The bridge uses a global registry stored in `System.getProperties()` to decouple the implementation from the consumer.

1. **The Provider (e.g., Plots Plugin)**: Registers handlers for various actions.
2. **The Consumer (e.g., Mixins)**: Calls `HookResolver` to check if an action should be allowed or blocked.

## Installation

### As a Submodule
To include this in your project:
```bash
git submodule add https://github.com/overworldlabs/regions-bridge.git lib/regions-bridge
```

### Gradle Dependency
Add the following to your `build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":regions-bridge"))
}
```

## Usage

### Registration (In your Plugin)

Your protection plugin must register its hooks during initialization:

```java
Map<String, Object> registry = new HashMap<>();
registry.put("regions.use.hook", new MyUseHook());
registry.put("regions.command.hook", new MyCommandHook());

System.getProperties().put("regions.hook.registry", registry);
```

Each hook should implement the expected methods (e.g., `check`, `shouldBlockCommand`, etc.). The bridge uses reflection, so you don't need to implement a specific interface from this library, but the method signatures must match what `HookResolver` expects.

### Querying (In your Mixins/Logic)

To check for permissions:

```java
boolean allowed = HookResolver.check(
    playerUuid, 
    worldName, 
    x, y, z, 
    "BREAK_BLOCK"
);

if (!allowed) {
    HookResolver.notifyDenied(playerUuid, worldName, x, y, z, "BREAK_BLOCK");
    // Cancel the event
}
```

## Available Hooks

- `regions.use.hook`: Generic interaction and block checks.
- `regions.command.hook`: Command execution filtering.
- `regions.explosion.hook`: Control over TNT/explosion damage.
- `regions.spawn.hook`: Entity spawning restrictions.
- `regions.death.hook`: Death-related flags (like keep inventory).
- `regions.durability.hook`: Item durability loss prevention.
