package org.phantazm.messaging;

/**
 * Contains shared message channel ID values.
 * Message channels should use the 1.13+ namespaced key format. These constants represent the key's value.
 */
public final class MessageChannels {


    /**
     * The message channel between the proxy and a server.
     */
    public static final String PROXY_TO_SERVER = "proxy2server";

    /**
     * The message channel between the client and a server.
     */
    public static final String CLIENT_TO_SERVER = "client2server";

    private MessageChannels() {
        throw new UnsupportedOperationException();
    }

}
