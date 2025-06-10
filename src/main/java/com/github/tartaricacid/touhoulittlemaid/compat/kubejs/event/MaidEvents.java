package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.event;

import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.event.common.*;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface MaidEvents {
    EventGroup GROUP = EventGroup.of("MaidEvents");
    EventHandler ADD_JADE_INFO = GROUP.common("addJadeInfo", () -> AddJadeInfoEventJS.class);
    EventHandler ADD_TOP_INFO = GROUP.common("addTopInfo", () -> AddTopInfoEventJS.class);
    EventHandler INTERACT_MAID = GROUP.common("interactMaid", () -> InteractMaidEventJS.class).hasResult();
    EventHandler MAID_AFTER_EAT = GROUP.common("maidAfterEat", () -> MaidAfterEatEventJS.class);
    EventHandler MAID_ATTACK = GROUP.common("maidAttack", () -> MaidAttackEventJS.class).hasResult();
    EventHandler MAID_DAMAGE = GROUP.common("maidDamage", () -> MaidDamageEventJS.class).hasResult();
    EventHandler MAID_DEATH = GROUP.common("maidDeath", () -> MaidDeathEventJS.class).hasResult();
    EventHandler MAID_EQUIP = GROUP.common("maidEquip", () -> MaidEquipEventJS.class);
    EventHandler MAID_FISHED = GROUP.common("maidFished", () -> MaidFishedEventJS.class);
    EventHandler MAID_HURT = GROUP.common("maidHurt", () -> MaidHurtEventJS.class).hasResult();
    EventHandler MAID_PICKUP_ITEM_RESULT_PRE = GROUP.common("maidPickupItemResultPre", () -> MaidPickupEventJS.ItemResultPre.class).hasResult();
    EventHandler MAID_PICKUP_ITEM_RESULT_POST = GROUP.common("maidPickupItemResultPost", () -> MaidPickupEventJS.ItemResultPost.class);
    EventHandler MAID_PICKUP_EXPERIENCE_RESULT = GROUP.common("maidPickupExperienceResult", () -> MaidPickupEventJS.ExperienceResult.class).hasResult();
    EventHandler MAID_PICKUP_ARROW_RESULT = GROUP.common("maidPickupArrowResult", () -> MaidPickupEventJS.ArrowResult.class).hasResult();
    EventHandler MAID_PICKUP_POWER_POINT_RESULT = GROUP.common("maidPickupPowerPointResult", () -> MaidPickupEventJS.PowerPointResult.class).hasResult();
    EventHandler MAID_PLAY_SOUND = GROUP.common("maidPlaySound", () -> MaidPlaySoundEventJS.class).hasResult();
    EventHandler MAID_TICK = GROUP.common("maidTick", () -> MaidTickEventJS.class).hasResult();
    EventHandler MAID_TASK_ENABLE = GROUP.common("maidTaskEnable", () -> MaidTaskEnableEventJS.class).hasResult();
    EventHandler MAID_TAMED = GROUP.common("maidTamed", () -> MaidTamedEventJS.class);
}
