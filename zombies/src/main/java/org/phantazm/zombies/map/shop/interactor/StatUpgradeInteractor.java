package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.UUID;

@Model("zombies.map.shop.interactor.stat_upgrade")
@Cache(false)
public class StatUpgradeInteractor extends InteractorBase<StatUpgradeInteractor.Data> {
    private final UUID id;

    @FactoryMethod
    public StatUpgradeInteractor(@NotNull Data data) {
        super(data);
        this.id = UUID.randomUUID();
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Effect effect = new Effect(this.data, this.id, interaction.player());
        if (interaction.player().addActivable(effect)) return true;

        Activable existing = interaction.player().getActivable(effect);
        if (!(existing instanceof Effect actualEffect)) return false;

        actualEffect.upgrade();
        return true;
    }

    private static class Effect implements Activable {
        private final Data data;
        private final UUID uuid;
        private final ZombiesPlayer zombiesPlayer;
        private final Object sync;
        private final Attribute attribute;

        private boolean ended;

        private Effect(Data data, UUID uuid, ZombiesPlayer zombiesPlayer) {
            this.data = data;
            this.uuid = uuid;
            this.zombiesPlayer = zombiesPlayer;
            this.sync = new Object();

            this.attribute = Attributes.get(data.attribute);
        }

        @Override
        public void start() {
            if (data.amount == 0 || ended) return;

            synchronized (sync) {
                if (ended) return;

                zombiesPlayer.getPlayer().ifPresent(player -> {
                    AttributeInstance instance = player.getAttribute(this.attribute);
                    instance.removeModifier(uuid);

                    UUID id = UUID.randomUUID();
                    String idString = id.toString();
                    instance.addModifier(new AttributeModifier(id, idString, data.amount, data.operation));
                });
            }
        }

        @Override
        public void end() {
            this.ended = true;
        }

        private void upgrade() {
            if (ended) return;

            // this just applies the attribute again, with synchronization to prevent race conditions
            start();
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;

            if (!(obj instanceof Effect effect)) return false;

            return this.uuid.equals(effect.uuid);
        }
    }

    @DataObject
    public record Data(@NotNull String attribute,
        double amount,
        @NotNull AttributeOperation operation) {
    }
}