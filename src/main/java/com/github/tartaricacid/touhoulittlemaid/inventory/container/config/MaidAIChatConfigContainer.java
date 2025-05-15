package com.github.tartaricacid.touhoulittlemaid.inventory.container.config;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.site.ClientAvailableSitesSync;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.AbstractMaidContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;

public class MaidAIChatConfigContainer extends AbstractMaidContainer {
    public static final MenuType<MaidAIChatConfigContainer> TYPE = IForgeMenuType.create(MaidAIChatConfigContainer::create);

    private static final int PLAYER_INVENTORY_SIZE = 27;

    private final CompoundTag configData;
    private final Map<String, Map<String, String>> llmSites;
    private final Map<String, Map<String, String>> ttsSites;

    public MaidAIChatConfigContainer(int id, Inventory inventory, int entityId,
                                     @Nullable CompoundTag configData,
                                     Map<String, Map<String, String>> llmSites,
                                     Map<String, Map<String, String>> ttsSites) {
        super(TYPE, id, inventory, entityId);
        this.configData = configData;
        this.llmSites = llmSites;
        this.ttsSites = ttsSites;
    }

    public static MenuProvider create(EntityMaid maid) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Maid AI Chat Config Container");
            }

            @Override
            public AbstractContainerMenu createMenu(int index, Inventory playerInventory, Player player) {
                int entityId = maid.getId();
                CompoundTag configData = maid.getAiChatManager().writeToTag(new CompoundTag());
                return new MaidAIChatConfigContainer(index, playerInventory, entityId, configData,
                        ClientAvailableSitesSync.getClientLLMSites(),
                        ClientAvailableSitesSync.getClientTTSSites());
            }
        };
    }

    @NotNull
    private static MaidAIChatConfigContainer create(int windowId, Inventory inv, FriendlyByteBuf data) {
        int entityId = data.readInt();
        CompoundTag configData = data.readNbt();
        var sites = ClientAvailableSitesSync.readFromNetwork(data);
        return new MaidAIChatConfigContainer(windowId, inv, entityId, configData, sites.getLeft(), sites.getRight());
    }

    public CompoundTag getConfigData() {
        return configData;
    }

    public Map<String, Map<String, String>> getLLMSites() {
        return llmSites;
    }

    public Map<String, Map<String, String>> getTTSSites() {
        return ttsSites;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack stack1 = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack2 = slot.getItem();
            stack1 = stack2.copy();
            if (index < PLAYER_INVENTORY_SIZE) {
                if (!this.moveItemStackTo(stack2, PLAYER_INVENTORY_SIZE, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack2, 0, PLAYER_INVENTORY_SIZE, true)) {
                return ItemStack.EMPTY;
            }
            if (stack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack1;
    }
}
