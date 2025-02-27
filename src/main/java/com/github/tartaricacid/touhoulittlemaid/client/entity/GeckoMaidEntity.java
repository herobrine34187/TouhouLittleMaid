package com.github.tartaricacid.touhoulittlemaid.client.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.HardcodedAnimationManger;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.AnimationManager;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.AnimatableEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.controller.AnimationController;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.MolangParser;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.context.AnimationContext;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.processor.IBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.ILocationModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.model.provider.data.EntityModelData;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.resource.GeckoLibCache;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.List;

public class GeckoMaidEntity<T extends Mob> extends AnimatableEntity<T> implements IGeoEntity {
    private static final ResourceLocation GECKO_DEFAULT_ID = new ResourceLocation(TouhouLittleMaid.MOD_ID, "fox_miko");
    private static final ResourceLocation GECKO_DEFAULT_TEXTURE = new ResourceLocation(TouhouLittleMaid.MOD_ID, "textures/entity/empty.png");
    private static final int FPS = 60;

    private final IMaid maid;
    private final Vector2f headRot = new Vector2f();
    private final MaidState<T> state;
    private MaidModelInfo maidInfo;
    private float currentTick = -1;
    private boolean modelDirty = false;

    public GeckoMaidEntity(T mob, IMaid maid) {
        super(mob, FPS);
        this.maid = maid;
        this.state = new MaidState<>(mob);
        registerControllers();
    }

    public void registerControllers() {
        AnimationManager manager = AnimationManager.getInstance();
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("pre_parallel_%d_controller", i);
            String animationName = String.format("pre_parallel%d", i);
            addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateParallel(e, animationName)));
        }
        addAnimationController(new AnimationController<>(this, "main", 2, manager::predicateMain));
        addAnimationController(new AnimationController<>(this, "hold_offhand", 0, manager::predicateOffhandHold));
        addAnimationController(new AnimationController<>(this, "hold_mainhand", 0, manager::predicateMainhandHold));
        addAnimationController(new AnimationController<>(this, "swing", 2, manager::predicateSwing));
        addAnimationController(new AnimationController<>(this, "use", 2, manager::predicateUse));
        addAnimationController(new AnimationController<>(this, "misc", 2, manager::predicateMisc));
        addAnimationController(new AnimationController<>(this, "passenger", 2, manager::predicatePassengerAnimation));
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("parallel_%d_controller", i);
            String animationName = String.format("parallel%d", i);
            addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateParallel(e, animationName)));
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                String controllerName = String.format("%s_controller", slot.getName());
                addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateArmor(e, slot)));
            }
        }
    }

    @Override
    @SuppressWarnings("all")
    public boolean setCustomAnimations(AnimationContext context, @NotNull AnimationEvent event) {
        List extraData = event.getExtraData();
        MolangParser parser = GeckoLibCache.getInstance().parser;
        if (!Minecraft.getInstance().isPaused() && extraData.size() == 1 && extraData.get(0) instanceof EntityModelData data) {
            var update = super.setCustomAnimations(context, event);
            AnimatedGeoModel currentModel = this.getCurrentModel();
            if (currentModel != null) {
                this.updateHead(data, currentModel, update);
                HardcodedAnimationManger.playGeckoMaidAnimation(maid, currentModel, event.getLimbSwing(), event.getLimbSwingAmount(),
                        maid.asEntity().tickCount + event.getPartialTick(), data.netHeadYaw, data.headPitch);
            }
            return update;
        } else {
            return super.setCustomAnimations(context, event);
        }
    }

    @SuppressWarnings("all")
    private void updateHead(EntityModelData data, AnimatedGeoModel currentModel, boolean update) {
        if (currentModel.head() != null) {
            IBone head = currentModel.head();
            if (update) {
                this.headRot.set(head.getRotationX(), head.getRotationY());
            }
            head.setRotationX(this.headRot.x + (float) Math.toRadians(data.headPitch));
            head.setRotationY(this.headRot.y + (float) Math.toRadians(data.netHeadYaw));
        }
    }

    @Override
    public ResourceLocation getModelLocation() {
        if (this.maidInfo != null && GeckoLibCache.getInstance().getGeoModels().containsKey(this.maidInfo.getModelId())) {
            return this.maidInfo.getModelId();
        }
        return GECKO_DEFAULT_ID;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return this.maidInfo != null ? maidInfo.getTexture() : GECKO_DEFAULT_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation() {
        if (this.maidInfo != null && GeckoLibCache.getInstance().getAnimations().containsKey(this.maidInfo.getModelId())) {
            return this.maidInfo.getModelId();
        }
        return GECKO_DEFAULT_ID;
    }

    @Override
    protected boolean forceUpdate(AnimationEvent<?> animationEvent) {
        var tick = (float) getCurrentTick(animationEvent);
        if (tick > this.currentTick) {
            this.currentTick = tick;
            this.state.updateState();
            this.modelDirty = false;
            return false;
        }
        if (this.modelDirty || !this.state.compareState()) {
            this.state.updateState();
            this.modelDirty = false;
            return true;
        }
        return false;
    }

    @Override
    public IMaid getMaid() {
        return maid;
    }

    @Override
    public MaidModelInfo getMaidInfo() {
        return maidInfo;
    }

    @Override
    public ILocationModel getGeoModel() {
        return this.getCurrentModel();
    }

    @Override
    public void setMaidInfo(MaidModelInfo info) {
        if (this.maidInfo != info) {
            this.maidInfo = info;
            this.modelDirty = true;
        }
    }

    @Override
    public void setYsmModel(String modelId, String texture) {
    }

    @Override
    public void updateRoamingVars(Object2FloatOpenHashMap<String> roamingVars) {
    }

    private static class MaidState<T extends Mob> {
        private final T maid;

        private float yHeadRot = 0;
        private float yBodyRot = 0;

        private MaidState(T maid) {
            this.maid = maid;
        }

        public boolean compareState() {
            if (this.yHeadRot != this.maid.yHeadRot || this.yBodyRot != this.maid.yBodyRot) {
                return false;
            }
            return true;
        }

        public void updateState() {
            this.yHeadRot = this.maid.yHeadRot;
            this.yBodyRot = this.maid.yBodyRot;
        }
    }
}
