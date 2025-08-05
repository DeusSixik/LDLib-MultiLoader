package com.lowdragmc.lowdraglib.core.mixins.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class MixinPluginSharedImpl {
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
