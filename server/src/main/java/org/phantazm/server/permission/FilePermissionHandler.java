package org.phantazm.server.permission;

import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.minestom.server.command.CommandSender;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class FilePermissionHandler implements PermissionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilePermissionHandler.class);

    private final ConfigProcessor<PermissionData> permissionDataConfigProcessor;
    private final ConfigCodec configCodec;
    private final Path permissionsFile;
    private final PermissionData permissionData;

    public FilePermissionHandler(@NotNull MappingProcessorSource mappingProcessorSource,
            @NotNull ConfigCodec configCodec, @NotNull Path permissionsFile) {
        this.permissionsFile = Objects.requireNonNull(permissionsFile, "permissionsFile");
        this.permissionDataConfigProcessor = mappingProcessorSource.processorFor(Token.ofClass(PermissionData.class));
        this.configCodec = Objects.requireNonNull(configCodec, "configCodec");

        PermissionData permissionData;
        try {
            FileUtils.createFileIfNotExists(permissionsFile);
            permissionData = Configuration.read(permissionsFile, configCodec, permissionDataConfigProcessor);
        }
        catch (Throwable e) {
            LOGGER.warn("Exception reading permissions file", e);
            permissionData = new PermissionData(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        }

        this.permissionData = permissionData;
    }

    @Override
    public void applyPermissions(@NotNull UUID uuid, @NotNull CommandSender sender) {
        applyTo(sender, ALL_GROUP);

        Set<String> groups = permissionData.groups().get(uuid);
        if (groups != null) {
            for (String group : groups) {
                applyTo(sender, group);
            }
        }
    }

    @Override
    public void flush() {
        try {
            FileUtils.createFileIfNotExists(permissionsFile);
            Configuration.write(permissionsFile, configCodec, permissionDataConfigProcessor, permissionData);
        }
        catch (Throwable e) {
            LOGGER.warn("Exception writing permissions file", e);
        }
    }

    @Override
    public void addGroupPermission(@NotNull String group, @NotNull Permission permission) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(permission, "permission");

        permissionData.groupPermissions().computeIfAbsent(group, ignored -> new CopyOnWriteArraySet<>())
                .add(permission);
    }

    @Override
    public void removeGroupPermission(@NotNull String group, @NotNull Permission permission) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(permission, "permission");

        Map<String, CopyOnWriteArraySet<Permission>> groupPermissions = permissionData.groupPermissions();
        Set<Permission> permissions = groupPermissions.get(group);
        if (permissions != null) {
            permissions.remove(permission);

            if (permissions.isEmpty()) {
                groupPermissions.remove(group);
            }
        }
    }

    @Override
    public void addToGroup(@NotNull UUID uuid, @NotNull String group) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(group, "group");

        permissionData.groups().computeIfAbsent(uuid, ignored -> new CopyOnWriteArraySet<>()).add(group);
    }

    @Override
    public void removeFromGroup(@NotNull UUID uuid, @NotNull String group) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(group, "group");

        Map<UUID, CopyOnWriteArraySet<String>> playerGroups = permissionData.groups();
        Set<String> groups = playerGroups.get(uuid);
        if (groups != null) {
            groups.remove(group);

            if (groups.isEmpty()) {
                playerGroups.remove(uuid);
            }
        }
    }

    private void applyTo(CommandSender sender, String groupName) {
        Set<Permission> permissions = permissionData.groupPermissions().get(groupName);

        if (permissions != null) {
            for (Permission permission : permissions) {
                sender.addPermission(permission);
            }
        }
    }
}
