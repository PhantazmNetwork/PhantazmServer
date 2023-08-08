package org.phantazm.server.role;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public sealed interface Role permits Role.RoleImpl {
    Tag<List<String>> ROLE_TAG = Tag.String("player_roles").list().defaultValue(List.of());

    Role NONE = new RoleImpl("none", player -> {
        return Optional.ofNullable(player.getDisplayName()).orElseGet(player::getName);
    }, Integer.MIN_VALUE, Set.of());

    @NotNull String identifier();

    @Nullable Component styleName(@NotNull Player player);

    int priority();

    @NotNull @Unmodifiable Set<Permission> grantedPermissions();

    static @NotNull Role of(@NotNull String identifier,
            @NotNull Function<? super Player, ? extends Component> styleFunction, int priority,
            @NotNull Set<Permission> permissions) {
        Objects.requireNonNull(identifier, "identifier");
        Objects.requireNonNull(styleFunction, "styleFunction");
        return new RoleImpl(identifier, styleFunction, priority, Set.copyOf(permissions));
    }

    final class RoleImpl implements Role {
        private final String identifier;
        private final Function<? super Player, ? extends Component> styleFunction;
        private final int priority;
        private final Set<Permission> permissions;

        private RoleImpl(String identifier, Function<? super Player, ? extends Component> styleFunction, int priority,
                Set<Permission> permissions) {
            this.identifier = identifier;
            this.styleFunction = styleFunction;
            this.priority = priority;
            this.permissions = permissions;
        }

        @Override
        public @NotNull String identifier() {
            return identifier;
        }

        @Override
        public @Nullable Component styleName(@NotNull Player player) {
            return styleFunction.apply(player);
        }

        @Override
        public int priority() {
            return priority;
        }

        @Override
        public @NotNull @Unmodifiable Set<Permission> grantedPermissions() {
            return permissions;
        }

        @Override
        public int hashCode() {
            return identifier.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            if (obj instanceof RoleImpl other) {
                return identifier.equals(other.identifier);
            }

            return false;
        }
    }
}
