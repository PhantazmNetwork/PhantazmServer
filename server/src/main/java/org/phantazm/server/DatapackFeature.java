package org.phantazm.server;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.datapack.Datapack;
import org.phantazm.core.datapack.DatapackLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DatapackFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatapackFeature.class);

    private DatapackFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull BiomeManager biomeManager) throws IOException {
        DatapackLoader loader = new DatapackLoader();

        LOGGER.info("Loading datapacks");
        List<Biome> biomes = new ArrayList<>();
        Path datapacksPath = Path.of("./datapacks");
        PathMatcher matcher = datapacksPath.getFileSystem().getPathMatcher("glob:**/*.zip");
        DirectoryStream.Filter<Path> filter = entry -> matcher.matches(entry) && Files.isRegularFile(entry);
        int loadedDatapacks = 0;
        try (DirectoryStream<Path> datapacks = Files.newDirectoryStream(datapacksPath, filter)) {
            for (Path zipPath : datapacks) {
                Datapack datapack;
                try {
                    datapack = loadDatapackFromPath(loader, zipPath);
                    ++loadedDatapacks;
                }
                catch (IOException e) {
                    LOGGER.warn("Failed to load datapack at {}", zipPath, e);
                    continue;
                }

                biomes.addAll(datapack.biomes().values());
            }
        }

        LOGGER.info("Loaded {} datapacks", loadedDatapacks);

        NamespaceID plainsID = NamespaceID.from("minecraft:plains");
        biomeManager.removeBiome(Biome.PLAINS);
        int id = -1;
        boolean hasPlains = false;

        Collection<IntObjectPair<Biome>> biomeEntries = new ArrayList<>(biomes.size());
        for (Biome biome : biomes) {
            if (biome.name().equals(plainsID)) {
                hasPlains = true;
            }

            biomeEntries.add(IntObjectPair.of(++id, biome));
        }
        if (!hasPlains) {
            biomeEntries.add(IntObjectPair.of(++id, Biome.PLAINS));
        }

        biomeManager.addBiomes(biomeEntries);
    }

    private static Datapack loadDatapackFromPath(DatapackLoader loader, Path zipPath) throws IOException {
        try (FileSystem datapackFileSystem = FileSystems.newFileSystem(zipPath)) {
            Path datapackPath = datapackFileSystem.getPath("/");
            return loader.loadDatapack(datapackPath);
        }
    }

}
