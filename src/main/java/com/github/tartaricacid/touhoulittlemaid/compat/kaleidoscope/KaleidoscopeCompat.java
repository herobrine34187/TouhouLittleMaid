package com.github.tartaricacid.touhoulittlemaid.compat.kaleidoscope;

import net.minecraftforge.fml.ModList;

public class KaleidoscopeCompat {
    public static final String COOKERY_ID = "kaleidoscope_cookery";
    private static boolean IS_COOKERY_LOADED = false;

    public static void init() {
        IS_COOKERY_LOADED = ModList.get().isLoaded(COOKERY_ID);
    }
}
