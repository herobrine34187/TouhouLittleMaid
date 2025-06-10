package com.github.tartaricacid.touhoulittlemaid.compat.kubejs.recipe;

import com.github.tartaricacid.touhoulittlemaid.util.EntityCraftingHelper;
import com.google.gson.JsonParseException;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class AltarInput {
    @Info("Converting KubeJS' Item object to the altar's' output item object")
    public static EntityCraftingHelper.Output itemstack(ItemStack stack) {
        EntityType<ItemEntity> item = EntityType.ITEM;
        CompoundTag data = new CompoundTag();
        data.put("Item", stack.save(new CompoundTag()));
        return new EntityCraftingHelper.Output(item, data);
    }

    @Info("Directly converting an item ID and count to the altar's output item object")
    public static EntityCraftingHelper.Output item(String itemId, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) {
            throw new JsonParseException("Item Not Found: " + itemId);
        }
        return itemstack(new ItemStack(item, count));
    }

    @Info("Directly converting an item ID to the altar's output item object with a default count of 1")
    public static EntityCraftingHelper.Output item(String itemId) {
        return item(itemId, 1);
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
}
