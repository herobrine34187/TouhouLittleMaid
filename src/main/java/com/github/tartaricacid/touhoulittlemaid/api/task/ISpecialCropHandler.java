package com.github.tartaricacid.touhoulittlemaid.api.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

public interface ISpecialCropHandler {
    /**
     * 判断是否是种子，在前面 Item 基础上增加额外判断
     */
    default boolean isSeed(ItemStack stack) {
        return true;
    }

    /**
     * 判断是否可以收获
     */
    default boolean canHarvest(EntityMaid maid, BlockPos cropPos, BlockState cropState) {
        return false;
    }

    /**
     * 执行收获逻辑
     *
     * @param isDestroyMode 当女仆持有锄头时，此值为 true，表示直接破坏
     *                      否则是类似于右键收获
     */
    default void harvest(EntityMaid maid, BlockPos cropPos, BlockState cropState, boolean isDestroyMode) {
    }

    /**
     * 判断是否可以种植
     */
    default boolean canPlant(EntityMaid maid, BlockPos basePos, BlockState baseState, ItemStack seed) {
        BlockState aboveState = maid.level.getBlockState(basePos.above());
        if (!aboveState.canBeReplaced() || aboveState.liquid()) {
            return false;
        }
        if (seed.getItem() instanceof ItemNameBlockItem blockNamedItem) {
            Block block = blockNamedItem.getBlock();
            if (block instanceof IPlantable plant) {
                return baseState.canSustainPlant(maid.level, basePos, Direction.UP, plant);
            }
        }
        return false;
    }

    /**
     * 执行种植逻辑
     */
    default ItemStack plant(EntityMaid maid, BlockPos basePos, BlockState baseState, ItemStack seed) {
        if (seed.getItem() instanceof ItemNameBlockItem blockNamedItem) {
            Block block = blockNamedItem.getBlock();
            if (block instanceof IPlantable) {
                maid.placeItemBlock(basePos.above(), seed);
            }
        }
        return seed;
    }
}
