# Protection Implementation Example

> [!IMPORTANT]
> Verify the [Development Setup](../setup.md) to ensure your plugin can register hooks correctly.

This documentation is for **Protection Providers** implementing the Bridge contract.

### Damage Prevention Hook

```java
public class MyDamageHook {
    /**
     * Called by the Bridge.
     * @param damageType FIRE, LAVA, FALL, etc.
     */
    public boolean shouldPreventDamage(UUID uuid, String world, int x, int y, int z, String damageType) {
        Region region = RegionManager.getAt(world, x, y, z);
        return region != null && region.hasFlag("DISABLE_" + damageType);
    }
}
```

### Movement Multiplier Hook

```java
public class MyMovementHook {
    public double getMovementMultiplier(UUID uuid, String world, int x, int y, int z) {
        Region region = RegionManager.getAt(world, x, y, z);
        return (region != null) ? region.getSpeedMultiplier() : 1.0;
    }
}
```
