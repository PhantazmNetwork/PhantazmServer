package org.phantazm.zombies.equipment.perk.effect;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.UUID;

public class ModifierPerkEffect implements PerkEffect {
    private final Attribute attribute;
    private final ZombiesPlayer zombiesPlayer;
    private final UUID uuid;
    private final String name;
    private final double amount;
    private final AttributeOperation attributeOperation;

    public ModifierPerkEffect(@NotNull Attribute attribute, @NotNull ZombiesPlayer zombiesPlayer, double amount,
            @NotNull AttributeOperation operation) {
        this.attribute = Objects.requireNonNull(attribute, "attribute");
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
        this.uuid = UUID.randomUUID();
        this.name = uuid.toString();
        this.amount = amount;
        this.attributeOperation = Objects.requireNonNull(operation, "operation");
    }

    @Override
    public void start() {
        zombiesPlayer.getPlayer().ifPresent(player -> player.getAttribute(attribute)
                .addModifier(new AttributeModifier(uuid, name, amount, attributeOperation)));
    }

    @Override
    public void end() {
        zombiesPlayer.getPlayer().ifPresent(player -> player.getAttribute(attribute).removeModifier(uuid));
    }
}
