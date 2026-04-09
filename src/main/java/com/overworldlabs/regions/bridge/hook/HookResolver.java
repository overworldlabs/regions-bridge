package com.overworldlabs.regions.bridge.hook;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HookResolver {
    private static final String REGISTRY_KEY = "regions.hook.registry";
    private static final String GENERIC_CHECK_HOOK_KEY = "regions.use.hook";
    private static final String COMMAND_HOOK_KEY = "regions.command.hook";
    private static final String EXPLOSION_HOOK_KEY = "regions.explosion.hook";
    private static final String SPAWN_HOOK_KEY = "regions.spawn.hook";
    private static final String DEATH_HOOK_KEY = "regions.death.hook";
    private static final String DURABILITY_HOOK_KEY = "regions.durability.hook";
    private static final String ATTRIBUTE_HOOK_KEY = "regions.attribute.hook";
    private static final String MOVEMENT_HOOK_KEY = "regions.movement.hook";
    private static final String VISIBILITY_HOOK_KEY = "regions.visibility.hook";
    private static final String DAMAGE_HOOK_KEY = "regions.damage.hook";

    private static final Map<Class<?>, MethodHandle> CHECK_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> NOTIFY_DENIED_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> COMMAND_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> DENY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> EXPLOSION_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> SPAWN_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> KEEP_INV_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> DURABILITY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> ATTRIBUTE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> MOVEMENT_MULTIPLIER_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> FLIGHT_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> VISIBILITY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> DAMAGE_CACHE = new ConcurrentHashMap<>();

    private HookResolver() {
    }

    public static boolean check(@Nullable UUID playerUuid, @Nullable String worldName, int x, int y, int z,
            @Nullable String mode) {
        if (playerUuid == null) {
            return true;
        }

        Object hook = getHook(GENERIC_CHECK_HOOK_KEY);
        if (hook == null) {
            return true;
        }

        MethodHandle mh = CHECK_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findCheckHandle);
        if (mh == null) {
            return true;
        }

        String useWorld = worldName;
        if (useWorld == null || useWorld.isBlank()) {
            PlayerRef ref = Universe.get().getPlayer(playerUuid);
            if (ref != null && ref.getWorldUuid() != null) {
                World world = Universe.get().getWorld(ref.getWorldUuid());
                if (world != null) {
                    useWorld = world.getName();
                }
            }
        }

        if (useWorld == null) {
            useWorld = "";
        }

        if (mode == null) {
            mode = "INTERACT";
        }

        try {
            return (boolean) mh.invoke(hook, playerUuid, useWorld, (double) x, (double) y, (double) z, mode);
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static void notifyDenied(@Nullable UUID playerUuid, @Nullable String worldName, int x, int y, int z,
            @Nullable String mode) {
        if (playerUuid == null) {
            return;
        }

        Object hook = getHook(GENERIC_CHECK_HOOK_KEY);
        if (hook == null) {
            return;
        }

        MethodHandle mh = NOTIFY_DENIED_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findNotifyDeniedHandle);
        if (mh == null) {
            return;
        }

        String useWorld = worldName;
        if (useWorld == null || useWorld.isBlank()) {
            PlayerRef ref = Universe.get().getPlayer(playerUuid);
            if (ref != null && ref.getWorldUuid() != null) {
                World world = Universe.get().getWorld(ref.getWorldUuid());
                if (world != null) {
                    useWorld = world.getName();
                }
            }
        }

        if (useWorld == null) {
            useWorld = "";
        }

        if (mode == null) {
            mode = "INTERACT";
        }

        try {
            mh.invoke(hook, playerUuid, useWorld, (double) x, (double) y, (double) z, mode);
        } catch (Throwable ignored) {
        }
    }

    @Nullable
    private static Object getHook(String key) {
        Object raw = System.getProperties().get(REGISTRY_KEY);
        if (!(raw instanceof Map<?, ?> map)) {
            return null;
        }
        return map.get(key);
    }

    @Nullable
    private static MethodHandle findCheckHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "check",
                    MethodType.methodType(boolean.class, UUID.class, String.class, double.class, double.class,
                            double.class, String.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findNotifyDeniedHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "notifyDenied",
                    MethodType.methodType(void.class, UUID.class, String.class, double.class, double.class,
                            double.class, String.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean shouldBlockCommand(@Nullable CommandSender sender, @Nullable String rawCommand) {
        if (sender == null) {
            return false;
        }

        Object hook = getHook(COMMAND_HOOK_KEY);
        if (hook == null) {
            return false;
        }

        MethodHandle mh = COMMAND_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findCommandHandle);
        if (mh == null) {
            return false;
        }

        try {
            return (boolean) mh.invoke(hook, sender, rawCommand != null ? rawCommand : "");
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public static String getCommandDenialMessage() {
        Object hook = getHook(COMMAND_HOOK_KEY);
        if (hook == null) {
            return null;
        }

        MethodHandle mh = DENY_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findDenyHandle);
        if (mh == null) {
            return null;
        }

        try {
            Object value = mh.invoke(hook);
            return value != null ? String.valueOf(value) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean shouldBlockExplosion(@Nullable World world, int x, int y, int z) {
        if (world == null) {
            return false;
        }

        Object hook = getHook(EXPLOSION_HOOK_KEY);
        if (hook == null) {
            return false;
        }

        MethodHandle mh = EXPLOSION_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findExplosionHandle);
        if (mh == null) {
            return false;
        }

        try {
            return (boolean) mh.invoke(hook, world, x, y, z);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    private static MethodHandle findCommandHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "shouldBlockCommand",
                    MethodType.methodType(boolean.class, CommandSender.class, String.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findDenyHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "getDenialMessage",
                    MethodType.methodType(String.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findExplosionHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "shouldBlockExplosion",
                    MethodType.methodType(boolean.class, World.class, int.class, int.class, int.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean shouldBlockSpawn(@Nullable String worldName, int x, int y, int z) {
        if (worldName == null || worldName.isBlank()) {
            return false;
        }

        Object hook = getHook(SPAWN_HOOK_KEY);
        if (hook == null) {
            return false;
        }

        MethodHandle mh = SPAWN_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findSpawnHandle);
        if (mh == null) {
            return false;
        }

        try {
            return (boolean) mh.invoke(hook, worldName, x, y, z);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean shouldKeepInventory(@Nullable UUID uuid, @Nullable String worldName, int x, int y, int z) {
        if (uuid == null || worldName == null || worldName.isBlank()) {
            return false;
        }

        Object hook = getHook(DEATH_HOOK_KEY);
        if (hook == null) {
            return false;
        }

        MethodHandle mh = KEEP_INV_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findKeepInvHandle);
        if (mh == null) {
            return false;
        }

        try {
            return (boolean) mh.invoke(hook, uuid, worldName, x, y, z);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean shouldPreventDurabilityLoss(@Nullable UUID uuid, @Nullable String worldName, int x, int y,
            int z) {
        if (uuid == null || worldName == null || worldName.isBlank()) {
            return false;
        }

        Object hook = getHook(DURABILITY_HOOK_KEY);
        if (hook == null) {
            return false;
        }

        MethodHandle mh = DURABILITY_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findDurabilityHandle);
        if (mh == null) {
            return false;
        }

        try {
            return (boolean) mh.invoke(hook, uuid, worldName, x, y, z);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean shouldPreventAttributeLoss(@Nullable UUID uuid, @Nullable String worldName, int x, int y,
            int z, @Nullable String attributeType) {
        if (uuid == null || worldName == null || worldName.isBlank()) {
            return false;
        }

        Object hook = getHook(ATTRIBUTE_HOOK_KEY);
        if (hook == null) {
            return false;
        }

        MethodHandle mh = ATTRIBUTE_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findAttributeHandle);
        if (mh == null) {
            return false;
        }

        try {
            return (boolean) mh.invoke(hook, uuid, worldName, x, y, z, attributeType != null ? attributeType : "HEALTH");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static double getMovementMultiplier(@Nullable UUID uuid, @Nullable String worldName, int x, int y, int z) {
        if (uuid == null || worldName == null || worldName.isBlank()) {
            return 1.0;
        }

        Object hook = getHook(MOVEMENT_HOOK_KEY);
        if (hook == null) {
            return 1.0;
        }

        MethodHandle mh = MOVEMENT_MULTIPLIER_CACHE.computeIfAbsent(hook.getClass(),
                HookResolver::findMovementMultiplierHandle);
        if (mh == null) {
            return 1.0;
        }

        try {
            return (double) mh.invoke(hook, uuid, worldName, x, y, z);
        } catch (Throwable ignored) {
            return 1.0;
        }
    }

    public static boolean canFly(@Nullable UUID uuid, @Nullable String worldName, int x, int y, int z) {
        if (uuid == null || worldName == null || worldName.isBlank()) {
            return false;
        }

        Object hook = getHook(MOVEMENT_HOOK_KEY);
        if (hook == null) {
            return false;
        }

        MethodHandle mh = FLIGHT_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findFlightHandle);
        if (mh == null) {
            return false;
        }

        try {
            return (boolean) mh.invoke(hook, uuid, worldName, x, y, z);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean shouldHidePlayer(@Nullable UUID uuid, @Nullable String worldName, int x, int y, int z) {
        if (uuid == null || worldName == null || worldName.isBlank()) {
            return false;
        }

        Object hook = getHook(VISIBILITY_HOOK_KEY);
        if (hook == null) {
            return false;
        }

        MethodHandle mh = VISIBILITY_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findVisibilityHandle);
        if (mh == null) {
            return false;
        }

        try {
            return (boolean) mh.invoke(hook, uuid, worldName, x, y, z);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean shouldPreventDamage(@Nullable UUID uuid, @Nullable String worldName, int x, int y, int z,
            @Nullable String damageType) {
        if (uuid == null || worldName == null || worldName.isBlank()) {
            return false;
        }

        Object hook = getHook(DAMAGE_HOOK_KEY);
        if (hook == null) {
            return false;
        }

        MethodHandle mh = DAMAGE_CACHE.computeIfAbsent(hook.getClass(), HookResolver::findDamageHandle);
        if (mh == null) {
            return false;
        }

        try {
            return (boolean) mh.invoke(hook, uuid, worldName, x, y, z, damageType != null ? damageType : "GENERIC");
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    private static MethodHandle findSpawnHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "shouldBlockSpawn",
                    MethodType.methodType(boolean.class, String.class, int.class, int.class, int.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findKeepInvHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "shouldKeepInventory",
                    MethodType.methodType(boolean.class, UUID.class, String.class, int.class, int.class, int.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findDurabilityHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "shouldPreventDurabilityLoss",
                    MethodType.methodType(boolean.class, UUID.class, String.class, int.class, int.class, int.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findAttributeHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "shouldPreventAttributeLoss",
                    MethodType.methodType(boolean.class, UUID.class, String.class, int.class, int.class, int.class,
                            String.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findMovementMultiplierHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "getMovementMultiplier",
                    MethodType.methodType(double.class, UUID.class, String.class, int.class, int.class, int.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findFlightHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "canFly",
                    MethodType.methodType(boolean.class, UUID.class, String.class, int.class, int.class, int.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findVisibilityHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "shouldHidePlayer",
                    MethodType.methodType(boolean.class, UUID.class, String.class, int.class, int.class, int.class));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle findDamageHandle(Class<?> hookClass) {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    hookClass,
                    "shouldPreventDamage",
                    MethodType.methodType(boolean.class, UUID.class, String.class, int.class, int.class, int.class,
                            String.class));
        } catch (Exception ignored) {
            return null;
        }
    }
}
