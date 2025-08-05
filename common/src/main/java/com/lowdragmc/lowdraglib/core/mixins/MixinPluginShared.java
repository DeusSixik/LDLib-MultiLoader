package com.lowdragmc.lowdraglib.core.mixins;

import dev.architectury.injectables.annotations.ExpectPlatform;

public interface MixinPluginShared {

	@Deprecated
	static boolean isClassFound(String className) {
		try {
			Class.forName(className, false, Thread.currentThread().getContextClassLoader());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@ExpectPlatform
	private static boolean isModLoaded(String modId) {
		throw new AssertionError();
	}

	boolean IS_OPT_LOAD = isModLoaded("optifine");
	boolean IS_SODIUM_LOAD = isModLoaded("sodium");
	boolean IS_JEI_LOAD = isModLoaded("jei");
	boolean IS_REI_LOAD = isModLoaded("rei");
	boolean IS_MEI_LOAD = isModLoaded("emi");
	boolean IS_EMI_LOADED = IS_MEI_LOAD;
	boolean IS_RUBIDIUM_LOAD = IS_SODIUM_LOAD;
	boolean IS_IRIS_LOAD = isModLoaded("iris");
	boolean IS_OCULUS_LOAD = IS_IRIS_LOAD || isModLoaded("oculus");
	boolean IS_KJS_LOAD = isModLoaded("kubejs");

}
