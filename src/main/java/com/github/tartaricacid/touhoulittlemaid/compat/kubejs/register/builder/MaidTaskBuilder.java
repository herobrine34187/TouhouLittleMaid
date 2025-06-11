package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task.CustomKubeJSAttackTask;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.register.builder.task.CustomKubeJSTask;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.google.common.collect.Lists;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MaidTaskBuilder {
    private final List<IMaidTask> tasks = Lists.newArrayList();

    public CustomKubeJSTask.Builder normalTask(ResourceLocation id, ItemStack icon) {
        CustomKubeJSTask.Builder builder = new CustomKubeJSTask.Builder(id, icon);
        tasks.add(new CustomKubeJSTask(builder));
        return builder;
    }

    public CustomKubeJSAttackTask.Builder attackTask(ResourceLocation id, ItemStack icon) {
        CustomKubeJSAttackTask.Builder builder = new CustomKubeJSAttackTask.Builder(id, icon);
        tasks.add(new CustomKubeJSAttackTask(builder));
        return builder;
    }

    @HideFromJS
    public void register(TaskManager manager) {
        this.tasks.forEach(manager::add);
        this.tasks.clear();
    }
}
