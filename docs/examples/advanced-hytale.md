# Advanced Feature: Legendary Phoenix Mount

This guide demonstrates how to build a high-tier feature using Regions Bridge. It covers custom template instantiation, skeletal animations, spatial audio, and Hytale-style input handling.

> [!IMPORTANT]
> Ensure you have followed the [Development Setup Guide](../setup.md) before implementing these features.

---

## 1. Unlocking Server Internals (Mixins)

To manage flight and custom states, we map the private fields of the `LivingEntity` class using Accessors and Invokers.

```java
package com.phoenixmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.universe.world.component.FlightComponent;

/**
 * Accessor targeting the core LivingEntity class.
 */
@Mixin(LivingEntity.class)
public interface PhoenixEntityAccessor {
    
    /** Accesses the private flight physics component */
    @Accessor("flightComponent")
    FlightComponent getFlightComponent();

    /** Forces server-to-client physics state synchronization */
    @Invoker("syncPhysicsState")
    void callSync();
}
```

---

## 2. Integrated Phoenix System (Templates, SFX & VFX)

Hytale uses `String` identifiers often separated by dots (e.g., `hytale.creature.kweebec`) rather than the colon-syntax found in other games.

```java
package com.phoenixmod.systems;

import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.component.AnimationComponent;
import com.hypixel.hytale.server.core.effect.SoundEffect;
import com.hypixel.hytale.server.core.effect.ParticleEffect;
import com.hypixel.hytale.server.core.util.math.Vector3;
import com.phoenixmod.mixin.PhoenixEntityAccessor;

/**
 * Manages the "Solar Phoenix" lifecycle using Hytale API standards.
 */
public class PhoenixMountManager {

    /**
     * Spawns a mount using a custom template and triggers cinematic effects.
     */
    public void spawnPhoenix(World world, Vector3 pos) {
        // 1. Spawn using Hytale Template Identifier (dot-separated)
        // Ensure "phoenixmod.creature.solar_phoenix" is defined in your templates.json
        Entity phoenix = world.spawnEntity("phoenixmod.creature.solar_phoenix", pos);

        // 2. Enable Flight via Bridge-enabled Accessor
        PhoenixEntityAccessor accessor = (PhoenixEntityAccessor) phoenix;
        accessor.getFlightComponent().setCanFly(true);
        accessor.callSync();

        // 3. Play spatial sound effect
        // Hytale references sounds via flat or dotted IDs
        SoundEffect.at(pos, "phoenixmod.sfx.fire_rise").setPitch(1.1f).play();

        // 4. Trigger fiery particles attached to the skeletal bones
        // Hytale's ParticleEffect system uses registered identifiers
        ParticleEffect.attach(phoenix, "phoenixmod.vfx.solar_trail").start();

        // 5. Play initial skeletal animation
        // Animations reflect the keys defined in the entity's .hmo file
        phoenix.getComponent(AnimationComponent.class).play("phoenix.rise");
        
        System.out.println("Phoenix summoned using Hytale Template System.");
    }
}
```

---

## 3. Advanced Input: Combination Detection (Shift + Space)

Detecting complex inputs by intercepting the `InputPacket` state.

```java
package com.phoenixmod.mixin;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.modules.input.InputPacket;
import com.hypixel.hytale.server.core.modules.input.Key;
import com.hypixel.hytale.builtin.mounts.MountSystems;
import com.hypixel.hytale.server.core.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.hypixel.hytale.server.core.modules.input.PlayerInputSystem")
public abstract class PhoenixInputMixin {

    @Inject(method = "handleInput", at = @At("HEAD"))
    private void onInput(PlayerRef player, InputPacket packet, CallbackInfo ci) {
        Entity mount = MountSystems.getMount(player);
        
        // Match template ID using Hytale's string format
        if (mount != null && "phoenixmod.creature.solar_phoenix".equals(mount.getTemplateId())) {
            
            // Detection: SHIFT + SPACE (Dash ability)
            if (packet.isKeyDown(Key.SPACE) && packet.isKeyDown(Key.LSHIFT)) {
                applySolarDash(player, mount);
            }
            
            // Dedicated Action Key
            if (packet.isKeyDown(Key.ACTION_1)) {
                triggerFireBreath(mount);
            }
        }
    }

    private void triggerFireBreath(Entity e) {
        // Execute SFX/VFX for fire breath
        SoundEffect.at(e.getTransform().getPosition(), "phoenixmod.sfx.firebreath").play();
    }
}
```

---

## 4. Hytale API Standards
- **Identifiers**: Use dots (`.`) to separate categories (e.g., `modname.category.item`).
- **Templates**: Everything in Hytale is data-driven via Templates. Ensure your `TemplateId` strings match your JSON asset definitions.
- **Components**: Use `getComponent(Class)` to access entity systems like `AnimationComponent`.
