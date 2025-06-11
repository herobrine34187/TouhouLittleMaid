package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class CustomKubeJSAttackTask implements IAttackTask {
    private final Builder builder;

    public CustomKubeJSAttackTask(Builder builder) {
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
        return this.builder.brains;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        return this.builder.rideBrains;
    }

    @Override
    public boolean isEnable(EntityMaid maid) {
        if (this.builder.enable == null) {
            return IAttackTask.super.isEnable(maid);
        }
        return this.builder.enable.apply(maid);
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        if (this.builder.enableLookAndRandomWalk == null) {
            return IAttackTask.super.enableLookAndRandomWalk(maid);
        }
        return this.builder.enableLookAndRandomWalk.apply(maid);
    }

    @Override
    public boolean enablePanic(EntityMaid maid) {
        if (this.builder.enablePanic == null) {
            return IAttackTask.super.enablePanic(maid);
        }
        return this.builder.enablePanic.apply(maid);
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        if (this.builder.enableEating == null) {
            return IAttackTask.super.enableEating(maid);
        }
        return this.builder.enableEating.apply(maid);
    }

    @Override
    public boolean workPointTask(EntityMaid maid) {
        if (this.builder.workPointTask == null) {
            return IAttackTask.super.workPointTask(maid);
        }
        return this.builder.workPointTask.apply(maid);
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
            return IAttackTask.super.searchRadius(maid);
        }
        return this.builder.searchRadius;
    }

    @Override
    public boolean canAttack(EntityMaid maid, LivingEntity target) {
        if (this.builder.canAttack == null) {
            return IAttackTask.super.canAttack(maid, target);
        }
        return this.builder.canAttack.apply(maid, target);
    }

    @Override
    public boolean hasExtraAttack(EntityMaid maid, Entity target) {
        if (this.builder.hasExtraAttack == null) {
            return IAttackTask.super.hasExtraAttack(maid, target);
        }
        return this.builder.hasExtraAttack.apply(maid, target);
    }

    @Override
    public boolean doExtraAttack(EntityMaid maid, Entity target) {
        if (this.builder.doExtraAttack == null) {
            return IAttackTask.super.doExtraAttack(maid, target);
        }
        return this.builder.doExtraAttack.apply(maid, target);
    }

    @Override
    public boolean isWeapon(EntityMaid maid, ItemStack stack) {
        if (this.builder.isWeapon == null) {
            return IAttackTask.super.isWeapon(maid, stack);
        }
        return this.builder.isWeapon.apply(maid, stack);
    }

    public static class Builder {
        private final ResourceLocation id;
        private final ItemStack icon;

        private final List<Pair<Integer, BehaviorControl<? super EntityMaid>>> brains = Lists.newArrayList();
        private final List<Pair<Integer, BehaviorControl<? super EntityMaid>>> rideBrains = Lists.newArrayList();

        private final List<Pair<String, Predicate<EntityMaid>>> enableConditionDesc = Lists.newArrayList();
        private final List<Pair<String, Predicate<EntityMaid>>> conditionDesc = Lists.newArrayList();

        private @Nullable Function<EntityMaid, Boolean> enable = null;
        private @Nullable Function<EntityMaid, Boolean> enableLookAndRandomWalk = null;
        private @Nullable Function<EntityMaid, Boolean> enablePanic = null;
        private @Nullable Function<EntityMaid, Boolean> enableEating = null;
        private @Nullable Function<EntityMaid, Boolean> workPointTask = null;

        private @Nullable BiFunction<EntityMaid, LivingEntity, Boolean> canAttack = null;
        private @Nullable BiFunction<EntityMaid, Entity, Boolean> hasExtraAttack = null;
        private @Nullable BiFunction<EntityMaid, Entity, Boolean> doExtraAttack = null;
        private @Nullable BiFunction<EntityMaid, ItemStack, Boolean> isWeapon = null;

        private @Nullable SoundEvent sound;
        private float searchRadius = -1;

        public Builder(ResourceLocation id, ItemStack icon) {
            this.id = id;
            this.icon = icon;
        }

        public Builder addBrain(int priority, BehaviorControl<? super EntityMaid> control) {
            this.brains.add(Pair.of(priority, control));
            return this;
        }

        public Builder addRideBrain(int priority, BehaviorControl<? super EntityMaid> control) {
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

        public Builder enable(Function<EntityMaid, Boolean> enable) {
            this.enable = enable;
            return this;
        }

        public Builder enableLookAndRandomWalk(Function<EntityMaid, Boolean> enableLookAndRandomWalk) {
            this.enableLookAndRandomWalk = enableLookAndRandomWalk;
            return this;
        }

        public Builder enablePanic(Function<EntityMaid, Boolean> enablePanic) {
            this.enablePanic = enablePanic;
            return this;
        }

        public Builder enableEating(Function<EntityMaid, Boolean> enableEating) {
            this.enableEating = enableEating;
            return this;
        }

        public Builder workPoint(Function<EntityMaid, Boolean> workPointTask) {
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

        public Builder canAttack(BiFunction<EntityMaid, LivingEntity, Boolean> canAttack) {
            this.canAttack = canAttack;
            return this;
        }

        public Builder hasExtraAttack(BiFunction<EntityMaid, Entity, Boolean> hasExtraAttack) {
            this.hasExtraAttack = hasExtraAttack;
            return this;
        }

        public Builder doExtraAttack(BiFunction<EntityMaid, Entity, Boolean> doExtraAttack) {
            this.doExtraAttack = doExtraAttack;
            return this;
        }

        public Builder isWeapon(BiFunction<EntityMaid, ItemStack, Boolean> isWeapon) {
            this.isWeapon = isWeapon;
            return this;
        }
    }
}

