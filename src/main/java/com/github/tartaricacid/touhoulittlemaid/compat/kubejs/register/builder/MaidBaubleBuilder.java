package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.google.common.collect.Maps;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiConsumer;

public class MaidBaubleBuilder {
    private final Map<Item, CustomKubeJSBauble> baubles = Maps.newHashMap();

    public CustomKubeJSBauble bind(Item item, @Nullable BiConsumer<EntityMaid, ItemStack> onTick) {
        CustomKubeJSBauble bauble = new CustomKubeJSBauble(onTick);
        baubles.put(item, bauble);
        return bauble;
    }

    public CustomKubeJSBauble bind(Item item) {
        return bind(item, null);
    }

    @HideFromJS
    public void register(BaubleManager manager) {
        this.baubles.forEach(manager::bind);
        this.baubles.clear();
    }

    public static class CustomKubeJSBauble implements IMaidBauble {
        private final BiConsumer<EntityMaid, ItemStack> biConsumer;

        public CustomKubeJSBauble(@Nullable BiConsumer<EntityMaid, ItemStack> biConsumer) {
            this.biConsumer = biConsumer;
        }

        @Override
        public void onTick(EntityMaid maid, ItemStack baubleItem) {
            if (biConsumer != null) {
                biConsumer.accept(maid, baubleItem);
            }
        }
    }
}
