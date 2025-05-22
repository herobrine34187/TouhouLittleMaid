package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class EntityGraphics extends GuiGraphics {
    private final EntityMaid maid;
    private final int packedLight;
    private final float partialTicks;

    public EntityGraphics(Minecraft minecraft, PoseStack pose, MultiBufferSource.BufferSource bufferSource, EntityMaid maid, int packedLight, float partialTicks) {
        super(minecraft, pose, bufferSource);
        this.maid = maid;
        this.packedLight = packedLight;
        this.partialTicks = partialTicks;
    }

    public EntityMaid getMaid() {
        return maid;
    }

    public int getPackedLight() {
        return packedLight;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    /**
     * 实体渲染不需要提交顶点
     */
    @Override
    public void flush() {
    }

    /**
     * 渲染物品目前有问题，暂时禁用
     */
    @Override
    public void renderItem(@Nullable LivingEntity pEntity, @Nullable Level pLevel, ItemStack pStack, int pX, int pY, int pSeed, int pGuiOffset) {
    }
}
