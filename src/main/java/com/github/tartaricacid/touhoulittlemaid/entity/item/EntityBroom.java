package com.github.tartaricacid.touhoulittlemaid.entity.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.OpenPlayerInventoryMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EntityBroom extends AbstractEntityFromItem implements OwnableEntity, HasCustomInventoryScreen {
    public static final EntityType<EntityBroom> TYPE = EntityType.Builder.<EntityBroom>of(EntityBroom::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10).build("broom");

    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(EntityBroom.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final String OWNER_UUID_TAG = "OwnerUUID";

    private boolean keyJump = false;

    public EntityBroom(EntityType<EntityBroom> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.setNoGravity(true);
    }

    public EntityBroom(Level worldIn) {
        this(TYPE, worldIn);
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyJump() {
        return Minecraft.getInstance().options.keyJump.isDown();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_ID, Optional.empty());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(OWNER_UUID_TAG)) {
            setOwnerUUID(NbtUtils.loadUUID(Objects.requireNonNull(compound.get(OWNER_UUID_TAG))));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.entityData.get(OWNER_ID).ifPresent(uuid -> compound.putUUID(OWNER_UUID_TAG, uuid));
    }

    @Override
    public void travel(Vec3 vec3) {
        Entity entity = this.getControllingPassenger();
        if (entity instanceof Player player && this.isVehicle() && this.hasPassenger(e -> e instanceof EntityMaid)) {
            boolean keyForward = player.zza > 0;
            boolean keyBack = player.zza < 0;
            boolean keyLeft = player.xxa > 0;
            boolean keyRight = player.xxa < 0;
            boolean keySneak = player.isShiftKeyDown();

            if (level.isClientSide) {
                // 空格键起飞
                this.keyJump = !keySneak && keyJump();
            }

            Vec3 currentMotion = this.getDeltaMovement();
            boolean hasInput = keyForward || keyBack || keyLeft || keyRight || this.keyJump || keySneak;

            if (hasInput) {
                float strafe = keyLeft ? 0.2f : (keyRight ? -0.2f : 0);
                float vertical = 0;

                // 垂直移动控制
                if (keyJump) {
                    vertical = 0.25f;
                } else if (keySneak) {
                    vertical = -0.2f;
                } else if (keyForward) {
                    // 原有的俯仰控制
                    vertical = -(player.getXRot() - 10) / 360f;
                }

                float forward = keyForward ? 0.375f : (keyBack ? -0.2f : 0);

                // 玩家基础速度是 0.1，速度二效果是 0.14，为了增加速度效果带来的增益，故这样计算
                float speed = Math.max(getRiddenSpeed(player) - 0.1f, 0) * 2.5f + 0.1f;
                Vec3 targetMotion = new Vec3(strafe, vertical, forward).scale(speed * 20);
                targetMotion = targetMotion.yRot((float) (-this.getYRot() * Math.PI / 180.0));

                // 插值到目标速度，而不是直接累加
                Vec3 newMotion = currentMotion.lerp(targetMotion, 0.25f);

                if (hasCollisionAbove()) {
                    newMotion = new Vec3(-newMotion.x, -0.3, -newMotion.z);
                }

                this.setDeltaMovement(newMotion);
            } else {
                // 没有输入时，快速减速
                this.setDeltaMovement(currentMotion.scale(0.75));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }
        if (!this.onGround()) {
            // 玩家没有坐在扫帚上，那就让它掉下来
            super.travel(new Vec3(0, -0.3f, 0));
            return;
        }
        super.travel(vec3);
    }

    private boolean hasCollisionAbove() {
        if (this.onGround()) {
            return false;
        }
        // 检查扫帚及乘客上方 1.5 格的空间
        double hOffset = 0.25;
        double vOffset = 1.75;
        AABB aabb = this.getBoundingBox();
        AABB checkBox = new AABB(aabb.minX - hOffset, aabb.minY, aabb.minZ - hOffset,
                aabb.maxX + hOffset, aabb.maxY + vOffset, aabb.maxZ + hOffset);
        return level.collidesWithSuffocatingBlock(this, checkBox);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) player.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    protected void pushEntities() {
        // 已经坐满两人，不执行
        if (this.getPassengers().size() >= 2) {
            return;
        }
        // 已经坐了一人，但不是玩家，不执行
        if (!this.getPassengers().isEmpty() && !(this.getControllingPassenger() instanceof Player)) {
            return;
        }
        if (!level.isClientSide) {
            List<EntityMaid> list = level.getEntitiesOfClass(EntityMaid.class, getBoundingBox().expandTowards(0.5, 0.1, 0.5), this::canMaidRide);
            list.stream().findFirst().ifPresent(entity -> entity.startRiding(this));
        }
    }

    private boolean canMaidRide(EntityMaid maid) {
        if (maid.canBrainMoving() && !maid.isVehicle() && EntitySelector.pushableBy(this).test(maid)) {
            UUID maidOwnerUUID = maid.getOwnerUUID();
            UUID broomOwnerUUID = this.getOwnerUUID();
            if (maidOwnerUUID == null || broomOwnerUUID == null) {
                return false;
            }
            return maidOwnerUUID.equals(broomOwnerUUID);
        }
        return false;
    }

    @Override
    protected void tickRidden(Player player, Vec3 pTravelVector) {
        // 记得将 fall distance 设置为 0，否则会摔死
        this.fallDistance = 0;

        // 施加上下晃动
        if (!this.onGround()) {
            this.addDeltaMovement(new Vec3(0, 0.003 * Math.sin(this.tickCount * Math.PI / 18), 0));
        }

        // 与旋转有关系的一堆东西，用来控制扫帚朝向
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.setRot(player.getYRot(), player.getXRot());
        super.tickRidden(player, pTravelVector);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            double xOffset = passenger instanceof EntityMaid ? -0.5 : 0;
            double yOffset = this.isRemoved() ? 0.01 : this.getPassengersRidingOffset() + passenger.getMyRidingOffset();
            if (this.getPassengers().size() > 1) {
                int passengerIndex = this.getPassengers().indexOf(passenger);
                if (passengerIndex == 0) {
                    xOffset = 0.35;
                } else {
                    xOffset = -0.35;
                }
            }
            Vec3 offset = new Vec3(xOffset, yOffset, 0).yRot((float) (-this.getYRot() * Math.PI / 180 - Math.PI / 2));
            moveFunction.accept(passenger, this.getX() + offset.x, this.getY() + offset.y, this.getZ() + offset.z);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.isDiscrete() && !this.isPassenger() && !(this.getControllingPassenger() instanceof Player)) {
            if (this.getPassengers().size() > 1) {
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            if (!level.isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.interact(player, hand);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        List<Entity> passengers = this.getPassengers();
        boolean hasPlayer = false;
        EntityMaid maidOpen = null;
        for (int i = 0; i < Math.min(passengers.size(), 2); i++) {
            Entity entity = passengers.get(i);
            if (entity.equals(player)) {
                hasPlayer = true;
            }
            if (entity instanceof EntityMaid maid && maid.isOwnedBy(player)) {
                maidOpen = maid;
            }
        }
        if (hasPlayer) {
            if (maidOpen == null) {
                NetworkHandler.sendToClientPlayer(new OpenPlayerInventoryMessage(OpenPlayerInventoryMessage.OPEN_PLAYER_INVENTORY), serverPlayer);
            } else {
                maidOpen.openMaidGui(serverPlayer);
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isAlive();
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() < 2;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0;
    }

    @Override
    protected boolean canKillEntity(Player player) {
        return true;
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.WOOL_BREAK;
    }

    @Override
    protected Item getWithItem() {
        return InitItems.BROOM.get();
    }

    @Override
    protected ItemStack getKilledStack() {
        return new ItemStack(this.getWithItem());
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        this.resetFallDistance();
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_ID, Optional.ofNullable(uuid));
    }
}
