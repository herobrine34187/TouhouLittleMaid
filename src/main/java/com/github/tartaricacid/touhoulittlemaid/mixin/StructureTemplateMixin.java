package com.github.tartaricacid.touhoulittlemaid.mixin;

import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Random;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {
    @Unique
    private static final Random TOUHOU_LITTLE_MAID$RANDOM = new Random();

    @Inject(method = "createEntityIgnoreException(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private static void createEntityIgnoreException(ServerLevelAccessor accessor, CompoundTag tag, CallbackInfoReturnable<Optional<Entity>> ci) {
        ci.getReturnValue().ifPresent(entity -> {
            ServerLevel level = accessor.getLevel();
            if (entity.getType().equals(EntityType.ALLAY) && TOUHOU_LITTLE_MAID$RANDOM.nextDouble() < MaidConfig.REPLACE_ALLAY_PERCENT.get()) {
                EntityMaid entityMaid = InitEntities.MAID.get().create(level);
                ci.setReturnValue(Optional.ofNullable(entityMaid));
            }
        });
    }
}
