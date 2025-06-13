package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidAttackStrafingTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidRangedWalkToTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidShootTargetTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidUseShieldTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class MaidBrainsToolJS {
    @Info(value = "Creates a behavior control for starting an attack on a target.", params = {
            @Param(name = "canAttack", value = "A predicate to check if the maid can attack."),
            @Param(name = "targetFinder", value = "A function to find the target for the attack, returning an Optional of LivingEntity.")
    })
    public static BehaviorControl<EntityMaid> startAttacking(Predicate<EntityMaid> canAttack, Function<EntityMaid, Optional<? extends LivingEntity>> targetFinder) {
        return StartAttacking.create(canAttack, targetFinder);
    }

    @Info(value = "Creates a behavior control for stopping an attack if the target is invalid.", params = {
            @Param(name = "canStopAttacking", value = "A predicate to check if the maid can stop attacking based on the target.")
    })
    public static BehaviorControl<EntityMaid> stopAttackingIfTargetInvalid(Predicate<LivingEntity> canStopAttacking) {
        return StopAttackingIfTargetInvalid.create(canStopAttacking);
    }

    @Info(value = "Creates a behavior control for setting a walk target from the attack target if the target is out of reach.", params = {
            @Param(name = "speedModifier", value = "The speed modifier for the walk target.")
    })
    public static BehaviorControl<Mob> setWalkTargetFromAttackTargetIfTargetOutOfReach(float speedModifier) {
        return SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(speedModifier);
    }

    @Info(value = """
            Creates a behavior control for setting a walk target from the attack target, mainly used for long-range shooting.
            For melee combat, please use setWalkTargetFromAttackTargetIfTargetOutOfReach()
            """,
            params = {
                    @Param(name = "speedModifier", value = "The speed modifier for the walk target.")
            })
    public static BehaviorControl<EntityMaid> maidRangedWalkToTarget(float speedModifier) {
        return MaidRangedWalkToTarget.create(speedModifier);
    }

    @Info(value = "Creates a behavior control for melee attacks with a specified cooldown between attacks.", params = {
            @Param(name = "cooldownBetweenAttacks", value = "The cooldown time in ticks between melee attacks.")
    })
    public static BehaviorControl<Mob> meleeAttack(int cooldownBetweenAttacks) {
        return MeleeAttack.create(cooldownBetweenAttacks);
    }

    @Info("Creates a behavior control for making the maid strafe while in range attacking.")
    public static BehaviorControl<EntityMaid> maidAttackStrafingTask() {
        return new MaidAttackStrafingTask();
    }

    @Info(value = "Creates a behavior control for making the maid shoot at a target with a specified cooldown between shots.", params = {
            @Param(name = "attackCooldown", value = "The cooldown time in ticks between ranged attacks.")
    })
    public static BehaviorControl<EntityMaid> maidShootTargetTask(int attackCooldown) {
        return new MaidShootTargetTask(attackCooldown);
    }

    @Info("Creates a behavior control for making the maid use a shield when attacked.")
    public static MaidUseShieldTask maidUseShieldTask() {
        return new MaidUseShieldTask();
    }
}
