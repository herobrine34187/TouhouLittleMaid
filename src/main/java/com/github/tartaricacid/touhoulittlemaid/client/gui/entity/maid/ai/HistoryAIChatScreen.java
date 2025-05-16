package com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.Client;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMMessage;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.Role;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.HistoryChatWidget;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.TabIndex;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.ClearMaidAIDataMessage;
import com.github.tartaricacid.touhoulittlemaid.network.message.OpenMaidGuiMessage;
import com.google.common.collect.Lists;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.Deque;
import java.util.List;

public class HistoryAIChatScreen extends Screen {
    private final EntityMaid maid;
    private final ResourceLocation playerSkin;
    private final List<LLMMessage> history = Lists.newArrayList();
    private final List<Renderable> historyWidgets = Lists.newArrayList();

    private double scroll = 0;
    private int maxHeight = 0;
    private int posX = 0;

    public HistoryAIChatScreen(EntityMaid maid) {
        super(Component.literal("Maid History AI Chat Screen"));
        this.maid = maid;
        this.playerSkin = this.getPlayerSkin();
        this.transformMessage();
    }

    @Override
    protected void init() {
        historyWidgets.clear();
        this.posX = this.width / 2 - 75;
        this.maxHeight = 10;
        for (LLMMessage message : this.history) {
            int lineHeight = this.addHistoryWidget(message, posX);
            maxHeight += lineHeight + 5;
        }
        this.addButtons();

        // 让滚动一开始就在中间
        double topMax = this.height / 2.0 - 100;
        double bottomMax = this.height / 2.0 + 100;
        double scrollBottom = scroll + maxHeight;
        if (scroll > topMax) {
            scroll = topMax;
        }
        if (bottomMax > scrollBottom) {
            scroll = bottomMax - maxHeight;
        }
    }

    private void addButtons() {
        MutableComponent clearName = Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.clear_history_chat");
        MutableComponent clearMsg = Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.clear_history_chat.confirm");
        this.addRenderableWidget(Button.builder(clearName, button -> {
            this.getMinecraft().setScreen(new ConfirmScreen(yes -> {
                if (yes) {
                    this.history.clear();
                    this.historyWidgets.clear();
                    this.maid.getAiChatManager().getHistory().getDeque().clear();
                    NetworkHandler.CHANNEL.sendToServer(new ClearMaidAIDataMessage(this.maid.getId()));
                }
                this.getMinecraft().setScreen(this);
            }, clearName, clearMsg));
        }).bounds(posX + 150, this.height / 2 - 20, 120, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> {
            OpenMaidGuiMessage message = new OpenMaidGuiMessage(this.maid.getId(), TabIndex.MAID_AI_CHAT_CONFIG);
            NetworkHandler.CHANNEL.sendToServer(message);
        }).bounds(posX + 150, this.height / 2 + 5, 120, 20).build());
    }

    private int addHistoryWidget(LLMMessage message, int posX) {
        boolean isLeft = message.role() != Role.USER;
        Component msg = Component.literal(message.message());
        int width = Math.min(font.width(msg), 140) + 10;
        int lineHeight = 10 + font.split(msg, 140).size() * font.lineHeight;
        if (isLeft) {
            historyWidgets.add(new HistoryChatWidget(posX - 100, maxHeight,
                    width, lineHeight, msg, playerSkin, message.gameTime(), true));
        } else {
            historyWidgets.add(new HistoryChatWidget(posX + 100 - width, maxHeight,
                    width, lineHeight, msg, playerSkin, message.gameTime(), false));
        }
        return lineHeight;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);

        MutableComponent title = Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.history_chat.title");
        graphics.drawCenteredString(font, title, posX + 210, this.height / 2 - 35, 0xFFFFFF);

        if (this.historyWidgets.isEmpty()) {
            MutableComponent empty = Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.history_chat_is_empty");
            List<FormattedCharSequence> split = font.split(empty, 150);
            for (int i = 0; i < split.size(); i++) {
                int height = i * font.lineHeight;
                graphics.drawCenteredString(font, split.get(i), posX, this.height / 2 - 10 + height, 0xff5555);
            }
        } else {
            graphics.enableScissor(posX - 128, 5, posX + 128, this.height - 5);
            graphics.pose().pushPose();
            graphics.pose().translate(0, scroll, 0);
            for (Renderable renderable : this.historyWidgets) {
                renderable.render(graphics, mouseX, mouseY, partialTicks);
            }
            graphics.pose().popPose();
            graphics.disableScissor();
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double delta) {
        if (delta != 0) {
            double topMax = this.height / 2.0 - 100;
            double bottomMax = this.height / 2.0 + 100;
            double scrollBottom = scroll + maxHeight;
            if (delta < 0 && bottomMax < scrollBottom) {
                scroll += delta * 15;
            }
            if (0 < delta && scroll < topMax) {
                scroll += delta * 15;
            }
        }
        return super.mouseScrolled(pMouseX, pMouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void transformMessage() {
        Deque<LLMMessage> deque = this.maid.getAiChatManager().getHistory().getDeque();
        deque.descendingIterator().forEachRemaining(message -> {
            if (message.role() == Role.USER) {
                this.history.add(message);
            } else if (message.role() == Role.ASSISTANT) {
                ResponseChat responseChat = Client.GSON.fromJson(message.message(), ResponseChat.class);
                this.history.add(new LLMMessage(Role.ASSISTANT, responseChat.getChatText(), message.gameTime()));
            }
        });
    }

    private ResourceLocation getPlayerSkin() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return DefaultPlayerSkin.getDefaultSkin(Util.NIL_UUID);
        }
        return mc.getSkinManager().getInsecureSkinLocation(player.getGameProfile());
    }
}
