package org.phantazm.core.guild.party;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public class Flags {

    public static final Key ALL_INVITE = Key.key(Namespaces.PHANTAZM, "party.allinvite");

    private Flags() {
        throw new UnsupportedOperationException();
    }

}
