# Regions Bridge Examples

This document provides concrete examples of how to implement and consume the various hooks provided by Regions Bridge.

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

Below are example implementations of handlers that the Bridge will call via reflection.

### Damage Hook (`regions.damage.hook`)
Prevents specific types of environmental damage.

```java
public class MyDamageHook {
    public boolean shouldPreventDamage(UUID uuid, String worldName, int x, int y, int z, String damageType) {
        // Example: Prevent fire damage in a specific region
        if (damageType.equals("FIRE") || damageType.equals("LAVA")) {
            return isLavaSafeZone(worldName, x, y, z);
        }
        return false;
    }
}
```

### Movement Hook (`regions.movement.hook`)
Handles speed and flight.

```java
public class MyMovementHook {
    public double getMovementMultiplier(UUID uuid, String worldName, int x, int y, int z) {
        // Grant 50% speed boost in adventure zones
        return isAdventureZone(worldName, x, y, z) ? 1.5 : 1.0;
    }

    public boolean canFly(UUID uuid, String worldName, int x, int y, int z) {
        // Allow flight only if the player owns the plot
        return isPlotOwner(uuid, worldName, x, y, z);
    }
}
```

### Attribute Hook (`regions.attribute.hook`)
Controls Health, Stamina, and Special (Mana) resources.

```java
public class MyAttributeHook {
    public boolean shouldPreventAttributeLoss(UUID uuid, String worldName, int x, int y, int z, String type) {
        if (type.equals("STAMINA")) {
            return true; // Infinite stamina everywhere for this example
        }
        return false;
    }
}
```

---

## 3. Querying Examples (Consumer Side / Mixins)

How to call these hooks from within server systems or Mixins.

### Controlling Flight in Player Systems

```java
public void updatePlayerFlight(PlayerRef player) {
    UUID uuid = player.getUuid();
    String world = player.getWorldName();
    int x = (int) player.getTransform().getPosition().x;
    int y = (int) player.getTransform().getPosition().y;
    int z = (int) player.getTransform().getPosition().z;

    if (HookResolver.canFly(uuid, world, x, y, z)) {
        // Enable Hytale's internal flight component
    }
}
```

### Blocking Damage in Damage Systems

```java
public void onDamage(DamageEvent event) {
    if (HookResolver.shouldPreventDamage(
            event.getTargetUuid(), 
            event.getWorldName(), 
            event.getX(), event.getY(), event.getZ(), 
            event.getDamageType().name())) {
        event.setCancelled(true);
    }
}
```

### Applying Speed Multipliers

```java
public float calculateFinalSpeed(PlayerRef player, float baseSpeed) {
    double multiplier = HookResolver.getMovementMultiplier(
        player.getUuid(), 
        player.getWorldName(),
        (int)player.getTransform().getPosition().x,
        (int)player.getTransform().getPosition().y,
        (int)player.getTransform().getPosition().z
    );
    
    return (float) (baseSpeed * multiplier);
}
```
