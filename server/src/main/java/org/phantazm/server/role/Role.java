package org.phantazm.server.role;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Role permits Role.RoleImpl {
    Role NONE = new RoleImpl("none", player -> {
        return Optional.ofNullable(player.getDisplayName()).orElseGet(player::getName);
    }, ignored -> {
    }, Integer.MIN_VALUE, Set.of());

    static @NotNull Role of(@NotNull String identifier,
        @NotNull Function<? super Player, ? extends Component> chatStyleFunction,
        @NotNull Consumer<? super Player> displayNameStyler, int priority, @NotNull Set<Permission> permissions) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(chatStyleFunction);
        Objects.requireNonNull(displayNameStyler);
        return new RoleImpl(identifier, chatStyleFunction, displayNameStyler, priority, Set.copyOf(permissions));
    }

    @NotNull
    String identifier();

    @Nullable
    Component styleChatName(@NotNull Player player);

    void styleDisplayName(@NotNull Player player);

    int priority();

    @NotNull
    @Unmodifiable
    Set<Permission> grantedPermissions();

    final class RoleImpl implements Role {
        private final String identifier;
        private final Function<? super Player, ? extends Component> chatStyleFunction;
        private final Consumer<? super Player> displayNameStyler;
        private final int priority;
        private final Set<Permission> permissions;

        private RoleImpl(String identifier, Function<? super Player, ? extends Component> chatStyleFunction,
            Consumer<? super Player> displayNameStyler, int priority, Set<Permission> permissions) {
            this.identifier = identifier;
            this.chatStyleFunction = chatStyleFunction;
            this.displayNameStyler = displayNameStyler;
            this.priority = priority;
            this.permissions = permissions;
        }

        @Override
        public @NotNull String identifier() {
            return identifier;
        }

        @Override
        public @Nullable Component styleChatName(@NotNull Player player) {
            return chatStyleFunction.apply(player);
        }

        @Override
        public void styleDisplayName(@NotNull Player player) {
            displayNameStyler.accept(player);
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

            if (obj instanceof Role other) {
                return identifier.equals(other.identifier());
            }

            return false;
        }
    }
}
