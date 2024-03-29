package org.phantazm.zombies.listener;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class EntityDamageByGunEventListener extends PhantazmMobEventListener<EntityDamageByGunEvent> {
    private final MapObjects mapObjects;
    private final Map<PlayerView, ZombiesPlayer> playerMap;

    public EntityDamageByGunEventListener(@NotNull Instance instance,
        @NotNull MapObjects mapObjects, @NotNull Map<PlayerView, ZombiesPlayer> playerMap, @NotNull Supplier<ZombiesScene> scene) {
        super(instance, scene);
        this.mapObjects = Objects.requireNonNull(mapObjects);
        this.playerMap = Objects.requireNonNull(playerMap);
    }

    @Override
    protected void accept(@NotNull ZombiesScene scene, @NotNull Mob mob, @NotNull EntityDamageByGunEvent event) {
        if (event.isInstakill()) {
            if (mobResistsInstakill(mob)) {
                event.setInstakill(false);
            }
        } else if ((mapObjects.module().flags().hasFlag(Flags.INSTA_KILL) ||
            playerHasInstakill(event.getShooter().getUuid())) && !mobResistsInstakill(mob)) {
            event.setInstakill(true);
        }
    }

    private boolean playerHasInstakill(UUID uuid) {
        ZombiesPlayer player = playerMap.get(PlayerView.lookup(uuid));
        return player != null && player.module().flags().hasFlag(Flags.INSTA_KILL);
    }

    private boolean mobResistsInstakill(Mob mob) {
        return mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_INSTAKILL);
    }
}
