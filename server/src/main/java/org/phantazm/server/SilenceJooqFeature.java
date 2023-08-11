package org.phantazm.server;

public final class SilenceJooqFeature {
    private SilenceJooqFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize() {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
    }
}
