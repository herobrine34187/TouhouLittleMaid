package com.github.tartaricacid.touhoulittlemaid.config;

import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.*;
import net.minecraftforge.common.ForgeConfigSpec;

public final class GeneralConfig {
    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        MaidConfig.init(builder);
        ChairConfig.init(builder);
        MiscConfig.init(builder);
        VanillaConfig.init(builder);
        AIConfig.init(builder);
        return builder.build();
    }
}
