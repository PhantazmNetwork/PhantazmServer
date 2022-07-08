package com.github.phantazmnetwork.zombies.equipment.gun.data;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link ConfigProcessor} for {@link GunLevelData}.
 */
public class GunLevelDataConfigProcessor implements ConfigProcessor<GunLevelData> {

    private static final ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

    private static final ConfigProcessor<Set<Key>> KEY_SET_PROCESSOR = KEY_PROCESSOR.setProcessor();

    private static final ConfigProcessor<Collection<Key>> KEY_COLLECTION_PROCESSOR
            = KEY_PROCESSOR.collectionProcessor();

    private final ConfigProcessor<ItemStack> stackProcessor;

    /**
     * Creates a {@link GunLevelDataConfigProcessor}.
     * @param stackProcessor A {@link ConfigProcessor} for {@link ItemStack}s
     */
    public GunLevelDataConfigProcessor(@NotNull ConfigProcessor<ItemStack> stackProcessor) {
        this.stackProcessor = Objects.requireNonNull(stackProcessor, "stackProcessor");
    }

    @Override
    public @NotNull GunLevelData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        Set<Key> upgrades = KEY_SET_PROCESSOR.dataFromElement(element.getElementOrThrow("upgrades"));
        ItemStack stack = stackProcessor.dataFromElement(element.getElementOrThrow("stack"));
        Key stats = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("stats"));
        Key shootTester = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("shootTester"));
        Key reloadTester = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("reloadTester"));
        Key firer = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("firer"));
        Collection<Key> activateEffects = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("activateEffects"));
        Collection<Key> shootEffects = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("shootEffects"));
        Collection<Key> reloadEffects = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("reloadEffects"));
        Collection<Key> tickEffects = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("tickEffects"));
        Collection<Key> noAmmoEffects = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("noAmmoEffects"));
        Collection<Key> gunStackMappers = KEY_COLLECTION_PROCESSOR.dataFromElement(element.getElementOrThrow("gunStackMappers"));

        return new GunLevelData(upgrades, stack, stats, shootTester, reloadTester, firer, activateEffects, shootEffects,
                reloadEffects, tickEffects, noAmmoEffects, gunStackMappers);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull GunLevelData gunLevelData) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode(12);
        node.put("upgrades", KEY_SET_PROCESSOR.elementFromData(gunLevelData.upgrades()));
        node.put("stack", stackProcessor.elementFromData(gunLevelData.stack()));
        node.put("stats", KEY_PROCESSOR.elementFromData(gunLevelData.stats()));
        node.put("shootTester", KEY_PROCESSOR.elementFromData(gunLevelData.shootTester()));
        node.put("reloadTester", KEY_PROCESSOR.elementFromData(gunLevelData.reloadTester()));
        node.put("firer", KEY_PROCESSOR.elementFromData(gunLevelData.firer()));
        node.put("activateEffects", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.activateEffects()));
        node.put("shootEffects", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.shootEffects()));
        node.put("reloadEffects", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.reloadEffects()));
        node.put("tickEffects", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.tickEffects()));
        node.put("noAmmoEffects", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.noAmmoEffects()));
        node.put("gunStackMappers", KEY_COLLECTION_PROCESSOR.elementFromData(gunLevelData.gunStackMappers()));

        return node;
    }
}
