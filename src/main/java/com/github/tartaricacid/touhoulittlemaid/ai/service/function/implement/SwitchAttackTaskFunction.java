package com.github.tartaricacid.touhoulittlemaid.ai.service.function.implement;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.EndToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.KeepToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.ToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.compat.tacz.TacCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RangedWrapper;

import java.util.Optional;

public class SwitchAttackTaskFunction implements IFunctionCall<SwitchAttackTaskFunction.Result> {
    private static final String FUNCTION_ID = "switch_maid_attack_task";
    private static final String FUNCTION_DESC = """
            当你需要切换与击杀怪物相关的工作模式时，可以调用此函数。
            参数需要给定一个工作模式的 ID，和一个此刻你打算说的话。
            根据对话记录自动选择工作模式的 ID
            如果我提供的信息缺少工具所需要的必填参数，你需要进一步追问让我提供更多信息。
            """;
    private static final String TASK_ID_PARAMETER_ID = "task_id";
    private static final String TASK_ID_PARAMETER_DESC = """
            你需要切换的工作模式 ID：
            - touhou_little_maid:idle 表示空闲模式，此工作模式下，你什么也不会做
            - touhou_little_maid:attack 表示近战模式，你会主动攻击周围的敌对生物
            - touhou_little_maid:ranged_attack 表示弓箭攻击模式，你会主动用弓箭攻击周围的敌对生物
            - touhou_little_maid:crossbow_attack 表示弩箭攻击模式，你会主动用弩箭攻击周围的敌对生物
            - touhou_little_maid:danmaku_attack 表示弹幕攻击模式，你会主动用弹幕攻击周围的敌对生物
            - touhou_little_maid:trident_attack 表示三叉戟攻击模式，你会主动用三叉戟攻击周围的敌对生物
            """;
    private static final String GUN_TASK_ID_PARAMETER_DESC = "- touhou_little_maid:gun_attack 表示枪械攻击，你会主动用 TACZ 的枪械攻击周围的敌对生物";
    private static final String CHAT_PARAMETER_ID = "chat_text";
    private static final String CHAT_PARAMETER_DESC = """
            你此刻打算说的话，需要符合当前的工作模式切换
            """;
    private static final String TTS_PARAMETER_ID = "tts_text";
    private static final String TTS_PARAMETER_DESC = """
            将 chat_text 部分翻译成 %s 语言的文本
            """;

    @Override
    public String getId() {
        return FUNCTION_ID;
    }

    @Override
    public String getDescription(EntityMaid maid) {
        return FUNCTION_DESC;
    }

    @Override
    public Parameter addParameters(ObjectParameter root, EntityMaid maid) {
        Parameter taskId = StringParameter.create()
                .setDescription(TASK_ID_PARAMETER_DESC)
                .addEnumValues(
                        "touhou_little_maid:idle",
                        "touhou_little_maid:attack",
                        "touhou_little_maid:ranged_attack",
                        "touhou_little_maid:crossbow_attack",
                        "touhou_little_maid:danmaku_attack",
                        "touhou_little_maid:trident_attack"
                );
        // 兼容 TACZ
        if (TacCompat.isInstalled()) {
            taskId.setDescription(TASK_ID_PARAMETER_DESC + "\n" + GUN_TASK_ID_PARAMETER_DESC);
            taskId.addEnumValues("touhou_little_maid:gun_attack");
        }
        Parameter chat = StringParameter.create().setDescription(CHAT_PARAMETER_DESC);
        String ttsLanguage = maid.getAiChatManager().getTTSLanguage();
        Parameter tts = StringParameter.create().setDescription(TTS_PARAMETER_DESC.formatted(ttsLanguage));

        root.addProperties(TASK_ID_PARAMETER_ID, taskId)
                .addProperties(CHAT_PARAMETER_ID, chat)
                .addProperties(TTS_PARAMETER_ID, tts);
        return root;
    }

    @Override
    public Codec<Result> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf(TASK_ID_PARAMETER_ID).forGetter(Result::taskId),
                Codec.STRING.fieldOf(CHAT_PARAMETER_ID).forGetter(Result::chat),
                Codec.STRING.fieldOf(TTS_PARAMETER_ID).forGetter(Result::tts)
        ).apply(instance, Result::new));
    }

    @Override
    public ToolResponse onToolCall(Result result, EntityMaid maid) {
        ResourceLocation taskId = result.taskId;
        KeepToolResponse response = new KeepToolResponse("干得不错，摸摸你的头");
        Optional<IMaidTask> optional = TaskManager.findTask(taskId);
        if (optional.isEmpty()) {
            return response;
        }

        // 空闲模式额外判断
        IMaidTask task = optional.get();
        RangedWrapper backpack = maid.getAvailableBackpackInv();
        if (task == TaskManager.getIdleTask()) {
            putItemBack(maid, backpack);
            maid.setTask(task);
            return response;
        }

        // 攻击模式判断
        if (!(task instanceof IAttackTask attackTask)) {
            return response;
        }
        // 如果不需要拿武器并切换模式，那么不用执行后面的逻辑
        IMaidTask currentTask = maid.getTask();
        if (attackTask == currentTask && attackTask.isWeapon(maid, maid.getMainHandItem())) {
            return response;
        }

        maid.setTask(task);

        // 将武器取到主手上
        int slot = ItemsUtil.findStackSlot(backpack, item -> attackTask.isWeapon(maid, item));
        if (slot >= 0) {
            int count = backpack.getStackInSlot(slot).getCount();
            ItemStack output = backpack.extractItem(slot, count, false);
            if (!maid.getMainHandItem().isEmpty()) {
                ItemStack mainhand = maid.getMainHandItem();
                backpack.setStackInSlot(slot, mainhand);
            }
            maid.setItemInHand(InteractionHand.MAIN_HAND, output);
        }
        return response;
    }

    private void putItemBack(EntityMaid maid, RangedWrapper backpack) {
        if (maid.getMainHandItem().isEmpty()) {
            return;
        }
        ItemStack mainHandItem = maid.getMainHandItem();
        for (int i = 0; i < backpack.getSlots(); i++) {
            ItemStack stackInSlot = backpack.getStackInSlot(i);
            if (stackInSlot.isEmpty()) {
                backpack.setStackInSlot(i, mainHandItem);
                maid.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                return;
            }
        }
    }

    public record Result(ResourceLocation taskId, String chat, String tts) {
    }
}
