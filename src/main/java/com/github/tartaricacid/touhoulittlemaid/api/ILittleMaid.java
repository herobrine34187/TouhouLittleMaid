package com.github.tartaricacid.touhoulittlemaid.api;

import com.github.tartaricacid.touhoulittlemaid.ai.service.SerializerRegister;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.FunctionCallRegister;
import com.github.tartaricacid.touhoulittlemaid.block.multiblock.MultiBlockManager;
import com.github.tartaricacid.touhoulittlemaid.client.animation.HardcodedAnimationManger;
import com.github.tartaricacid.touhoulittlemaid.client.overlay.MaidTipsOverlay;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.GeckoEntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.debug.target.DebugTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.BackpackManager;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleRegister;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
import com.github.tartaricacid.touhoulittlemaid.entity.item.control.BroomControlManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.crop.SpecialCropManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.MaidMealManager;
import com.github.tartaricacid.touhoulittlemaid.inventory.chest.ChestManager;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface ILittleMaid {
    /**
     * 为物品绑定女仆饰品属性
     *
     * @param manager 注册器
     */
    default void bindMaidBauble(BaubleManager manager) {
    }

    /**
     * 添加女仆的 Task
     *
     * @param manager 注册器
     */
    default void addMaidTask(TaskManager manager) {
    }

    /**
     * 添加女仆的背包
     *
     * @param manager 注册器
     */
    default void addMaidBackpack(BackpackManager manager) {
    }

    /**
     * 添加多方块结构
     *
     * @param manager 注册器
     */
    default void addMultiBlock(MultiBlockManager manager) {
    }

    /**
     * 添加箱子类型，用于隙间饰品的箱子识别
     *
     * @param manager 注册器
     */
    default void addChestType(ChestManager manager) {
    }

    /**
     * 添加女仆饭类型
     *
     * @param manager 注册器
     */
    default void addMaidMeal(MaidMealManager manager) {
    }

    /**
     * 注册任务数据，任务数据是一种可以自定义添加到女仆上的数据
     *
     * @param register 注册器
     */
    default void registerTaskData(TaskDataRegister register) {
    }

    /**
     * 给女仆添加额外的 AI 数据，比如 MemoryModuleType 或者 SensorType
     *
     * @param manager 注册器
     */
    default void addExtraMaidBrain(ExtraMaidBrainManager manager) {
    }

    /**
     * 注册女仆的聊天气泡类型
     *
     * @param register 注册器
     */
    default void registerChatBubble(ChatBubbleRegister register) {
    }

    /**
     * 注册女仆的 AI 聊天功能的序列化器，相当于新增一个站点解析支持
     *
     * @param register 注册器
     */
    default void registerAIChatSerializer(SerializerRegister register) {
    }

    /**
     * 注册一个自己的 function call
     */
    default void registerAIFunctionCall(FunctionCallRegister register) {
    }

    /**
     * 注册一个扫帚的控制器
     */
    default void registerBroomControl(BroomControlManager register) {
    }

    /**
     * 给女仆模组的作物模式添加特判
     */
    default void registerSpecialCropHandler(SpecialCropManager register) {
    }

    /**
     * 添加女仆相关提示
     * <p>
     * 有些物品在指向女仆时，能够在屏幕上显示相关提示文本
     */
    @OnlyIn(Dist.CLIENT)
    default void addMaidTips(MaidTipsOverlay maidTipsOverlay) {
    }

    /**
     * 添加默认模型风格的实体 layer 渲染
     */
    @OnlyIn(Dist.CLIENT)
    default void addAdditionMaidLayer(EntityMaidRenderer renderer, EntityRendererProvider.Context context) {
    }

    /**
     * 添加 Gecko 风格的实体 layer 渲染
     */
    @OnlyIn(Dist.CLIENT)
    default void addAdditionGeckoMaidLayer(GeckoEntityMaidRenderer<? extends Mob> renderer, EntityRendererProvider.Context context) {
    }

    /**
     * 添加硬编码的动画
     */
    @OnlyIn(Dist.CLIENT)
    default void addHardcodeAnimation(HardcodedAnimationManger manger) {
    }

    @VisibleForDebug
    default Collection<? extends Function<EntityMaid, List<DebugTarget>>> getMaidDebugTargets() {
        return List.of();
    }
}
