package com.github.phantazmnetwork.core.chat;

import com.github.phantazmnetwork.core.player.PlayerViewProvider;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@link ChatChannel} that sends a message to an entire {@link Instance}.
 */
public class InstanceChatChannel extends BasicChatChannel {

    /**
     * Creates a {@link InstanceChatChannel}.
     *
     * @param viewProvider The {@link InstanceChatChannel}'s {@link PlayerViewProvider}
     */
    public InstanceChatChannel(@NotNull PlayerViewProvider viewProvider) {
        super(viewProvider);
    }

    @Override
    protected @NotNull Pair<Audience, ObjectBooleanPair<Component>> getAudience(@NotNull Player player) {
        Instance instance = player.getInstance();
        if (instance == null) {
            return Pair.of(null,
                           ObjectBooleanPair.of(Component.text("You are not in an instance.", NamedTextColor.RED), true)
            );
        }

        return Pair.of((ForwardingAudience)() -> List.of(instance), null);
    }

}
