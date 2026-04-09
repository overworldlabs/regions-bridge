# Regions Bridge

A decoupled, lightweight bridge module for Hytale server plugins to interact with region protection systems (like Plots) without direct dependencies.

## Overview

Regions Bridge provides a reflection-based hook system. It allows low-level modules (like Mixins or native server components) to query protection rules stored in high-level plugins.

## How it Works

The bridge uses a global registry stored in `System.getProperties()` to decouple the implementation from the consumer.

1. **The Provider (e.g., Plots Plugin)**: Registers handlers for various actions.
2. **The Consumer (e.g., Mixins)**: Calls `HookResolver` to check if an action should be allowed or blocked.

## Installation

This bridge is intended to be used as a standalone `.jar` plugin on the server. Other plugins can then optionally interact with it.

### As a Dependency
Add the `regions-bridge.jar` to your project's build path (as a `compileOnly` or `provided` dependency) so you can access the `HookResolver` class during development.

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
