package com.overworldlabs.regions.bridge.util;

import com.hypixel.hytale.server.core.universe.world.World;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BridgeHook {
    private static Map<String, Object> registryCache = null;
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> hookCache = new ConcurrentHashMap<>();

    private BridgeHook() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getRegistry() {
        if (registryCache != null) return registryCache;
        Object raw = System.getProperties().get("regions.hook.registry");
        if (raw instanceof Map) {
            registryCache = (Map<String, Object>) raw;
            return registryCache;
        }
        return null;
    }

    public static Object getHook(String key) {
        Object cached = hookCache.get(key);
        if (cached != null) return cached;
        
        Map<String, Object> registry = getRegistry();
        if (registry == null) return null;
        
        Object hook = registry.get(key);
        if (hook != null) {
            hookCache.put(key, hook);
        }
        return hook;
    }

    public static Object call(String hookKey, String methodName, Class<?>[] argTypes, Object... args) {
        Object hook = getHook(hookKey);
        if (hook == null) return null;

        String methodKey = hook.getClass().getName() + "#" + methodName;
        Method method = methodCache.get(methodKey);
        
        try {
            if (method == null) {
                method = hook.getClass().getMethod(methodName, argTypes);
                methodCache.put(methodKey, method);
            }
            return method.invoke(hook, args);
        } catch (Exception e) {
            return null;
        }
    }
}


