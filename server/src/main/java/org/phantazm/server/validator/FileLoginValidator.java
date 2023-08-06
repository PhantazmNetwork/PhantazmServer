package org.phantazm.server.validator;

import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileLoginValidator implements LoginValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileLoginValidator.class);
    private static final BooleanObjectPair<Component> SUCCESS = BooleanObjectPair.of(true, Component.empty());

    private final boolean isWhitelist;
    private final Path whitelistPath;
    private final Path banPath;
    private final Set<UUID> whitelist;
    private final Map<UUID, Component> banned;

    private volatile boolean bansDirty;
    private volatile boolean whitelistDirty;

    public FileLoginValidator(boolean isWhitelist, @NotNull Path whitelist, @NotNull Path bans) {
        this.isWhitelist = isWhitelist;
        this.whitelistPath = Objects.requireNonNull(whitelist, "whitelist");
        this.banPath = Objects.requireNonNull(bans, "bans");

        try {
            FileUtils.createFileIfNotExists(whitelist);
            FileUtils.createFileIfNotExists(bans);
        }
        catch (IOException e) {
            LOGGER.warn("Error creating ban or whitelist file.");
        }

        this.whitelist = loadWhitelist(whitelist);
        this.banned = loadBans(bans);
    }

    private static Set<UUID> loadWhitelist(@NotNull Path path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path).stream().filter(string -> !string.isEmpty()).toList();
        }
        catch (IOException e) {
            LOGGER.warn("Exception reading whitelist file", e);
            return Collections.newSetFromMap(new ConcurrentHashMap<>());
        }

        Set<UUID> uuids = Collections.newSetFromMap(new ConcurrentHashMap<>());
        for (String line : lines) {
            try {
                uuids.add(UUID.fromString(line));
            }
            catch (IllegalArgumentException e) {
                LOGGER.warn("Malformed UUID {} in whitelist file", line);
            }
        }

        return uuids;
    }

    private static Map<UUID, Component> loadBans(@NotNull Path path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path).stream().filter(string -> !string.isEmpty()).toList();
        }
        catch (IOException e) {
            LOGGER.warn("Exception reading whitelist file", e);
            return new ConcurrentHashMap<>();
        }

        Map<UUID, Component> bans = new ConcurrentHashMap<>();
        for (String line : lines) {
            String[] split = line.split(":", 2);
            if (split.length != 2) {
                LOGGER.warn("Invalid ban entry {}", line);
                continue;
            }

            UUID uuid;
            try {
                uuid = UUID.fromString(split[0]);
            }
            catch (IllegalArgumentException e) {
                LOGGER.warn("Malformed UUID {} in ban file", split[0]);
                continue;
            }

            Component reason = MiniMessage.miniMessage().deserialize(split[1]);
            bans.put(uuid, reason);
        }

        return bans;
    }

    @Override
    public @NotNull BooleanObjectPair<Component> validateLogin(@NotNull UUID uuid) {
        Component bannedReason = banned.get(uuid);
        if (bannedReason != null) {
            return BooleanObjectPair.of(false, bannedReason);
        }

        if (isWhitelist && !whitelist.contains(uuid)) {
            return BooleanObjectPair.of(false, NOT_WHITELISTED_MESSAGE);
        }

        return SUCCESS;
    }

    @Override
    public void ban(@NotNull UUID uuid, @NotNull Component reason) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(reason, "reason");
        this.banned.put(uuid, reason);
        bansDirty = true;
    }

    @Override
    public boolean isBanned(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return banned.containsKey(uuid);
    }

    @Override
    public void pardon(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        if (this.banned.remove(uuid) != null) {
            bansDirty = true;
        }
    }

    @Override
    public void addWhitelist(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        if (this.whitelist.add(uuid)) {
            whitelistDirty = true;
        }
    }

    @Override
    public boolean isWhitelisted(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return whitelist.contains(uuid);
    }

    @Override
    public void removeWhitelist(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        if (this.whitelist.remove(uuid)) {
            whitelistDirty = true;
        }
    }

    @Override
    public void flush() {
        if (whitelistDirty) {
            List<String> text = whitelist.stream().map(UUID::toString).toList();

            try {
                Files.write(whitelistPath, text);
            }
            catch (IOException e) {
                LOGGER.warn("Error writing to whitelist file", e);
            }

            whitelistDirty = false;
        }

        if (bansDirty) {
            List<String> text = banned.entrySet().stream().map(entry -> entry.getKey().toString() + ":" +
                    MiniMessage.miniMessage().serialize(entry.getValue())).toList();

            try {
                Files.write(banPath, text);
            }
            catch (IOException e) {
                LOGGER.warn("Error writing to bans file", e);
            }

            bansDirty = false;
        }
    }
}
