package com.github.phantazmnetwork.zombies.equipment.gun.audience;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class EntityInstanceAudienceProvider implements AudienceProvider {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY
                = Key.key(Namespaces.PHANTAZM, "gun.audience_provider.entity_instance");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
    }

    private final Supplier<Optional<? extends Entity>> entitySupplier;

    public EntityInstanceAudienceProvider(@NotNull Supplier<Optional<? extends Entity>> entitySupplier) {
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
    }

    @Override
    public @NotNull Optional<? extends Audience> provideAudience() {
        return entitySupplier.get().map(Entity::getInstance);
    }
}
