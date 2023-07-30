package org.phantazm.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

public class HikariFeature {

    private static HikariDataSource dataSource;

    static void initialize() {
        HikariConfig config = new HikariConfig("./hikari.properties");
        dataSource = new HikariDataSource(config);
    }

    public static @NotNull HikariDataSource getDataSource() {
        return FeatureUtils.check(dataSource);
    }

    public static void end() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

}
