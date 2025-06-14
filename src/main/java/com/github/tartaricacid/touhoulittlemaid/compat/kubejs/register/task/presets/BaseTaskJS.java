package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.task.presets;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class BaseTaskJS implements IMaidTask {
    private final Builder builder;

    public BaseTaskJS(Builder builder) {
        this.builder = builder;
    }

    @Override
    public ResourceLocation getUid() {
        return this.builder.id;
    }

    @Override
    public ItemStack getIcon() {
        return this.builder.icon;
    }

    @Override
    @Nullable
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return this.builder.sound;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = Lists.newArrayList();
        for (var pair : this.builder.brains) {
            tasks.add(Pair.of(pair.getFirst(), pair.getSecond().apply(this, maid)));
        }
        return tasks;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = Lists.newArrayList();
        for (var pair : this.builder.rideBrains) {
            tasks.add(Pair.of(pair.getFirst(), pair.getSecond().apply(this, maid)));
        }
        return tasks;
    }

    @Override
    public boolean isEnable(EntityMaid maid) {
        if (this.builder.enable == null) {
            return IMaidTask.super.isEnable(maid);
        }
        return this.builder.enable.test(maid);
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        if (this.builder.enableLookAndRandomWalk == null) {
            return IMaidTask.super.enableLookAndRandomWalk(maid);
        }
        return this.builder.enableLookAndRandomWalk.test(maid);
    }

    @Override
    public boolean enablePanic(EntityMaid maid) {
        if (this.builder.enablePanic == null) {
            return IMaidTask.super.enablePanic(maid);
        }
        return this.builder.enablePanic.test(maid);
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        if (this.builder.enableEating == null) {
            return IMaidTask.super.enableEating(maid);
        }
        return this.builder.enableEating.test(maid);
    }

    @Override
    public boolean workPointTask(EntityMaid maid) {
        if (this.builder.workPointTask == null) {
            return IMaidTask.super.workPointTask(maid);
        }
        return this.builder.workPointTask.test(maid);
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
    public float searchRadius(EntityMaid maid) {
        if (this.builder.searchRadius < 0) {
            return IMaidTask.super.searchRadius(maid);
        }
        return this.builder.searchRadius;
    }

    public static class Builder {
        private final ResourceLocation id;
        private final ItemStack icon;

        private final List<Pair<Integer, BiFunction<BaseTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>>>> brains = Lists.newArrayList();
        private final List<Pair<Integer, BiFunction<BaseTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>>>> rideBrains = Lists.newArrayList();

        private final List<Pair<String, Predicate<EntityMaid>>> enableConditionDesc = Lists.newArrayList();
        private final List<Pair<String, Predicate<EntityMaid>>> conditionDesc = Lists.newArrayList();

        private @Nullable Predicate<EntityMaid> enable = null;
        private @Nullable Predicate<EntityMaid> enableLookAndRandomWalk = null;
        private @Nullable Predicate<EntityMaid> enablePanic = null;
        private @Nullable Predicate<EntityMaid> enableEating = null;
        private @Nullable Predicate<EntityMaid> workPointTask = null;

        private @Nullable SoundEvent sound;
        private float searchRadius = -1;

        public Builder(ResourceLocation id, ItemStack icon) {
            this.id = id;
            this.icon = icon;
        }

        public Builder addBrain(int priority, BiFunction<BaseTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>> control) {
            this.brains.add(Pair.of(priority, control));
            return this;
        }

        public Builder addRideBrain(int priority, BiFunction<BaseTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>> control) {
            this.rideBrains.add(Pair.of(priority, control));
            return this;
        }

        public Builder addEnableConditionDesc(String languageKey, Predicate<EntityMaid> condition) {
            this.enableConditionDesc.add(Pair.of(languageKey, condition));
            return this;
        }

        public Builder addConditionDesc(String languageKey, Predicate<EntityMaid> condition) {
            this.conditionDesc.add(Pair.of(languageKey, condition));
            return this;
        }

        public Builder enable(Predicate<EntityMaid> enable) {
            this.enable = enable;
            return this;
        }

        public Builder enableLookAndRandomWalk(Predicate<EntityMaid> enableLookAndRandomWalk) {
            this.enableLookAndRandomWalk = enableLookAndRandomWalk;
            return this;
        }

        public Builder enablePanic(Predicate<EntityMaid> enablePanic) {
            this.enablePanic = enablePanic;
            return this;
        }

        public Builder enableEating(Predicate<EntityMaid> enableEating) {
            this.enableEating = enableEating;
            return this;
        }

        public Builder workPoint(Predicate<EntityMaid> workPointTask) {
            this.workPointTask = workPointTask;
            return this;
        }

        public Builder sound(SoundEvent sound) {
            this.sound = sound;
            return this;
        }

        public Builder searchRadius(float searchRadius) {
            this.searchRadius = searchRadius;
            return this;
        }
    }
}

