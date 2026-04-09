package com.overworldlabs.regions.bridge.update;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BridgeUpdateNotifier {
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final String RELEASES_URL_DEFAULT = "https://github.com/overworldlabs/regions-bridge/releases";
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "Regions-Bridge-UpdateChecker");
        thread.setDaemon(true);
        return thread;
    });

    private BridgeUpdateNotifier() {
    }

    public static void check(String currentVersion) {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }

        // Check immediately
        performCheck(currentVersion);

        // Schedule periodic check every 1 hour
        SCHEDULER.scheduleAtFixedRate(() -> performCheck(currentVersion), 1, 1, TimeUnit.HOURS);
    }

    private static void performCheck(String currentVersion) {
        BridgeUpdateChecker.checkForUpdates(currentVersion).thenAccept(latestVersion -> {
            if (latestVersion == null) {
                return;
            }

            // Expose to other plugins (like Plots)
            System.setProperty("regions.bridge.latest_version", latestVersion);

            if (BridgeUpdateChecker.isNewerVersion(currentVersion, latestVersion)) {
                System.out.println("[Regions-Bridge] A new version is available: " + latestVersion + " (Current: " + currentVersion + ")");
                System.out.println("[Regions-Bridge] Download: " + releasesUrl());
            }
        });
    }

    private static String releasesUrl() {
        String fromProperty = System.getProperty("regions.mixin.bridge.update.releases");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        return RELEASES_URL_DEFAULT;
    }
}
