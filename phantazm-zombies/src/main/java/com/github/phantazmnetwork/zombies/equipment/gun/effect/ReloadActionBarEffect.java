package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.AudienceProvider;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.ReloadActionBarChooser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A {@link GunEffect} that sends a message to an {@link Audience}'s action bar.
 */
public class ReloadActionBarEffect implements GunEffect {

    /**
     * Data for an {@link ReloadActionBarEffect}.
     * @param statsKey A {@link Key} to the guns's {@link GunStats}
     * @param audienceProviderKey A {@link Key} to the {@link ReloadActionBarEffect}'s {@link AudienceProvider}
     * @param reloadTesterKey A {@link Key} to the gun's {@link ReloadTester}
     * @param reloadActionBarChooserKey A {@link Key} to the {@link ReloadActionBarEffect}'s {@link ReloadActionBarChooser}
     */
    public record Data(@NotNull Key statsKey,
                       @NotNull Key audienceProviderKey,
                       @NotNull Key reloadTesterKey,
                       @NotNull Key reloadActionBarChooserKey) implements Keyed {

        /**
         * The serial {@link Key} for this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.action_bar.reload");

        /**
         * Creates a {@link Data}.
         * @param statsKey A {@link Key} to the guns's {@link GunStats}
         * @param audienceProviderKey A {@link Key} to the {@link ReloadActionBarEffect}'s {@link AudienceProvider}
         * @param reloadTesterKey A {@link Key} to the gun's {@link ReloadTester}
         * @param reloadActionBarChooserKey A {@link Key} to the {@link ReloadActionBarEffect}'s {@link ReloadActionBarChooser}
         */
        public Data {
            Objects.requireNonNull(statsKey, "statsKey");
            Objects.requireNonNull(audienceProviderKey, "audienceProviderKey");
            Objects.requireNonNull(reloadTesterKey, "reloadTesterKey");
            Objects.requireNonNull(reloadActionBarChooserKey, "reloadActionBarChooserKey");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}.
     * @return A {@link ConfigProcessor} for {@link Data}
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key statsKey = keyProcessor.dataFromElement(element.getElementOrThrow("statsKey"));
                Key audienceProviderKey = keyProcessor.dataFromElement(element.getElementOrThrow("audienceProviderKey"));
                Key reloadTesterKey = keyProcessor.dataFromElement(element.getElementOrThrow("reloadTesterKey"));
                Key reloadActionBarChooserKey = keyProcessor.dataFromElement(element.getElementOrThrow("reloadActionBarChooserKey"));

                return new Data(statsKey, audienceProviderKey, reloadTesterKey, reloadActionBarChooserKey);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(4);
                node.put("statsKey", keyProcessor.elementFromData(data.statsKey()));
                node.put("audienceProviderKey", keyProcessor.elementFromData(data.audienceProviderKey()));
                node.put("reloadTesterKey", keyProcessor.elementFromData(data.reloadTesterKey()));
                node.put("reloadActionBarChooserKey", keyProcessor.elementFromData(data.reloadActionBarChooserKey()));

                return node;
            }
        };
    }

    /**
     * Creates a dependency consumer for {@link Data}s.
     * @return A dependency consumer for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.statsKey());
            keys.add(data.audienceProviderKey());
            keys.add(data.reloadTesterKey());
            keys.add(data.reloadActionBarChooserKey());
        };
    }

    private final GunStats stats;

    private final AudienceProvider audienceProvider;

    private final ReloadTester reloadTester;

    private final ReloadActionBarChooser chooser;

    private boolean active = false;

    /**
     * Creates a {@link ReloadActionBarEffect}.
     * @param stats The gun's {@link GunStats}
     * @param audienceProvider A {@link AudienceProvider} to provide an {@link Audience} to send action bars to
     * @param reloadTester The gun's {@link ReloadTester}
     * @param chooser The {@link ReloadActionBarChooser} to choose an action bar to send to the {@link Audience}
     */
    public ReloadActionBarEffect(@NotNull GunStats stats, @NotNull AudienceProvider audienceProvider,
                                 @NotNull ReloadTester reloadTester, @NotNull ReloadActionBarChooser chooser) {
        this.stats = Objects.requireNonNull(stats, "stats");
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
        this.chooser = Objects.requireNonNull(chooser, "chooser");
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (reloadTester.isReloading(state) && state.isMainEquipment()) {
            float progress = (float) state.ticksSinceLastReload() / stats.reloadSpeed();
            audienceProvider.provideAudience().ifPresent(audience -> audience.sendActionBar(chooser.choose(state, progress)));
            active = true;
        }
        else if (active) {
            audienceProvider.provideAudience().ifPresent(audience -> audience.sendActionBar(Component.empty()));
            active = false;
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

}
