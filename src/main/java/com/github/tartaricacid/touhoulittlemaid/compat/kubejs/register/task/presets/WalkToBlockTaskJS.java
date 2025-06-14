package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.task.presets;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidArriveAtBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToPredicateBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class WalkToBlockTaskJS implements IMaidTask {
    private final Builder builder;

    public WalkToBlockTaskJS(Builder builder) {
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
    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        if (this.builder.sound == null) {
            return SoundUtil.environmentSound(maid, InitSounds.MAID_IDLE.get(), 0.5f);
        }
        return this.builder.sound;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        MaidMoveToPredicateBlockTask moveToPredicateBlockTask = new MaidMoveToPredicateBlockTask(
                0.6f, builder.verticalSearchRange, builder.searchCondition, builder.blockPredicate);
        MaidArriveAtBlockTask arriveAtBlockTask = new MaidArriveAtBlockTask(builder.closeEnoughDist, builder.arriveAction);
        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = Lists.newArrayList(
                Pair.of(5, moveToPredicateBlockTask),
                Pair.of(6, arriveAtBlockTask));
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
    public List<Pair<String, Predicate<EntityMaid>>> getEnableConditionDesc(EntityMaid maid) {
        return this.builder.enableConditionDesc;
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return this.builder.conditionDesc;
    }

    public static class Builder {
        private final ResourceLocation id;
        private final ItemStack icon;

        private final List<Pair<Integer, BiFunction<WalkToBlockTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>>>> brains = Lists.newArrayList();

        private final List<Pair<String, Predicate<EntityMaid>>> enableConditionDesc = Lists.newArrayList();
        private final List<Pair<String, Predicate<EntityMaid>>> conditionDesc = Lists.newArrayList();

        private @Nullable Predicate<EntityMaid> enable = null;
        private @Nullable Predicate<EntityMaid> enableLookAndRandomWalk = null;

        private @Nullable Predicate<EntityMaid> searchCondition = null;
        private @Nullable BiPredicate<EntityMaid, BlockPos> blockPredicate = null;
        private @Nullable BiConsumer<EntityMaid, BlockPos> arriveAction;

        private @Nullable SoundEvent sound;
        private double closeEnoughDist = 2;
        private int verticalSearchRange = 2;

        public Builder(ResourceLocation id, ItemStack icon) {
            this.id = id;
            this.icon = icon;
        }

        public Builder addBrain(int priority, BiFunction<WalkToBlockTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>> brain) {
            this.brains.add(Pair.of(priority, brain));
            return this;
        }

        public Builder setEnableCondition(Predicate<EntityMaid> condition) {
            this.enable = condition;
            return this;
        }

        public Builder setEnableLookAndRandomWalk(Predicate<EntityMaid> condition) {
            this.enableLookAndRandomWalk = condition;
            return this;
        }

        public Builder setSearchCondition(Predicate<EntityMaid> condition) {
            this.searchCondition = condition;
            return this;
        }

        public Builder setBlockPredicate(BiPredicate<EntityMaid, BlockPos> predicate) {
            this.blockPredicate = predicate;
            return this;
        }

        public Builder setArriveAction(BiConsumer<EntityMaid, BlockPos> action) {
            this.arriveAction = action;
            return this;
        }

        public Builder setSound(SoundEvent sound) {
            this.sound = sound;
            return this;
        }

        public Builder setCloseEnoughDist(double dist) {
            this.closeEnoughDist = dist;
            return this;
        }

        public Builder setVerticalSearchRange(int range) {
            this.verticalSearchRange = range;
            return this;
        }
    }
}
