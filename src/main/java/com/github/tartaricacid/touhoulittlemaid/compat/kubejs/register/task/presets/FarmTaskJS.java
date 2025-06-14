package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.task.presets;

import com.github.tartaricacid.touhoulittlemaid.api.task.IFarmTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFarmPlantTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFarmSurroundingMoveTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.github.tartaricacid.touhoulittlemaid.util.functional.QuadFunction;
import com.github.tartaricacid.touhoulittlemaid.util.functional.QuadPredicate;
import com.github.tartaricacid.touhoulittlemaid.util.functional.TriConsumer;
import com.github.tartaricacid.touhoulittlemaid.util.functional.TriPredicate;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class FarmTaskJS implements IFarmTask {
    private final Builder builder;

    public FarmTaskJS(Builder builder) {
        this.builder = builder;
    }

    @Override
    public ResourceLocation getUid() {
        return builder.id;
    }

    @Override
    public ItemStack getIcon() {
        return builder.icon;
    }

    @Override
    @Nullable
    public SoundEvent getAmbientSound(EntityMaid maid) {
        if (this.builder.sound == null) {
            return SoundUtil.attackSound(maid, InitSounds.MAID_FARM.get(), 0.5f);
        }
        return this.builder.sound;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        // 使用带 3x3x1 的范围的寻路任务
        MaidFarmSurroundingMoveTask maidFarmSurroundingMoveTask = new MaidFarmSurroundingMoveTask(this, 0.6f);
        MaidFarmPlantTask maidFarmPlantTask = new MaidFarmPlantTask(this);
        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = Lists.newArrayList(Pair.of(5, maidFarmSurroundingMoveTask), Pair.of(6, maidFarmPlantTask));
        for (var pair : this.builder.brains) {
            tasks.add(Pair.of(pair.getFirst(), pair.getSecond().apply(this, maid)));
        }
        return tasks;
    }

    @Override
    public boolean isEnable(EntityMaid maid) {
        if (this.builder.enable == null) {
            return true;
        }
        return this.builder.enable.test(maid);
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        if (this.builder.enableLookAndRandomWalk == null) {
            return true;
        }
        return this.builder.enableLookAndRandomWalk.test(maid);
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        if (this.builder.enableEating == null) {
            return true;
        }
        return this.builder.enableEating.test(maid);
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getEnableConditionDesc(EntityMaid maid) {
        return this.builder.enableConditionDesc;
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return this.builder.conditionDesc;
    }

    @Override
    public boolean isSeed(ItemStack stack) {
        if (this.builder.isSeed == null) {
            return false;
        }
        return this.builder.isSeed.test(stack);
    }

    @Override
    public boolean canHarvest(EntityMaid maid, BlockPos cropPos, BlockState cropState) {
        if (this.builder.canHarvest == null) {
            return false;
        }
        return this.builder.canHarvest.test(maid, cropPos, cropState);
    }

    @Override
    public void harvest(EntityMaid maid, BlockPos cropPos, BlockState cropState) {
        if (this.builder.harvest == null) {
            return;
        }
        this.builder.harvest.accept(maid, cropPos, cropState);
    }

    @Override
    public boolean canPlant(EntityMaid maid, BlockPos basePos, BlockState baseState, ItemStack seed) {
        if (this.builder.canPlant == null) {
            return false;
        }
        return this.builder.canPlant.test(maid, basePos, baseState, seed);
    }

    @Override
    public ItemStack plant(EntityMaid maid, BlockPos basePos, BlockState baseState, ItemStack seed) {
        if (this.builder.plant == null) {
            return seed;
        }
        return this.builder.plant.apply(maid, basePos, baseState, seed);
    }

    @Override
    public double getCloseEnoughDist() {
        return this.builder.closeEnoughDist;
    }

    @Override
    public boolean checkCropPosAbove() {
        return this.builder.checkCropPosAbove;
    }

    public static class Builder {
        private final ResourceLocation id;
        private final ItemStack icon;

        private final List<Pair<Integer, BiFunction<FarmTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>>>> brains = Lists.newArrayList();

        private final List<Pair<String, Predicate<EntityMaid>>> enableConditionDesc = Lists.newArrayList();
        private final List<Pair<String, Predicate<EntityMaid>>> conditionDesc = Lists.newArrayList();

        private @Nullable Predicate<EntityMaid> enable = null;
        private @Nullable Predicate<EntityMaid> enableLookAndRandomWalk = null;
        private @Nullable Predicate<EntityMaid> enableEating = null;

        private @Nullable Predicate<ItemStack> isSeed = null;
        private @Nullable TriPredicate<EntityMaid, BlockPos, BlockState> canHarvest = null;
        private @Nullable TriConsumer<EntityMaid, BlockPos, BlockState> harvest = null;
        private @Nullable QuadPredicate<EntityMaid, BlockPos, BlockState, ItemStack> canPlant = null;
        private @Nullable QuadFunction<EntityMaid, BlockPos, BlockState, ItemStack, ItemStack> plant = null;

        private @Nullable SoundEvent sound;
        private double closeEnoughDist = 2;
        private boolean checkCropPosAbove = true;

        public Builder(ResourceLocation id, ItemStack icon) {
            this.id = id;
            this.icon = icon;
        }

        public FarmTaskJS.Builder addBrain(int priority, BiFunction<FarmTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>> control) {
            this.brains.add(Pair.of(priority, control));
            return this;
        }

        public FarmTaskJS.Builder addEnableConditionDesc(String languageKey, Predicate<EntityMaid> condition) {
            this.enableConditionDesc.add(Pair.of(languageKey, condition));
            return this;
        }

        public FarmTaskJS.Builder addConditionDesc(String languageKey, Predicate<EntityMaid> condition) {
            this.conditionDesc.add(Pair.of(languageKey, condition));
            return this;
        }

        public FarmTaskJS.Builder enable(Predicate<EntityMaid> enable) {
            this.enable = enable;
            return this;
        }

        public FarmTaskJS.Builder enableLookAndRandomWalk(Predicate<EntityMaid> enableLookAndRandomWalk) {
            this.enableLookAndRandomWalk = enableLookAndRandomWalk;
            return this;
        }

        public FarmTaskJS.Builder enableEating(Predicate<EntityMaid> enableEating) {
            this.enableEating = enableEating;
            return this;
        }

        public FarmTaskJS.Builder isSeed(Predicate<ItemStack> isSeed) {
            this.isSeed = isSeed;
            return this;
        }

        public FarmTaskJS.Builder canHarvest(TriPredicate<EntityMaid, BlockPos, BlockState> canHarvest) {
            this.canHarvest = canHarvest;
            return this;
        }

        public FarmTaskJS.Builder harvest(TriConsumer<EntityMaid, BlockPos, BlockState> harvest) {
            this.harvest = harvest;
            return this;
        }

        public FarmTaskJS.Builder canPlant(QuadPredicate<EntityMaid, BlockPos, BlockState, ItemStack> canPlant) {
            this.canPlant = canPlant;
            return this;
        }

        public FarmTaskJS.Builder plant(QuadFunction<EntityMaid, BlockPos, BlockState, ItemStack, ItemStack> plant) {
            this.plant = plant;
            return this;
        }

        public FarmTaskJS.Builder sound(SoundEvent sound) {
            this.sound = sound;
            return this;
        }

        public FarmTaskJS.Builder closeEnoughDist(double closeEnoughDist) {
            this.closeEnoughDist = closeEnoughDist;
            return this;
        }

        public FarmTaskJS.Builder checkCropPosAbove(boolean checkCropPosAbove) {
            this.checkCropPosAbove = checkCropPosAbove;
            return this;
        }
    }
}
