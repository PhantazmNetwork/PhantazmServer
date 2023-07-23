package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
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
    private final MobStore mobStore;
    private Shop shop;

    @FactoryMethod
    public DragonsWrathInteractor(@NotNull Data data, @NotNull Supplier<? extends RoundHandler> roundHandler,
            @NotNull MobStore mobStore) {
        this.data = data;
        this.roundHandler = roundHandler;
        this.mobStore = mobStore;
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
        instance.getEntityTracker()
                .nearbyEntities(shop.center(), data.radius, EntityTracker.Target.LIVING_ENTITIES, entity -> {
                    if (!currentRound.hasMob(entity.getUuid())) {
                        return;
                    }

                    PhantazmMob mob = mobStore.getMob(entity.getUuid());
                    if (mob == null ||
                            mob.model().getExtraNode().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_INSTAKILL)) {
                        return;
                    }

                    Entity lightningBolt = new Entity(EntityType.LIGHTNING_BOLT);
                    lightningBolt.setInstance(instance, entity.getPosition());
                    lightningBolt.scheduleRemove(20, TimeUnit.SERVER_TICK);

                    instance.playSound(
                            Sound.sound(Key.key("minecraft:entity.lightning_bolt.thunder"), Sound.Source.PLAYER, 20,
                                    1));

                    entity.kill();
                });

        return true;
    }

    @DataObject
    public record Data(double radius) {
    }
}
