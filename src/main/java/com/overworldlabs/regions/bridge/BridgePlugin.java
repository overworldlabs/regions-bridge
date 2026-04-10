package com.overworldlabs.regions.bridge;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

/**
 * Bridge plugin entrypoint for runtime diagnostics/state flags.
 */
public final class BridgePlugin extends JavaPlugin {
    private static final String ACTIVE_PROPERTY = "regions.mixin.bridge.active";
    private static final String BOOTSTRAP_READY_PROPERTY = "regions.mixin.bridge.bootstrap.ready";

    public BridgePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        System.setProperty(ACTIVE_PROPERTY, "true");
        System.setProperty(BOOTSTRAP_READY_PROPERTY, "true");
        getLogger().atInfo().log("Regions-Bridge plugin loaded.");
    }
}
