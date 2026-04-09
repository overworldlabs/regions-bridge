package com.overworldlabs.regions.bridge.mixin;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;
import java.util.Map;

@Mixin(com.hypixel.hytale.server.core.asset.type.fluid.FireFluidTicker.class)
public abstract class FireFluidTickerMixin {

    @Redirect(method = "process", at = @At(value = "INVOKE", target = "Lcom/hypixel/hytale/server/core/universe/world/chunk/section/FluidSection;setFluid(IIIIB)Z"), require = 0)
    private boolean regions$guardFireSpread(FluidSection section, int localX, int localY, int localZ, int fluidId, byte level,
            World world, long gameTick, int fromX, int fromY, int fromZ) {
        int toX = (section.getX() << 4) + localX;
        int toY = (section.getY() << 4) + localY;
        int toZ = (section.getZ() << 4) + localZ;
        
        if (shouldBlockFire(world, fromX, fromZ, toX, toZ)) {
            return false;
        }
        return section.setFluid(localX, localY, localZ, fluidId, level);
    }

    private static boolean shouldBlockFire(World world, int fromX, int fromZ, int toX, int toZ) {
        if (world == null) return false;
        Object result = com.overworldlabs.regions.bridge.util.BridgeHook.call("regions.fire.spread.hook", "shouldBlockFireSpread", 
            new Class<?>[]{String.class, int.class, int.class, int.class, int.class}, world.getName(), fromX, fromZ, toX, toZ);
        return result instanceof Boolean b && b;
    }
}


