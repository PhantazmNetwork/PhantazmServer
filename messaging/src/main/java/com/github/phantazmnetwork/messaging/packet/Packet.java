package com.github.phantazmnetwork.messaging.packet;

import org.jetbrains.annotations.NotNull;

public interface Packet {

    byte getId();

    void write(@NotNull DataWriter buf);
}
