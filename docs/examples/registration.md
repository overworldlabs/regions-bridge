# Hook Registration Example

> [!IMPORTANT]
> Ensure you have followed the [Development Setup](../setup.md) before implementing hooks.

This guide is for developers of **Protection Plugins** (Claiming, Land Protection, etc.) who want to expose their permissions to other mods using the Regions Bridge.

## Registering your Hooks

You must publish your logic classes to the global registry so other mods (consumers) can query them via the Bridge.

```java
package com.yourname.protection;

import java.util.HashMap;
import java.util.Map;

public class YourPlugin extends ServerPlugin {

    @Override
    public void onEnable() {
        // Prepare the hook map
        Map<String, Object> registry = new HashMap<>();
        
        // Map your internal handler logic
        registry.put("regions.use.hook", new YourClaimHandler());
        
        // Publish to the global system properties
        // This key is the standard contract for Regions Bridge v1
        System.getProperties().put("regions.hook.registry", registry);
    }
}
```
