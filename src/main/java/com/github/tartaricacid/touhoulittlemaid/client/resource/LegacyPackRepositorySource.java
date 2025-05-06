package com.github.tartaricacid.touhoulittlemaid.client.resource;


import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.util.GetJarResources;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class LegacyPackRepositorySource implements RepositorySource {
    private static final String CUSTOM_PACK_DIR_NAME = "tlm_custom_pack";
    private static final String PACK_NAME = "touhou_little_maid_legacy_resources_pack.zip";
    private static final Path LEGACY_PACK_PATH = FMLPaths.CONFIGDIR.get().resolve(TouhouLittleMaid.MOD_ID).resolve("legacy_pack");
    private final Pack legacyPack;

    public LegacyPackRepositorySource() {
        Pack.ResourcesSupplier supplier = name -> new FilePackResources(name, this.getLegacyPack(), false);
        MutableComponent desc = Component.translatable("pack.touhou_little_maid.legacy_resources_pack.desc");
        int packFormatVersion = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
        Pack.Info info = new Pack.Info(desc, packFormatVersion, FeatureFlagSet.of());
        MutableComponent title = Component.translatable("pack.touhou_little_maid.legacy_resources_pack.title");
        this.legacyPack = Pack.create(PACK_NAME, title, false, supplier, info, PackType.CLIENT_RESOURCES,
                Pack.Position.TOP, false, PackSource.BUILT_IN);
    }

    private File getLegacyPack() {
        if (!LEGACY_PACK_PATH.toFile().isDirectory()) {
            try {
                Files.createDirectories(LEGACY_PACK_PATH);
            } catch (IOException e) {
                TouhouLittleMaid.LOGGER.error("Failed to create directory for legacy pack", e);
            }
        }
        // 不管存不存在，强行覆盖
        String jarLegacyPackPath = String.format("/assets/%s/%s/%s", TouhouLittleMaid.MOD_ID, CUSTOM_PACK_DIR_NAME, PACK_NAME);
        GetJarResources.copyTouhouLittleMaidFile(jarLegacyPackPath, LEGACY_PACK_PATH, PACK_NAME);
        return LEGACY_PACK_PATH.resolve(PACK_NAME).toFile();
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        consumer.accept(this.legacyPack);
    }
}
