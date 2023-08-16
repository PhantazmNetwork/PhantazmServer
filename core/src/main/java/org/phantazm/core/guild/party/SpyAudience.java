package org.phantazm.core.guild.party;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.*;

public class SpyAudience implements ForwardingAudience {

    private final Map<UUID, PlayerView> playerSpies = new HashMap<>();

    private final PacketGroupingAudience playerAudience = new PacketGroupingAudience() {
        @Override
        public @NotNull Collection<@NotNull Player> getPlayers() {
            List<Player> players = new ArrayList<>(playerSpies.size());
            for (PlayerView spy : playerSpies.values()) {
                spy.getPlayer().ifPresent(players::add);
            }

            return players;
        }
    };

    private final Set<Audience> extraAudiences = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        List<Audience> audiences = new ArrayList<>(extraAudiences.size() + 1);
        audiences.addAll(extraAudiences);
        audiences.add(playerAudience);

        return audiences;
    }

    public void addPlayerSpy(@NotNull PlayerView spy) {
        playerSpies.put(spy.getUUID(), spy);
    }

    public void removePlayerSpy(@NotNull UUID spyUUID) {
        playerSpies.remove(spyUUID);
    }

    public boolean hasPlayerSpy(@NotNull UUID spyUUID) {
        return playerSpies.containsKey(spyUUID);
    }

    public void addExtraSpy(@NotNull Audience audience) {
        extraAudiences.add(audience);
    }

    public void removeExtraSpy(@NotNull Audience audience) {
        extraAudiences.remove(audience);
    }

    public boolean hasExtraSpy(@NotNull Audience audience) {
        return extraAudiences.contains(audience);
    }

}
