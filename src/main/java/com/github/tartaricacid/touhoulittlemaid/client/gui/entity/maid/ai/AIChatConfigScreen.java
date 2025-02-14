package com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.ai;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.config.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.config.ApiKeyManager;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

// FIXME: 目前只能单机更改本机配置，不能在服务端管理服务端配置。需要解决！
public class AIChatConfigScreen extends Screen {
    private final List<EditBox> configEditBoxes = Lists.newArrayList();
    private final List<Consumer<String>> configOnSave = Lists.newArrayList();
    private final AIChatScreen parent;

    public AIChatConfigScreen(AIChatScreen parent) {
        super(Component.literal("AI Chat Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int posX = this.width / 2;
        int posY = this.height / 2 - 5;

        this.configEditBoxes.clear();
        this.configOnSave.clear();

        this.createCheckbox(posX, posY);
        this.createConfigBox(posX, posY);
        this.createSaveExitButton(posX, posY);
    }

    private void createConfigBox(int posX, int posY) {
        createInputEditBox(posX - 203, posY - 60, ApiKeyManager.getChatApiKey(), true,
                Component.translatable("ai.touhou_little_maid.chat.config.api_key"), ApiKeyManager::setChatApiKey);
        createInputEditBox(posX - 203, posY - 20, AIConfig.CHAT_BASE_URL.get(),
                Component.translatable("ai.touhou_little_maid.chat.config.base_url"), v -> AIConfig.CHAT_BASE_URL.set(v));
        createInputEditBox(posX - 203, posY + 20, AIConfig.CHAT_MODEL.get(),
                Component.translatable("ai.touhou_little_maid.chat.config.model"), v -> AIConfig.CHAT_MODEL.set(v));
        createInputEditBox(posX - 203, posY + 60, AIConfig.CHAT_LANGUAGE.get(),
                Component.translatable("ai.touhou_little_maid.chat.config.language"), v -> AIConfig.CHAT_LANGUAGE.set(v));

        createInputEditBox(posX + 3, posY - 60, ApiKeyManager.getTtsApiKey(), true,
                Component.translatable("ai.touhou_little_maid.tts.config.api_key"), ApiKeyManager::setTtsApiKey);
        createInputEditBox(posX + 3, posY - 20, AIConfig.TTS_BASE_URL.get(),
                Component.translatable("ai.touhou_little_maid.tts.config.base_url"), v -> AIConfig.TTS_BASE_URL.set(v));
        createInputEditBox(posX + 3, posY + 20, AIConfig.TTS_MODEL.get(),
                Component.translatable("ai.touhou_little_maid.tts.config.model"), v -> AIConfig.TTS_MODEL.set(v));
        createInputEditBox(posX + 3, posY + 60, AIConfig.TTS_LANGUAGE.get(),
                Component.translatable("ai.touhou_little_maid.tts.config.language"), v -> AIConfig.TTS_LANGUAGE.set(v));
    }

    private void createCheckbox(int posX, int posY) {
        this.addRenderableWidget(new Checkbox(posX - 203, posY - 100, 20, 20,
                Component.translatable("ai.touhou_little_maid.chat.config.enable"), AIConfig.CHAT_ENABLED.get()) {
            @Override
            public void onPress() {
                super.onPress();
                AIConfig.CHAT_ENABLED.set(this.selected());
            }
        });

        this.addRenderableWidget(new Checkbox(posX + 3, posY - 100, 20, 20,
                Component.translatable("ai.touhou_little_maid.tts.config.enable"), AIConfig.TTS_ENABLED.get()) {
            @Override
            public void onPress() {
                super.onPress();
                AIConfig.TTS_ENABLED.set(this.selected());
            }
        });
    }

    private void createSaveExitButton(int posX, int posY) {
        this.addRenderableWidget(Button.builder(Component.translatable("button.touhou_little_maid.maid.return"), b -> this.getMinecraft().setScreen(parent))
                .pos(posX - 205, posY + 90).size(204, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.save"), b -> {
            for (int i = 0; i < this.configOnSave.size(); i++) {
                EditBox editBox = this.configEditBoxes.get(i);
                this.configOnSave.get(i).accept(editBox.getValue());
            }
            this.getMinecraft().setScreen(parent);
        }).pos(posX + 1, posY + 90).size(204, 20).build());
    }

    @Override
    public void resize(Minecraft mc, int pWidth, int pHeight) {
        List<String> values = Lists.newArrayList();
        this.configEditBoxes.forEach(editBox -> values.add(editBox.getValue()));
        super.resize(mc, pWidth, pHeight);
        IntStream.range(0, values.size()).forEach(index -> this.configEditBoxes.get(index).setValue(values.get(index)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.configEditBoxes.forEach(editBox -> {
            Component message = editBox.getMessage();
            graphics.drawString(font, message, editBox.getX() + 2, editBox.getY() - 12, 0xFFFFFF);
            editBox.render(graphics, mouseX, mouseY, partialTick);
        });
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {
        this.configEditBoxes.forEach(EditBox::tick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (EditBox configEditBox : this.configEditBoxes) {
            if (configEditBox.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(configEditBox);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void createInputEditBox(int posX, int posY, String value, Component name, Consumer<String> save) {
        createInputEditBox(posX, posY, value, false, name, save);
    }

    private void createInputEditBox(int posX, int posY, String value, boolean encrypt, Component name, Consumer<String> save) {
        EditBox input = new EditBox(this.getMinecraft().fontFilterFishy, posX, posY, 200, 20, name);
        if (encrypt) {
            input.setFormatter((text, index) -> {
                String repeat = StringUtils.repeat('*', text.length());
                return FormattedCharSequence.forward(repeat, Style.EMPTY);
            });
        }
        input.setMaxLength(128);
        input.setValue(value);
        this.addWidget(input);
        this.configEditBoxes.add(input);
        this.configOnSave.add(save);
    }
}
