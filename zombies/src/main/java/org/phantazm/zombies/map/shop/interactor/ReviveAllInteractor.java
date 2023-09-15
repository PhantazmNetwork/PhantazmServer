package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;
import org.phantazm.zombies.player.state.revive.KnockedPlayerState;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Model("zombies.map.shop.interactor.revive_all")
@Cache(false)
public class ReviveAllInteractor implements ShopInteractor {
    private final Data data;
    private final Map<PlayerView, ZombiesPlayer> playerMap;
    private final Pos respawnPos;
    private Instance instance;

    @FactoryMethod
    public ReviveAllInteractor(@NotNull Data data, @NotNull Map<PlayerView, ZombiesPlayer> playerMap,
        @NotNull Pos respawnPos) {
        this.data = Objects.requireNonNull(data);
        this.playerMap = playerMap;
        this.respawnPos = respawnPos;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.instance = shop.instance();
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Optional<Player> playerOptional = interaction.player().getPlayer();
        if (playerOptional.isEmpty()) {
            return false;
        }

        Player reviver = playerOptional.get();
        Component reviveName = reviver.getDisplayName();
        int reviveCount = 0;
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
                ++reviveCount;
            }
        }

        TagResolver reviveCountPlaceholder = Placeholder.component("revive_count", Component.text(reviveCount));
        Component message = MiniMessage.miniMessage().deserialize(data.messageFormat(), reviveCountPlaceholder);
        if (data.broadcast()) {
            instance.sendMessage(message);
        } else {
            reviver.sendMessage(message);
        }

        return true;
    }

    @DataObject
    public record Data(String messageFormat,
        boolean broadcast) {

    }

}
