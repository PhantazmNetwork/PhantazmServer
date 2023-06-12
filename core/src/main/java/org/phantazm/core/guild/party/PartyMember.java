package org.phantazm.core.guild.party;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.Ranked;
import org.phantazm.core.player.PlayerView;

public class PartyMember extends GuildMember implements Ranked {

    private int rank;

    public PartyMember(@NotNull PlayerView playerView, int rank) {
        super(playerView);
        this.rank = rank;
    }

    @Override
    public int rank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
