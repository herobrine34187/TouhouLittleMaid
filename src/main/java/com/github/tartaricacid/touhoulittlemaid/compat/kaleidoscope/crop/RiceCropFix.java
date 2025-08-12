package com.github.tartaricacid.touhoulittlemaid.compat.kaleidoscope.crop;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RiceCropFix {
    public static boolean isRiceSeed(ItemStack stack) {
        return stack.is(ModItems.RICE_SEED.get()) || stack.is(ModItems.WILD_RICE_SEED.get());
    }

    public static boolean isRiceCrop(BlockState state) {
        return state.is(ModBlocks.RICE_CROP.get());
    }
}
