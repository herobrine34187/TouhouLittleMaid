package com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button;

import com.github.tartaricacid.touhoulittlemaid.client.download.InfoGetManager;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.ModelDownloadGui;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.TipsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class MaidDownloadButton extends ImageButton {
    public MaidDownloadButton(int pX, int pY, ResourceLocation texture, EntityMaid maid) {
        super(pX, pY, 41, 20, 0, 86, 20, texture, (b) -> {
            InfoGetManager.STATUE = InfoGetManager.Statue.NOT_UPDATE;
            Minecraft.getInstance().setScreen(new ModelDownloadGui(maid));
        });
    }

    public void renderExtraTips(GuiGraphics graphics) {
        TipsHelper.renderTips(graphics, this, getText());
    }

    private MutableComponent getText() {
        if (InfoGetManager.STATUE == InfoGetManager.Statue.FIRST) {
            return Component.translatable("gui.touhou_little_maid.button.model_download.statue.first");
        }
        if (InfoGetManager.STATUE == InfoGetManager.Statue.UPDATE) {
            return Component.translatable("gui.touhou_little_maid.button.model_download.statue.update");
        }
        return Component.empty();
    }
}
