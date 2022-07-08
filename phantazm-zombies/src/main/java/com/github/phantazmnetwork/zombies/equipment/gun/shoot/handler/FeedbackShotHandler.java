package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.AudienceProvider;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public class FeedbackShotHandler implements ShotHandler {

    public record Data(@NotNull Key audienceProviderKey, @NotNull Component message,
                       @NotNull Component headshotMessage) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.feedback");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Component> componentProcessor = AdventureConfigProcessors.component();
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key audienceProviderKey = keyProcessor.dataFromElement(element.getElementOrThrow("audienceProviderKey"));
                Component message = componentProcessor.dataFromElement(element.getElementOrThrow("message"));
                Component headshotMessage = componentProcessor.dataFromElement(element.getElementOrThrow("headshotMessage"));

                return new Data(audienceProviderKey, message, headshotMessage);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(3);
                node.put("audienceProviderKey", keyProcessor.elementFromData(data.audienceProviderKey));
                node.put("message", componentProcessor.elementFromData(data.message()));
                node.put("headshotMessage", componentProcessor.elementFromData(data.headshotMessage()));

                return node;
            }
        };
    }

    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> keys.add(data.audienceProviderKey());
    }

    private final Data data;

    private final AudienceProvider audienceProvider;

    public FeedbackShotHandler(@NotNull Data data, @NotNull AudienceProvider audienceProvider) {
        this.data = Objects.requireNonNull(data, "data");
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        audienceProvider.provideAudience().ifPresent(audience -> {
            for (GunHit ignored : shot.regularTargets()) {
                audience.sendMessage(data.message());
            }
            for (GunHit ignored : shot.headshotTargets()) {
                audience.sendMessage(data.headshotMessage());
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }


}
