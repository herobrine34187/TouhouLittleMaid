package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register;

import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.MaidBaubleBuilder;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.MaidTaskBuilder;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.MaidTipsBuilder;

public class MaidRegisterJS {
    public static final MaidTipsBuilder TIPS = new MaidTipsBuilder();
    public static final MaidBaubleBuilder BAUBLE = new MaidBaubleBuilder();
    public static final MaidTaskBuilder TASK = new MaidTaskBuilder();
}
