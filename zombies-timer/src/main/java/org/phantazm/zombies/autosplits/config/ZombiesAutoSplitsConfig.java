package org.phantazm.zombies.autosplits.config;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ZombiesAutoSplitsConfig(@NotNull String host, int port, boolean useLiveSplits, boolean useInternal) {

    public static final String DEFAULT_HOST = "localhost";

    public static final int DEFAULT_PORT = 16834;

    public static final boolean DEFAULT_USE_LIVE_SPLITS = false;

    public static final boolean DEFAULT_USE_INTERNAL = true;

    public ZombiesAutoSplitsConfig {
        Objects.requireNonNull(host, "host");
    }

    public @NotNull Builder toBuilder() {
        return new Builder()
                .setHost(host)
                .setPort(port)
                .setUseLiveSplits(useLiveSplits)
                .setUseInternal(useInternal);
    }

    public static class Builder {

        private String host = DEFAULT_HOST;

        private int port = DEFAULT_PORT;

        private boolean useLiveSplits = DEFAULT_USE_LIVE_SPLITS;

        private boolean useInternal = DEFAULT_USE_INTERNAL;

        public @NotNull Builder setHost(String host) {
            Objects.requireNonNull(host, "host");
            this.host = host;
            return this;
        }

        public @NotNull Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public @NotNull Builder setUseLiveSplits(boolean useLiveSplits) {
            this.useLiveSplits = useLiveSplits;
            return this;
        }

        public @NotNull Builder setUseInternal(boolean useInternal) {
            this.useInternal = useInternal;
            return this;
        }

        public @NotNull ZombiesAutoSplitsConfig build() {
            return new ZombiesAutoSplitsConfig(host, port, useLiveSplits, useInternal);
        }

    }

}
