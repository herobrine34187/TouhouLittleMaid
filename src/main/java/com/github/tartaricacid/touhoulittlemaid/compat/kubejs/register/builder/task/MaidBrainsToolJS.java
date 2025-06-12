package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidUseShieldTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class MaidBrainsToolJS {
    public static BehaviorControl<EntityMaid> startAttacking(Predicate<EntityMaid> canAttack, Function<EntityMaid, Optional<? extends LivingEntity>> targetFinder) {
        return StartAttacking.create(canAttack, targetFinder);
    }

    public static BehaviorControl<EntityMaid> startAttacking(CustomKubeJSAttackTask task) {
        return StartAttacking.create(maid -> task.isWeapon(maid, maid.getMainHandItem()), IAttackTask::findFirstValidAttackTarget);
    }

    public static BehaviorControl<EntityMaid> stopAttackingIfTargetInvalid(Predicate<LivingEntity> canStopAttacking) {
        return StopAttackingIfTargetInvalid.create(canStopAttacking);
    }

    public static BehaviorControl<EntityMaid> stopAttackingIfTargetInvalid(CustomKubeJSAttackTask task, EntityMaid maid) {
        return StopAttackingIfTargetInvalid.create(target -> !task.isWeapon(maid, maid.getMainHandItem()) || maid.distanceTo(target) > maid.getRestrictRadius());
    }

    public static BehaviorControl<Mob> setWalkTargetFromAttackTargetIfTargetOutOfReach(float speedModifier) {
        return SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(speedModifier);
    }

    public static BehaviorControl<Mob> meleeAttack(int cooldownBetweenAttacks) {
        return MeleeAttack.create(cooldownBetweenAttacks);
    }

    public static MaidUseShieldTask maidUseShieldTask() {
        return new MaidUseShieldTask();
    }
}
