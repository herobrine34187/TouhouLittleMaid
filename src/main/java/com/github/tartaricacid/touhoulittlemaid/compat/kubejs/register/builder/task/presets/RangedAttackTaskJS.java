package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task.presets;

import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidAttackStrafingAnyItemTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidRangedWalkToTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidShootTargetAnyItemTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class RangedAttackTaskJS implements IRangedAttackTask {
    private final Builder builder;

    public RangedAttackTaskJS(Builder builder) {
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
        if (this.builder.sound == null) {
            return SoundUtil.attackSound(maid, InitSounds.MAID_RANGE_ATTACK.get(), 0.5f);
        }
        return this.builder.sound;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(m ->
                isWeapon(m, m.getMainHandItem()), IAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create(target ->
                !isWeapon(maid, maid.getMainHandItem()) || maid.distanceTo(target) > this.searchRadius(maid));
        BehaviorControl<EntityMaid> moveToTargetTask = MaidRangedWalkToTarget.create(0.6f);
        BehaviorControl<EntityMaid> maidAttackStrafingTask = new MaidAttackStrafingAnyItemTask(stack ->
                isWeapon(maid, stack), builder.projectileRange);
        BehaviorControl<EntityMaid> shootTargetTask = new MaidShootTargetAnyItemTask(2, builder.chargeDurationTick, stack ->
                isWeapon(maid, stack));

        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = Lists.newArrayList(
                Pair.of(5, supplementedTask),
                Pair.of(5, findTargetTask),
                Pair.of(5, moveToTargetTask),
                Pair.of(5, maidAttackStrafingTask),
                Pair.of(5, shootTargetTask));

        for (var pair : this.builder.brains) {
            tasks.add(Pair.of(pair.getFirst(), pair.getSecond().apply(this, maid)));
        }
        return tasks;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(m ->
                isWeapon(m, m.getMainHandItem()), IAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create(target ->
                !isWeapon(maid, maid.getMainHandItem()) || maid.distanceTo(target) > this.searchRadius(maid));
        BehaviorControl<EntityMaid> shootTargetTask = new MaidShootTargetAnyItemTask(2, builder.chargeDurationTick, stack ->
                isWeapon(maid, stack));

        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = Lists.newArrayList(
                Pair.of(5, supplementedTask),
                Pair.of(5, findTargetTask),
                Pair.of(5, shootTargetTask));

        for (var pair : this.builder.rideBrains) {
            tasks.add(Pair.of(pair.getFirst(), pair.getSecond().apply(this, maid)));
        }
        return tasks;
    }

    @Override
    public boolean isEnable(EntityMaid maid) {
        if (this.builder.enable == null) {
            return true;
        }
        return this.builder.enable.apply(maid);
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        if (this.builder.enableLookAndRandomWalk == null) {
            return true;
        }
        return this.builder.enableLookAndRandomWalk.apply(maid);
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
    public boolean canAttack(EntityMaid maid, LivingEntity target) {
        if (this.builder.canAttack == null) {
            return IRangedAttackTask.super.canAttack(maid, target);
        }
        return this.builder.canAttack.apply(maid, target);
    }

    @Override
    public boolean isWeapon(EntityMaid maid, ItemStack stack) {
        if (this.builder.isWeapon == null) {
            return false;
        }
        return this.builder.isWeapon.apply(maid, stack);
    }

    @Override
    public void performRangedAttack(EntityMaid shooter, LivingEntity target, float distanceFactor) {
        if (this.builder.performRangedAttack != null) {
            this.builder.performRangedAttack.accept(shooter, target, distanceFactor);
        }
    }

    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        return TARGET_CONDITIONS.range(this.searchRadius(maid)).test(maid, target);
    }

    @Override
    public AABB searchDimension(EntityMaid maid) {
        if (isWeapon(maid, maid.getMainHandItem())) {
            float searchRange = this.searchRadius(maid);
            if (maid.hasRestriction()) {
                return new AABB(maid.getRestrictCenter()).inflate(searchRange);
            } else {
                return maid.getBoundingBox().inflate(searchRange);
            }
        }
        return IRangedAttackTask.super.searchDimension(maid);
    }

    @Override
    public float searchRadius(EntityMaid maid) {
        if (this.builder.searchRadius > 0) {
            return this.builder.searchRadius;
        }
        return IRangedAttackTask.super.searchRadius(maid);
    }

    @FunctionalInterface
    public interface TriConsumer {
        void accept(EntityMaid shooter, LivingEntity target, float distanceFactor);
    }

    public static class Builder {
        private final ResourceLocation id;
        private final ItemStack icon;

        private final List<Pair<Integer, BiFunction<RangedAttackTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>>>> brains = Lists.newArrayList();
        private final List<Pair<Integer, BiFunction<RangedAttackTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>>>> rideBrains = Lists.newArrayList();

        private final List<Pair<String, Predicate<EntityMaid>>> enableConditionDesc = Lists.newArrayList();
        private final List<Pair<String, Predicate<EntityMaid>>> conditionDesc = Lists.newArrayList();

        private @Nullable Function<EntityMaid, Boolean> enable = null;
        private @Nullable Function<EntityMaid, Boolean> enableLookAndRandomWalk = null;
        private @Nullable BiFunction<EntityMaid, LivingEntity, Boolean> canAttack = null;
        private @Nullable BiFunction<EntityMaid, ItemStack, Boolean> isWeapon = null;

        private @Nullable TriConsumer performRangedAttack = null;

        private @Nullable SoundEvent sound;
        private float searchRadius = -1f;
        // 默认投射物射程
        private float projectileRange = 16f;
        // 默认充能时间为 20 tick
        private int chargeDurationTick = 20;

        public Builder(ResourceLocation id, ItemStack icon) {
            this.id = id;
            this.icon = icon;
        }

        public Builder addBrain(int priority, BiFunction<RangedAttackTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>> control) {
            this.brains.add(Pair.of(priority, control));
            return this;
        }

        public Builder addRideBrain(int priority, BiFunction<RangedAttackTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>> control) {
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

        public Builder sound(SoundEvent sound) {
            this.sound = sound;
            return this;
        }

        public Builder canAttack(BiFunction<EntityMaid, LivingEntity, Boolean> canAttack) {
            this.canAttack = canAttack;
            return this;
        }

        public Builder isWeapon(BiFunction<EntityMaid, ItemStack, Boolean> isWeapon) {
            this.isWeapon = isWeapon;
            return this;
        }

        public Builder searchRadius(float radius) {
            this.searchRadius = radius;
            return this;
        }

        public Builder projectileRange(float projectileRange) {
            this.projectileRange = projectileRange;
            return this;
        }

        public Builder chargeDurationTick(int chargeDurationTick) {
            this.chargeDurationTick = chargeDurationTick;
            return this;
        }

        public Builder performRangedAttack(TriConsumer performRangedAttack) {
            this.performRangedAttack = performRangedAttack;
            return this;
        }
    }
}

