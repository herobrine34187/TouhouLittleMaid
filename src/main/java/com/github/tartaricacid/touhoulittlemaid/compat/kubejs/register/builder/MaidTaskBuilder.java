package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task.presets.BaseTaskJS;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task.presets.MeleeTaskJS;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task.presets.RangedAttackTaskJS;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public class MaidTaskBuilder {
    private final List<IMaidTask> tasks = Lists.newArrayList();

    public BaseTaskJS.Builder baseTask(ResourceLocation id, ItemStack icon) {
        BaseTaskJS.Builder builder = new BaseTaskJS.Builder(id, icon);
        tasks.add(new BaseTaskJS(builder));
        return builder;
    }

    public MeleeTaskJS.Builder meleeTask(ResourceLocation id, ItemStack icon) {
        MeleeTaskJS.Builder builder = new MeleeTaskJS.Builder(id, icon);
        tasks.add(new MeleeTaskJS(builder));
        return builder;
    }

    public RangedAttackTaskJS.Builder rangedAttackTask(ResourceLocation id, ItemStack icon) {
        RangedAttackTaskJS.Builder builder = new RangedAttackTaskJS.Builder(id, icon);
        tasks.add(new RangedAttackTaskJS(builder));
        return builder;
    }

    @HideFromJS
    public void register(TaskManager manager) {
        Set<ResourceLocation> existingTasks = Sets.newHashSet();
        for (IMaidTask task : this.tasks) {
            ResourceLocation uid = task.getUid();
            if (!existingTasks.contains(uid)) {
                existingTasks.add(uid);
                manager.add(task);
            }
        }
        this.tasks.clear();
    }
}
