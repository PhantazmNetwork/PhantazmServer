package com.github.phantazmnetwork.zombies.equipment.gun.audience;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An {@link AudienceProvider} which returns an {@link Entity}'s {@link Instance}.
 */
public class EntityInstanceAudienceProvider implements AudienceProvider {

    /**
     * Data for an {@link EntityInstanceAudienceProvider}.
     */
    public record Data() implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY
                = Key.key(Namespaces.PHANTAZM, "gun.audience_provider.entity_instance");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
    }

    private final Supplier<Optional<? extends Entity>> entitySupplier;

    /**
     * Creates a new {@link EntityInstanceAudienceProvider}.
     * @param entitySupplier The {@link Supplier} of an {@link Entity} to retrieve the {@link Instance} of
     */
    public EntityInstanceAudienceProvider(@NotNull Supplier<Optional<? extends Entity>> entitySupplier) {
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
    }

    @Override
    public @NotNull Optional<? extends Instance> provideAudience() {
        return entitySupplier.get().map(Entity::getInstance);
    }
}
