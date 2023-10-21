package org.phantazm.zombies.equipment.perk.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.perk.effect.shot.ShotEffect;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Description("An effect that can perform arbitrary actions on entities shot by the player who has the effect.")
@Model("zombies.perk.effect.shot")
@Cache(false)
public class ShotEffectCreator implements PerkEffectCreator {
    private final EventNode<Event> rootNode;
    private final Collection<ShotEffect> actions;

    @FactoryMethod
    public ShotEffectCreator(@NotNull EventNode<Event> rootNode,
        @NotNull @Child("action") Collection<ShotEffect> actions) {
        this.rootNode = rootNode;
        this.actions = List.copyOf(actions);
    }

    @Override
    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Effect(rootNode, zombiesPlayer, actions);
    }

    private static class Effect implements PerkEffect {
        private final EventNode<Event> rootNode;
        private final ZombiesPlayer zombiesPlayer;
        private final Collection<ShotEffect> actions;

        private final EventListener<EntityDamageByGunEvent> listener;

        private final Tickable[] tickableActions;

        private Effect(EventNode<Event> rootNode, ZombiesPlayer zombiesPlayer, Collection<ShotEffect> actions) {
            this.rootNode = rootNode;
            this.zombiesPlayer = zombiesPlayer;
            this.actions = actions;

            this.listener = new EventListener<>() {
                @Override
                public @NotNull Class<EntityDamageByGunEvent> eventType() {
                    return EntityDamageByGunEvent.class;
                }

                @Override
                public @NotNull Result run(@NotNull EntityDamageByGunEvent event) {
                    onEntityDamageByGun(event);
                    return Result.SUCCESS;
                }
            };

            this.tickableActions =
                actions.stream().filter(action -> action instanceof Tickable).map(action -> (Tickable) action)
                    .toArray(Tickable[]::new);
        }

        @Override
        public void start() {
            rootNode.addListener(listener);
        }

        @Override
        public void tick(long time) {
            for (Tickable tickable : tickableActions) {
                tickable.tick(time);
            }
        }

        @Override
        public void end() {
            rootNode.removeListener(listener);
        }

        private void onEntityDamageByGun(EntityDamageByGunEvent event) {
            Entity shooter = event.getShooter();
            if (!(shooter instanceof Player player)) {
                return;
            }

            UUID uuid = player.getUuid();
            if (uuid.equals(zombiesPlayer.getUUID())) {
                for (ShotEffect action : actions) {
                    action.perform(event.getEntity(), zombiesPlayer);
                }
            }
        }
    }

    @DataObject
    public record Data(
        @NotNull @Description(
            "The actions to be executed on the entities that are hit by gun shots") @ChildPath(
            "action") Collection<String> actions) {
    }
}
