package com.lowdragmc.lowdraglib.core.mixins.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;

public class MixinPluginSharedImpl {
    public static boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getModFileById(modId) != null;
        }
        return ModList.get().isLoaded(modId);
    }
}
