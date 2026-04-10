package com.overworldlabs.regions.bridge.runtime;

import org.spongepowered.asm.service.IClassProvider;
import java.lang.reflect.Method;
import java.net.URL;

public final class MixinClassProvider implements IClassProvider {
    private static Method defineClassMethod;

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, false);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        if (name != null && name.startsWith("java.")) {
            return Class.forName(name, initialize, null);
        }

        // 1. Try finding in the original Hytale Runtime Loader
        ClassLoader runtime = LaunchEnvironment.get().getRuntimeLoader();
        if (runtime != null) {
            try {
                return Class.forName(name, initialize, runtime);
            } catch (ClassNotFoundException ignored) {}
        }

        // 2. Try finding in our Synthetic Registry (Accessors/Invokers)
        byte[] syntheticBytes = SyntheticClassRegistry.get(name);
        if (syntheticBytes != null && runtime != null) {
            return defineSyntheticClass(runtime, name, syntheticBytes);
        }

        return Class.forName(name, initialize, ClassLoader.getSystemClassLoader());
    }

    private Class<?> defineSyntheticClass(ClassLoader loader, String name, byte[] bytes) {
        try {
            if (defineClassMethod == null) {
                defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                defineClassMethod.setAccessible(true);
            }
            return (Class<?>) defineClassMethod.invoke(loader, name, bytes, 0, bytes.length);
        } catch (Exception e) {
            // If already defined, just return findLoadedClass (simplified for now)
            try {
                Method findLoaded = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
                findLoaded.setAccessible(true);
                return (Class<?>) findLoaded.invoke(loader, name);
            } catch (Exception ignored) {}
            throw new RuntimeException("Failed to define synthetic class: " + name, e);
        }
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return findClass(name, initialize);
    }
}
