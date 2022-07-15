package com.github.phantazmnetwork.messaging;

/**
 * Contains shared message channel ID values.
 * Message channels should use the 1.13+ namespaced key format. These constants represent the key's value.
 */
public final class MessageChannels {


    /**
     * The message channel between a server and the proxy.
     */
    public static String PROXY = "proxy";

    private MessageChannels() {
        throw new UnsupportedOperationException();
    }

}
