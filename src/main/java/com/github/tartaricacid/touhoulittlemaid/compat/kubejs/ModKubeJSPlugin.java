package com.github.tartaricacid.touhoulittlemaid.compat.kubejs;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.recipe.AltarInput;
import com.github.tartaricacid.touhoulittlemaid.compat.kubejs.recipe.AltarRecipeSchema;
import com.github.tartaricacid.touhoulittlemaid.init.InitRecipes;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class ModKubeJSPlugin extends KubeJSPlugin {
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
