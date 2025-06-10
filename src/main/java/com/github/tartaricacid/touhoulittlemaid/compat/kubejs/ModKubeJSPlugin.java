package com.github.tartaricacid.touhoulittlemaid.compat.kubejs;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.event.RegisterKubeJSEvent;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.event.CommonEventsPost;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.event.MaidEvents;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.recipe.AltarInput;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.recipe.AltarRecipeSchema;
import com.github.tartaricacid.touhoulittlemaid.init.InitRecipes;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraftforge.common.MinecraftForge;

public class ModKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(new CommonEventsPost());
    }

    @Override
    public void registerEvents() {
        EventGroup group = MaidEvents.GROUP;
        MinecraftForge.EVENT_BUS.post(new RegisterKubeJSEvent(group));
        group.register();
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace(TouhouLittleMaid.MOD_ID)
                .register(InitRecipes.ALTAR_RECIPE_SERIALIZER.getId().getPath(), AltarRecipeSchema.SCHEMA);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("MaidAltarInput", AltarInput.class);
    }
}
