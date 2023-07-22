package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;
import org.phantazm.zombies.player.state.revive.KnockedPlayerState;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Model("zombies.map.shop.interactor.revive_all")
@Cache(false)
public class ReviveAllInteractor implements ShopInteractor {
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
    private final Pos respawnPos;

    @FactoryMethod
    public ReviveAllInteractor(@NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap, @NotNull Pos respawnPos) {
        this.playerMap = playerMap;
        this.respawnPos = respawnPos;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Optional<Player> playerOptional = interaction.player().getPlayer();
        if (playerOptional.isEmpty()) {
            return false;
        }

        Player reviver = playerOptional.get();
        Component reviveName = reviver.getDisplayName();
        for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            ZombiesPlayerState state = zombiesPlayer.module().getStateSwitcher().getState();
            Point reviveLocation = null;

            if (state instanceof KnockedPlayerState knocked) {
                reviveLocation = knocked.getReviveHandler().context().getKnockLocation();
            }

            boolean dead = zombiesPlayer.isDead();
            if (dead || zombiesPlayer.isKnocked()) {
                if (dead) {
                    zombiesPlayer.getPlayer().ifPresent(player -> player.teleport(respawnPos));
                }

                zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE,
                        AlivePlayerStateContext.revive(reviveName, reviveLocation));
            }
        }

        return true;
    }
}
