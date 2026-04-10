package com.overworldlabs.regions.bridge.runtime;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import java.io.IOException;

public final class MixinBytecodeProvider implements IClassBytecodeProvider {
    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException, IOException {
        // First check our memory registry
        byte[] bytes = SyntheticClassRegistry.get(name);
        
        if (bytes == null) {
            // If not synthetic, read from Hytale's resources
            ClassReader reader = LaunchEnvironment.get().getClassReader(name);
            bytes = reader.b;
        }

        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, readerFlags);
        return node;
    }
}
