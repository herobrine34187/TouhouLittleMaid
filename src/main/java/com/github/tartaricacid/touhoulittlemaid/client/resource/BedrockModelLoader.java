package com.github.tartaricacid.touhoulittlemaid.client.resource;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.EntityFairyModel;
import com.github.tartaricacid.touhoulittlemaid.client.model.NewEntityFairyModel;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.SimpleBedrockModel;
import com.google.common.collect.Maps;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

/**
 * 把所有硬编码的模型全部资源包化，方便资源包替换模型
 */
@OnlyIn(Dist.CLIENT)
public class BedrockModelLoader {
    // 内部数据
    private static final Map<ResourceLocation, Function<InputStream, ? extends SimpleBedrockModel<? extends Entity>>> ALL_MODELS = Maps.newHashMap();
    private static final Map<ResourceLocation, SimpleBedrockModel<? extends Entity>> BEDROCK_MODELS = Maps.newHashMap();

    // 注册数据
    public static final ResourceLocation ALTAR = registerSimpleBlockModel("altar");
    public static final ResourceLocation MAID_FAIRY = registerEntityModel("maid_fairy", EntityFairyModel::new);
    public static final ResourceLocation NEW_MAID_FAIRY = registerEntityModel("new_maid_fairy", NewEntityFairyModel::new);

    public static ResourceLocation registerSimpleBlockModel(String name) {
        ResourceLocation location = new ResourceLocation(TouhouLittleMaid.MOD_ID, "models/bedrock/block/" + name + ".json");
        return registerSimpleModel(location);
    }

    public static ResourceLocation registerSimpleEntityModel(String name) {
        ResourceLocation location = new ResourceLocation(TouhouLittleMaid.MOD_ID, "models/bedrock/entity/" + name + ".json");
        return registerSimpleModel(location);
    }

    public static ResourceLocation registerSimpleModel(ResourceLocation location) {
        return registerModel(location, SimpleBedrockModel::new);
    }

    public static ResourceLocation registerBlockModel(String name, Function<InputStream, ? extends SimpleBedrockModel<? extends Entity>> function) {
        ResourceLocation location = new ResourceLocation(TouhouLittleMaid.MOD_ID, "models/bedrock/block/" + name + ".json");
        return registerModel(location, function);
    }

    public static ResourceLocation registerEntityModel(String name, Function<InputStream, ? extends SimpleBedrockModel<? extends Entity>> function) {
        ResourceLocation location = new ResourceLocation(TouhouLittleMaid.MOD_ID, "models/bedrock/entity/" + name + ".json");
        return registerModel(location, function);
    }

    public static ResourceLocation registerModel(ResourceLocation location, Function<InputStream, ? extends SimpleBedrockModel<? extends Entity>> function) {
        ALL_MODELS.put(location, function);
        return location;
    }

    public static void reload() {
        BEDROCK_MODELS.clear();
        ALL_MODELS.forEach(BedrockModelLoader::loadModel);
    }

    @Nullable
    public static SimpleBedrockModel<? extends Entity> getModel(ResourceLocation location) {
        return BEDROCK_MODELS.get(location);
    }

    private static void loadModel(ResourceLocation location, Function<InputStream, ? extends SimpleBedrockModel<? extends Entity>> function) {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(location)) {
            BEDROCK_MODELS.put(location, function.apply(stream));
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            TouhouLittleMaid.LOGGER.error("Failed to load bedrock model", e);
        }
    }
}
