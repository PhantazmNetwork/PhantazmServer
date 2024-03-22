package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Optional;
import java.util.function.Supplier;

@Model("zombies.map.shop.interactor.dragons_wrath")
@Cache(false)
public class DragonsWrathInteractor implements ShopInteractor {
    private final Data data;
    private final Supplier<? extends RoundHandler> roundHandler;
    private Shop shop;

    @FactoryMethod
    public DragonsWrathInteractor(@NotNull Data data, @NotNull Supplier<? extends RoundHandler> roundHandler) {
        this.data = data;
        this.roundHandler = roundHandler;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Instance instance = shop.instance();
        RoundHandler roundHandler = this.roundHandler.get();
        Optional<Round> currentRoundOptional = roundHandler.currentRound();
        if (currentRoundOptional.isEmpty()) {
            return false;
        }

        Round currentRound = currentRoundOptional.get();
        Wrapper<Integer> killCount = Wrapper.of(0);
        instance.getEntityTracker()
            .nearbyEntities(shop.center(), data.radius, EntityTracker.Target.LIVING_ENTITIES, entity -> {
                if (!currentRound.hasMob(entity.getUuid())) {
                    return;
                }

                if (!(entity instanceof Mob mob)) {
                    return;
                }

                if (mob.data().extra().getBooleanOrDefault(ExtraNodeKeys.RESIST_INSTAKILL, false)) {
                    return;
                }

                Entity lightningBolt = new Entity(EntityType.LIGHTNING_BOLT);
                lightningBolt.setInstance(instance, entity.getPosition());
                lightningBolt.scheduleRemove(20, TimeUnit.SERVER_TICK);

                instance.playSound(data.sound());

                interaction.player().getPlayer().ifPresent(player -> {
                    entity.getAcquirable().sync(ignored -> {
                        entity.damage(Damage.fromPlayer(player, entity.getHealth()), true);
                    });
                });

                killCount.set(killCount.get() + 1);
            });

        TagResolver killCountPlaceholder = Placeholder.component("kill_count", Component.text(killCount.get()));
        Component message = MiniMessage.miniMessage().deserialize(data.messageFormat(), killCountPlaceholder);
        if (data.broadcast()) {
            instance.sendMessage(message);
        } else {
            interaction.player().getPlayer().ifPresent(player -> player.sendMessage(message));
        }

        return true;
    }

    @DataObject
    public record Data(@NotNull Sound sound,
        @NotNull String messageFormat,
        boolean broadcast,
        double radius) {
    }
}
