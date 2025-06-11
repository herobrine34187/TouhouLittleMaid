package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder;

import com.github.tartaricacid.touhoulittlemaid.client.overlay.MaidTipsOverlay;
import com.google.common.collect.Lists;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.List;
import java.util.function.Consumer;

public class MaidTipsBuilder {
    private final List<Consumer<MaidTipsOverlay>> tips = Lists.newArrayList();

    public MaidTipsBuilder tips(Consumer<MaidTipsOverlay> consumer) {
        tips.add(consumer);
        return this;
    }

    @HideFromJS
    public void register(MaidTipsOverlay overlay) {
        this.tips.forEach(consumer -> consumer.accept(overlay));
        this.tips.clear();
    }
}
