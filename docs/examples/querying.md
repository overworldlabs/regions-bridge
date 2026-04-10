# Consuming Permissions Example

> [!IMPORTANT]
> Ensure you have followed the [Development Setup](../setup.md) before calling bridge methods.

This guide is for developers of **Content Mods** (Magic, Weapons, Tools) who need to respect area protections without adding dependencies on every individual protection plugin.

## Using the HookResolver

The `HookResolver` is the central point for querying permissions. It automatically checks all registered protection plugins on the server.

### Example: A Destruction Spell

```java
package com.magicmod.spells;

import com.overworldlabs.regions.bridge.hook.HookResolver;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class FireballSpell {

    public void cast(PlayerRef player) {
        var pos = player.getTransform().getPosition();
        
        // 1. Check if the player is allowed to perform a spell action here
        boolean allowed = HookResolver.check(
            player.getUuid(),
            player.getWorldName(),
            (int)pos.x, (int)pos.y, (int)pos.z,
            "MAGIC_CAST"
        );

        if (!allowed) {
            player.sendMessage("§4Magic is suppressed in this region!");
            
            // 2. Notify the protect plugin to send its own denial message
            HookResolver.notifyDenied(player.getUuid(), player.getWorldName(), (int)pos.x, (int)pos.y, (int)pos.z, "MAGIC_CAST");
            return;
        }

        // 3. Proceed with spell logic...
        spawnFireballEntity(player);
    }
}
```
