package com.github.tartaricacid.touhoulittlemaid.ai.service.function.implement;

import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
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
            当你需要切换与击杀怪物相关的工作模式时，才会调用此函数。
            参数需要给定一个工作模式的 ID！
            如果我提供的信息缺少工具所需要的必填参数，你需要进一步追问让我提供更多信息。
            """;
    private static final String PARAMETER_ID = "task_id";
    private static final String PARAMETER_DESC = """
            你需要切换的工作模式 ID：
            - touhou_little_maid:idle 表示空闲模式，此工作模式下，你什么也不会做
            - touhou_little_maid:attack 表示近战模式，你会主动攻击周围的敌对生物
            - touhou_little_maid:ranged_attack 表示弓箭攻击模式，你会主动用弓箭攻击周围的敌对生物
            - touhou_little_maid:crossbow_attack 表示弩箭攻击模式，你会主动用弩箭攻击周围的敌对生物
            - touhou_little_maid:danmaku_attack 表示弹幕攻击模式，你会主动用弹幕攻击周围的敌对生物
            - touhou_little_maid:trident_attack 表示三叉戟攻击模式，你会主动用三叉戟攻击周围的敌对生物
            """;
    private static final String RESPONSE_TEXT = "你已经切换%s模式";

    @Override
    public String getId() {
        return FUNCTION_ID;
    }

    @Override
    public String getDescription() {
        return FUNCTION_DESC;
    }

    @Override
    public Parameter addParameters(ObjectParameter root) {
        Parameter taskId = StringParameter.create()
                .setDescription(PARAMETER_DESC)
                .addEnumValues(
                        "touhou_little_maid:idle",
                        "touhou_little_maid:attack",
                        "touhou_little_maid:ranged_attack",
                        "touhou_little_maid:crossbow_attack",
                        "touhou_little_maid:danmaku_attack",
                        "touhou_little_maid:trident_attack"
                );
        root.addProperties(PARAMETER_ID, taskId);
        return root;
    }

    @Override
    public Codec<Result> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf(PARAMETER_ID).forGetter(Result::taskId)
        ).apply(instance, Result::new));
    }

    @Override
    public void onToolCall(Result result, EntityMaid maid) {
        ResourceLocation taskId = result.taskId;
        Optional<IMaidTask> optional = TaskManager.findTask(taskId);
        if (optional.isEmpty()) {
            return;
        }

        // 空闲模式额外判断
        IMaidTask task = optional.get();
        RangedWrapper backpack = maid.getAvailableBackpackInv();
        if (task == TaskManager.getIdleTask()) {
            putItemBack(maid, backpack);
            // TODO: 现在的聊天气泡居然不支持 Component，没法给女仆显示翻译过的字符串做提示
            maid.setTask(task);
            return;
        }

        // 攻击模式判断
        if (!(task instanceof IAttackTask attackTask)) {
            return;
        }
        // 如果不需要拿武器并切换模式，那么不用执行后面的逻辑
        IMaidTask currentTask = maid.getTask();
        if (attackTask == currentTask && attackTask.isWeapon(maid, maid.getMainHandItem())) {
            return;
        }

        // TODO: 现在的聊天气泡居然不支持 Component，没法给女仆显示翻译过的字符串做提示
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

    public record Result(ResourceLocation taskId) {
    }
}
