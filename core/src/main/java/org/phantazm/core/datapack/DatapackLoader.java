package org.phantazm.core.datapack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.BiomeParticle;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.json.NBTGsonReader;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class DatapackLoader {

    public @NotNull Datapack loadDatapack(@NotNull Path datapackPath) throws IOException {
        Path data = datapackPath.resolve("data");
        Map<NamespaceID, Biome> biomes = new TreeMap<>();
        try (DirectoryStream<Path> namespaceRoots = Files.newDirectoryStream(data, Files::isDirectory)) {
            for (Path namespaceRoot : namespaceRoots) {
                String namespace = namespaceRoot.getFileName().toString();
                biomes.putAll(loadBiomes(namespace, namespaceRoot));
            }

            return new Datapack(biomes);
        }
    }

    private Map<NamespaceID, Biome> loadBiomes(String namespace, Path root) throws IOException {
        Map<NamespaceID, Biome> biomes = new TreeMap<>();
        Path biomeRoot = root.resolve("worldgen/biome");
        PathMatcher matcher = root.getFileSystem().getPathMatcher("glob:**/*.json");
        DirectoryStream.Filter<Path> filter = entry -> matcher.matches(entry) && Files.isRegularFile(entry);
        try (DirectoryStream<Path> biomePaths = Files.newDirectoryStream(biomeRoot, filter)) {
            for (Path biomePath : biomePaths) {
                String fileName = biomePath.getFileName().toString();
                NamespaceID name = NamespaceID.from(namespace, fileName.substring(0, fileName.length() - ".json".length()));
                Biome biome = loadBiome(name, biomePath);
                biomes.put(name, biome);
            }
        }

        return biomes;
    }

    private Biome loadBiome(NamespaceID name, Path path) throws IOException {
        JsonObject rootJson = JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
        Biome.Builder biomeBuilder = Biome.builder()
                .name(name)
                .temperature(rootJson.get("temperature").getAsFloat())
                .downfall(rootJson.get("downfall").getAsFloat())
                .precipitation(Biome.Precipitation.valueOf(rootJson.get("precipitation").getAsString().toUpperCase()));
        if (rootJson.has("temperature_modifier")) {
            biomeBuilder.temperatureModifier(Biome.TemperatureModifier.valueOf(rootJson.get("temperature_modifier").getAsString().toUpperCase()));
        }

        JsonObject effectsJson = rootJson.getAsJsonObject("effects");
        BiomeEffects.Builder effectsBuilder = BiomeEffects.builder()
                .fogColor(effectsJson.get("fog_color").getAsInt())
                .skyColor(effectsJson.get("sky_color").getAsInt())
                .waterColor(effectsJson.get("water_color").getAsInt())
                .waterFogColor(effectsJson.get("water_fog_color").getAsInt());
        if (effectsJson.has("foliage_color")) {
            effectsBuilder.foliageColor(effectsJson.get("foliage_color").getAsInt());
        }
        if (effectsJson.has("grass_color")) {
            effectsBuilder.grassColor(effectsJson.get("grass_color").getAsInt());
        }
        if (effectsJson.has("grass_color_modifier")) {
            effectsBuilder.grassColorModifier(BiomeEffects.GrassColorModifier.valueOf(effectsJson.get(
                    "grass_color_modifier").getAsString().toUpperCase()));
        }
        if (effectsJson.has("particle")) {
            JsonObject particleJson = effectsJson.getAsJsonObject("particle");
            float probability = particleJson.get("probability").getAsFloat();
            String particleSNBT = particleJson.get("options").toString();

            try (NBTGsonReader nbtReader = new NBTGsonReader(new StringReader(particleSNBT))) {
                NBTCompound particleNBT = (NBTCompound) nbtReader.read(NBTType.TAG_Compound);
                BiomeParticle.Option option = () -> particleNBT;
                BiomeParticle particle = new BiomeParticle(probability, option);
                effectsBuilder.biomeParticle(particle);
            }
        }
        if (effectsJson.has("ambient_sound")) {
            effectsBuilder.ambientSound(NamespaceID.from(effectsJson.get("ambient_sound").getAsString()));
        }
        if (effectsJson.has("mood_sound")) {
            JsonObject moodSoundJson = effectsJson.getAsJsonObject("mood_sound");
            NamespaceID sound = NamespaceID.from(moodSoundJson.get("sound").getAsString());
            int tickDelay = moodSoundJson.get("tick_delay").getAsInt();
            int blockSearchExtent = moodSoundJson.get("block_search_extent").getAsInt();
            double offset = moodSoundJson.get("offset").getAsDouble();
            BiomeEffects.MoodSound moodSound = new BiomeEffects.MoodSound(sound, tickDelay, blockSearchExtent, offset);
            effectsBuilder.moodSound(moodSound);
        }
        if (effectsJson.has("additions_sound")) {
            JsonObject additionsSoundJson = effectsJson.getAsJsonObject("additions_sound");
            NamespaceID sound = NamespaceID.from(additionsSoundJson.get("sound").getAsString());
            double tickChance = additionsSoundJson.get("tick_chance").getAsDouble();
            BiomeEffects.AdditionsSound additionsSound = new BiomeEffects.AdditionsSound(sound, tickChance);
            effectsBuilder.additionsSound(additionsSound);
        }
        if (effectsJson.has("music")) {
            JsonObject musicJson = effectsJson.getAsJsonObject("music");
            NamespaceID sound = NamespaceID.from(musicJson.get("sound").getAsString());
            int minDelay = musicJson.get("min_delay").getAsInt();
            int maxDelay = musicJson.get("max_delay").getAsInt();
            boolean replaceCurrentMusic = musicJson.get("replace_current_music").getAsBoolean();
            BiomeEffects.Music music = new BiomeEffects.Music(sound, minDelay, maxDelay, replaceCurrentMusic);
            effectsBuilder.music(music);
        }

        biomeBuilder.effects(effectsBuilder.build());
        return biomeBuilder.build();
    }

}
