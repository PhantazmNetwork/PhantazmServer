package org.phantazm.zombies.equipment.gun.audience;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An {@link AudienceProvider} which returns an {@link Entity}'s {@link Instance}.
 */
@Model("zombies.gun.audience_provider.entity_instance")
@Cache(false)
public class EntityInstanceAudienceProvider implements AudienceProvider {

    private final Supplier<Optional<? extends Entity>> entitySupplier;

    /**
     * Creates a new {@link EntityInstanceAudienceProvider}.
     *
     * @param entitySupplier The {@link Supplier} of an {@link Entity} to retrieve the {@link Instance} of
     */
    @FactoryMethod
    public EntityInstanceAudienceProvider(@NotNull @Depend("zombies.dependency.gun.entity_supplier")
    Supplier<Optional<? extends Entity>> entitySupplier) {
        this.entitySupplier = Objects.requireNonNull(entitySupplier);
    }

    @Override
    public @NotNull Optional<? extends Instance> provideAudience() {
        return entitySupplier.get().map(Entity::getInstance);
    }

}
