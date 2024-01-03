package org.phantazm.core.role;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

@Model("role.basic")
@Cache(false)
public class BasicRoleCreator implements RoleCreator {
    private final Data data;

    @FactoryMethod
    public BasicRoleCreator(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Role get() {
        return Role.of(data.identifier, this::styleChat, this::styleDisplayName, this::styleName, data.priority,
            data.permissions.stream().map(Permission::new).collect(Collectors.toUnmodifiableSet()));
    }

    private Component styleChat(Player player) {
        TagResolver name = Placeholder.unparsed("name", player.getUsername());
        return MiniMessage.miniMessage().deserialize(data.chatFormat, name);
    }

    private void styleDisplayName(Player player) {
        if (data.displayNameFormat == null) {
            return;
        }

        TagResolver nameTag = Placeholder.unparsed("name", player.getUsername());
        player.setDisplayName(MiniMessage.miniMessage().deserialize(data.displayNameFormat, nameTag));
    }

    private Component styleName(String name) {
        if (data.displayNameFormat == null) {
            return Component.text(name);
        }

        return MiniMessage.miniMessage().deserialize(data.displayNameFormat, Placeholder.unparsed("name", name));
    }

    @DataObject
    public record Data(
        @NotNull String identifier,
        @NotNull String chatFormat,
        @Nullable String displayNameFormat,
        int priority,
        @NotNull Set<String> permissions) {

    }
}
