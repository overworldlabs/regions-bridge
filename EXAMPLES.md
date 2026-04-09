# Regions Bridge Examples

This document provides concrete examples of how to implement and consume the various hooks provided by Regions Bridge using a generic region-based protection system.

## 1. Registration

Register your hooks in the global registry (usually in your main plugin's `onEnable` or `setup` method).

```java
import java.util.HashMap;
import java.util.Map;

public void registerHooks() {
    Map<String, Object> registry = new HashMap<>();

    // Register various handlers
    registry.put("regions.use.hook", new MyProtectionHook());
    registry.put("regions.command.hook", new MyCommandHook());
    registry.put("regions.damage.hook", new MyDamageHook());
    registry.put("regions.movement.hook", new MyMovementHook());
    registry.put("regions.attribute.hook", new MyAttributeHook());
    
    // Publish the registry to System properties
    System.getProperties().put("regions.hook.registry", registry);
}
```

---

## 2. Implementation Examples (Provider Side)

Below are example implementations of handlers that the Bridge will call via reflection. These examples assume you have a `RegionManager` to check for area-based flags.

### Damage Hook (`regions.damage.hook`)
Prevents specific types of environmental damage within protected zones.

```java
public class MyDamageHook {
    public boolean shouldPreventDamage(UUID uuid, String worldName, int x, int y, int z, String damageType) {
        // Find if the location is inside a "No-Fire" or "Safety" region
        Region region = RegionManager.getAt(worldName, x, y, z);
        if (region != null && region.hasFlag("DISABLE_" + damageType)) {
            return true;
        }
        return false;
    }
}
```

### Movement Hook (`regions.movement.hook`)
Handles speed and flight within specific areas.

```java
public class MyMovementHook {
    public double getMovementMultiplier(UUID uuid, String worldName, int x, int y, int z) {
        // Apply a speed boost in "Haste" regions
        Region region = RegionManager.getAt(worldName, x, y, z);
        return (region != null) ? region.getSpeedMultiplier() : 1.0;
    }

    public boolean canFly(UUID uuid, String worldName, int x, int y, int z) {
        // Allow flight only in specific "Fly-Allowed" regions or for region owners
        Region region = RegionManager.getAt(worldName, x, y, z);
        return region != null && (region.isOwner(uuid) || region.hasFlag("ALLOW_FLIGHT"));
    }
}
```

### Attribute Hook (`regions.attribute.hook`)
Controls Health, Stamina, and Special resources based on region rules.

```java
public class MyAttributeHook {
    public boolean shouldPreventAttributeLoss(UUID uuid, String worldName, int x, int y, int z, String type) {
        Region region = RegionManager.getAt(worldName, x, y, z);
        // Example: Infinite stamina in "Hub" or "Parkour" regions
        if (type.equals("STAMINA") && region != null && region.hasFlag("INFINITE_STAMINA")) {
            return true;
        }
        return false;
    }
}
```

---

## 3. Querying Examples (Consumer Side / Mixins)

How to call these hooks from within server systems without knowing which plugin provides the protection.

### Controlling Flight in Player Systems

```java
public void updatePlayerFlight(PlayerRef player) {
    UUID uuid = player.getUuid();
    String world = player.getWorldName();
    int x = (int) player.getTransform().getPosition().x;
    int y = (int) player.getTransform().getPosition().y;
    int z = (int) player.getTransform().getPosition().z;

    // The bridge checks the registry automatically
    if (HookResolver.canFly(uuid, world, x, y, z)) {
        // Enable flight for the player
        player.getComponent(FlightComponent.class).setCanFly(true);
    }
}
```

### Blocking Damage in Damage Systems

```java
public void onDamage(DamageEvent event) {
    // Check if the area protects against this specific damage type
    if (HookResolver.shouldPreventDamage(
            event.getTargetUuid(), 
            event.getWorldName(), 
            (int)event.getX(), (int)event.getY(), (int)event.getZ(), 
            event.getDamageType().name())) {
        event.setCancelled(true);
    }
}
```
