package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.zombies.equipment.gun.data.GunLevelData;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class GunLevelDataConfigProcessor implements ConfigProcessor<GunLevelData> {

    private final static ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

    private final static ConfigProcessor<Collection<Key>> KEY_COLLECTION_PROCESSOR
            = KEY_PROCESSOR.collectionProcessor(ArrayList::new);

    private final ConfigProcessor<ItemStack> stackProcessor;

    public GunLevelDataConfigProcessor(@NotNull ConfigProcessor<ItemStack> stackProcessor) {
        this.stackProcessor = Objects.requireNonNull(stackProcessor, "stackProcessor");
    }

    @Override
    public @NotNull GunLevelData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        int order = element.getNumberOrThrow("order").intValue();
        ItemStack stack = stackProcessor.dataFromElement(element.getElementOrThrow("stack"));
        Key stats = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("stats"));
        Key shootTester = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("shootTester"));
        Key reloadTester = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("reloadTester"));
        Key firer = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("firer"));
        Collection<Key> shootEffects = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("shootEffects"));
        Collection<Key> reloadEffects = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("reloadEffects"));
        Collection<Key> tickEffects = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("tickEffects"));
        Collection<Key> emptyClipEffects = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("emptyClipEffects"));
        Collection<Key> gunStackMappers = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("gunStackMappers"));

        return new GunLevelData(order, stack, stats, shootTester, reloadTester, firer, shootEffects, reloadEffects,
                tickEffects, emptyClipEffects, gunStackMappers);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull GunLevelData gunLevelData) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode(11);
        node.putNumber("order", gunLevelData.order());
        node.put("stack", stackProcessor.elementFromData(gunLevelData.stack()));
        node.put("stats", KEY_PROCESSOR.elementFromData(gunLevelData.stats()));
        node.put("shootTester", KEY_PROCESSOR.elementFromData(gunLevelData.shootTester()));
        node.put("reloadTester", KEY_PROCESSOR.elementFromData(gunLevelData.reloadTester()));
        node.put("firer", KEY_PROCESSOR.elementFromData(gunLevelData.firer()));
        node.put("shootEffects", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.shootEffects()));
        node.put("reloadEffects", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.reloadEffects()));
        node.put("tickEffects", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.tickEffects()));
        node.put("emptyClipEffects", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.emptyClipEffects()));
        node.put("gunStackMappers", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.gunStackMappers()));

        return node;
    }
}
