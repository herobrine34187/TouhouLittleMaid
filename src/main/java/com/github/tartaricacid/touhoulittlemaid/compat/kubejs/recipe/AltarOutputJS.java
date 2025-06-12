package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.recipe;

import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.util.EntityCraftingHelper;
import com.google.gson.JsonParseException;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

public final class AltarOutputJS {
    @Info("Converting KubeJS' Item object to the altar's' output item object")
    public static EntityCraftingHelper.Output item(ItemStack stack) {
        EntityType<ItemEntity> item = EntityType.ITEM;
        CompoundTag data = new CompoundTag();
        data.put("Item", stack.save(new CompoundTag()));
        return new EntityCraftingHelper.Output(item, data);
    }

    @Info("Directly converting an entity ID and extra NBT data to the altar's output entity object")
    public static EntityCraftingHelper.Output entity(String entityId, CompoundTag data) {
        EntityType<?> value = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityId));
        if (value == null) {
            throw new JsonParseException("Entity Type Not Found: " + entityId);
        }
        return new EntityCraftingHelper.Output(value, data);
    }

    @Info("Directly converting an entity ID to the altar's output entity object with empty NBT data")
    public static EntityCraftingHelper.Output entity(String entityId) {
        return entity(entityId, new CompoundTag());
    }

    @Info("Spawn a new maid with a cake box")
    public static EntityCraftingHelper.Output spawnMaidWithBox() {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(InitEntities.MAID.get());
        if (key == null) {
            throw new JsonParseException("Maid Entity Type Not Found");
        }
        CompoundTag data = new CompoundTag();
        ListTag passengers = new ListTag();
        CompoundTag maid = new CompoundTag();
        maid.putString("id", key.toString());
        passengers.add(maid);
        data.put("Passengers", passengers);
        return new EntityCraftingHelper.Output(InitEntities.MAID.get(), data);
    }

    @Info("Reborn the maid from film, the ingredient must have a film")
    public static EntityCraftingHelper.Output rebornMaid() {
        return new EntityCraftingHelper.Output(InitEntities.MAID.get(), new CompoundTag(), Ingredient.of(InitItems.FILM.get()), "MaidInfo");
    }
}
