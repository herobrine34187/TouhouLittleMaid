package com.github.tartaricacid.touhoulittlemaid.client.input;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.STTCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.ai.service.stt.STTConfig;
import com.github.tartaricacid.touhoulittlemaid.ai.service.stt.STTSite;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static com.github.tartaricacid.touhoulittlemaid.client.event.PressAIChatKeyEvent.CAN_CHAT_MAID_IDS;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class STTChatKey {
    public static final KeyMapping STT_CHAT_KEY = new KeyMapping("key.touhou_little_maid.stt_chat.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.category.touhou_little_maid");

    @SubscribeEvent
    public static void onSttChatPress(InputEvent.Key event) {
        if (STT_CHAT_KEY.matches(event.getKey(), event.getScanCode())) {
            if (!AIConfig.LLM_ENABLED.get()) {
                return;
            }
            if (!AIConfig.STT_ENABLED.get()) {
                return;
            }
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || player.isSpectator()) {
                return;
            }
            if (!isInGame()) {
                return;
            }
            if (event.getAction() == GLFW.GLFW_PRESS) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(InitSounds.RECORDING_START.get(), 1f));
                getNearestMaid(player, STTChatKey::sttStart);
                return;
            }
            if (event.getAction() == GLFW.GLFW_RELEASE) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(InitSounds.RECORDING_END.get(), 1f));
                getNearestMaid(player, STTChatKey::sttStop);
            }
        }
    }

    private static void getNearestMaid(LocalPlayer player, Consumer<EntityMaid> consumer) {
        Level level = player.level;
        // TODO: 将搜索范围改成可配置的
        AABB aabb = player.getBoundingBox().inflate(12);
        List<EntityMaid> maids = level.getEntitiesOfClass(EntityMaid.class, aabb,
                maid -> maid.isOwnedBy(player) && maid.isAlive() &&
                        CAN_CHAT_MAID_IDS.contains(maid.getModelId()));
        maids.sort(Comparator.comparingDouble(maid -> maid.distanceToSqr(player)));
        if (maids.isEmpty()) {
            player.sendSystemMessage(Component.translatable("ai.touhou_little_maid.stt.content.no_maid_found"));
        } else {
            consumer.accept(maids.get(0));
        }
    }

    private static boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (mc.getOverlay() != null) {
            return false;
        }
        // 不能打开任何 GUI
        if (mc.screen != null) {
            return false;
        }
        // 当前窗口捕获鼠标操作
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        // 选择了当前窗口
        return mc.isWindowActive();
    }

    private static void sttStart(EntityMaid maid) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        STTSite sttSite = AvailableSites.getSTTSite(AIConfig.STT_TYPE.get().getName());
        if (!sttSite.enabled()) {
            player.sendSystemMessage(Component.translatable("ai.touhou_little_maid.chat.stt.empty"));
            return;
        }
        STTConfig config = new STTConfig();
        STTCallback callback = new STTCallback(player, maid);
        sttSite.client().startRecord(config, callback);
    }

    private static void sttStop(EntityMaid maid) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        STTSite sttSite = AvailableSites.getSTTSite(AIConfig.STT_TYPE.get().getName());
        if (sttSite.enabled()) {
            STTConfig config = new STTConfig();
            STTCallback callback = new STTCallback(player, maid);
            sttSite.client().stopRecord(config, callback);
        }
    }
}
