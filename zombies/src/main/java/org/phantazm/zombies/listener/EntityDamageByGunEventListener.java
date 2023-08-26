package org.phantazm.zombies.listener;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.event.EntityDamageByGunEvent;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EntityDamageByGunEventListener extends PhantazmMobEventListener<EntityDamageByGunEvent> {
    private final MapObjects mapObjects;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    public EntityDamageByGunEventListener(@NotNull Instance instance,
        @NotNull MapObjects mapObjects, @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
        super(instance);
        this.mapObjects = Objects.requireNonNull(mapObjects);
        this.playerMap = Objects.requireNonNull(playerMap);
    }

    @Override
    protected void accept(@NotNull Mob mob, @NotNull EntityDamageByGunEvent event) {
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
        ZombiesPlayer player = playerMap.get(uuid);
        return player != null && player.module().flags().hasFlag(Flags.INSTA_KILL);
    }

    private boolean mobResistsInstakill(Mob mob) {
        return mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_INSTAKILL);
    }
}
