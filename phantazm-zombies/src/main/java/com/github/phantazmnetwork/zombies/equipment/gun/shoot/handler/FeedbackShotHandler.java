package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
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

public class FeedbackShotHandler implements ShotHandler {

    public record Data(@NotNull Component message, @NotNull Component headshotMessage) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.feedback");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Component> componentProcessor = AdventureConfigProcessors.component();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Component message = componentProcessor.dataFromElement(element.getElementOrThrow("message"));
                Component headshotMessage = componentProcessor.dataFromElement(element.getElementOrThrow("headshotMessage"));
                return new Data(message, headshotMessage);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("message", componentProcessor.elementFromData(data.message()));
                node.put("headshotMessage", componentProcessor.elementFromData(data.headshotMessage()));

                return node;
            }
        };
    }

    private final Data data;

    private final PlayerView playerView;

    public FeedbackShotHandler(@NotNull Data data, @NotNull PlayerView playerView) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        playerView.getPlayer().ifPresent(player -> {
            for (GunHit ignored : shot.regularTargets()) {
                player.sendMessage(data.message());
            }
            for (GunHit ignored : shot.headshotTargets()) {
                player.sendMessage(data.headshotMessage());
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }


}
