# Development Setup

To use Regions Bridge in your Mod or Plugin, follow these steps to configure your environment.

## 1. Gradle Configuration

Add the following to your `build.gradle.kts`. Note that we use `compileOnly` because the Bridge and Hytale Server are provided by the runtime environment.

```kotlin
dependencies {
    // The Regions Bridge API
    compileOnly(files("libs/regions-bridge.jar"))
    
    // Official Hytale Server JAR (required for Mixins and Core API)
    compileOnly(files("libs/HytaleServer.jar"))
}
```

## 2. Server Installation

1.  Download the latest `regions-bridge.jar`.
2.  Place it in the `plugins/` directory of your Hytale Server.
3.  Start the server. You should see `[Regions-MixinBridge] Early mixin bootstrap is active.` in the console.

## 3. Runtime Verification

To safely check if the bridge is available at runtime before performing Mixin injections or Hook calls:

```java
public boolean isBridgeLoaded() {
    return System.getProperty("regions.mixin.bridge.active") != null;
}
```
