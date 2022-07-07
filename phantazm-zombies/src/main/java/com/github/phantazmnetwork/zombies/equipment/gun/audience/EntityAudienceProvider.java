package com.github.phantazmnetwork.zombies.equipment.gun.audience;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class EntityAudienceProvider implements AudienceProvider {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.audience_provider.entity");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                return new LinkedConfigNode(0);
            }
        };
    }

    private final Supplier<Optional<? extends Audience>> entitySupplier;

    public EntityAudienceProvider(@NotNull Supplier<Optional<? extends Audience>> entitySupplier) {
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
    }

    @Override
    public @NotNull Optional<? extends Audience> provideAudience() {
        return entitySupplier.get();
    }
}