package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task.presets;

import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidUseShieldTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class MeleeTaskJS implements IAttackTask {
    private final Builder builder;

    public MeleeTaskJS(Builder builder) {
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
            return SoundUtil.attackSound(maid, InitSounds.MAID_ATTACK.get(), 0.5f);
        }
        return this.builder.sound;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(m ->
                isWeapon(m, m.getMainHandItem()), IAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create(target ->
                !isWeapon(maid, maid.getMainHandItem()) || maid.distanceTo(target) > maid.getRestrictRadius());
        BehaviorControl<Mob> moveToTargetTask = SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(0.6f);
        BehaviorControl<Mob> attackTargetTask = MeleeAttack.create(20);
        MaidUseShieldTask maidUseShieldTask = new MaidUseShieldTask();

        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = Lists.newArrayList(
                Pair.of(5, supplementedTask),
                Pair.of(5, findTargetTask),
                Pair.of(5, moveToTargetTask),
                Pair.of(5, attackTargetTask),
                Pair.of(5, maidUseShieldTask));

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
                !isWeapon(maid, maid.getMainHandItem()) || maid.distanceTo(target) > maid.getRestrictRadius());
        BehaviorControl<Mob> attackTargetTask = MeleeAttack.create(20);
        MaidUseShieldTask maidUseShieldTask = new MaidUseShieldTask();

        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = Lists.newArrayList(
                Pair.of(5, supplementedTask),
                Pair.of(5, findTargetTask),
                Pair.of(5, attackTargetTask),
                Pair.of(5, maidUseShieldTask));

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
            return IAttackTask.super.canAttack(maid, target);
        }
        return this.builder.canAttack.apply(maid, target);
    }

    @Override
    public boolean hasExtraAttack(EntityMaid maid, Entity target) {
        if (this.builder.hasExtraAttack == null) {
            return false;
        }
        return this.builder.hasExtraAttack.apply(maid, target);
    }

    @Override
    public boolean doExtraAttack(EntityMaid maid, Entity target) {
        if (this.builder.doExtraAttack == null) {
            return false;
        }
        return this.builder.doExtraAttack.apply(maid, target);
    }

    @Override
    public boolean isWeapon(EntityMaid maid, ItemStack stack) {
        if (this.builder.isWeapon == null) {
            return false;
        }
        return this.builder.isWeapon.apply(maid, stack);
    }

    public static class Builder {
        private final ResourceLocation id;
        private final ItemStack icon;

        private final List<Pair<Integer, BiFunction<MeleeTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>>>> brains = Lists.newArrayList();
        private final List<Pair<Integer, BiFunction<MeleeTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>>>> rideBrains = Lists.newArrayList();

        private final List<Pair<String, Predicate<EntityMaid>>> enableConditionDesc = Lists.newArrayList();
        private final List<Pair<String, Predicate<EntityMaid>>> conditionDesc = Lists.newArrayList();

        private @Nullable Function<EntityMaid, Boolean> enable = null;
        private @Nullable Function<EntityMaid, Boolean> enableLookAndRandomWalk = null;

        private @Nullable BiFunction<EntityMaid, LivingEntity, Boolean> canAttack = null;
        private @Nullable BiFunction<EntityMaid, Entity, Boolean> hasExtraAttack = null;
        private @Nullable BiFunction<EntityMaid, Entity, Boolean> doExtraAttack = null;
        private @Nullable BiFunction<EntityMaid, ItemStack, Boolean> isWeapon = null;

        private @Nullable SoundEvent sound;

        public Builder(ResourceLocation id, ItemStack icon) {
            this.id = id;
            this.icon = icon;
        }

        public Builder addBrain(int priority, BiFunction<MeleeTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>> control) {
            this.brains.add(Pair.of(priority, control));
            return this;
        }

        public Builder addRideBrain(int priority, BiFunction<MeleeTaskJS, EntityMaid, BehaviorControl<? super EntityMaid>> control) {
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

