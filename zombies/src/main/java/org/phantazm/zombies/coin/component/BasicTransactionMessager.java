package org.phantazm.zombies.coin.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MiniMessageUtils;
import org.phantazm.zombies.map.PlayerCoinsInfo;
import org.phantazm.zombies.player.action_bar.ZombiesPlayerActionBar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class BasicTransactionMessager implements TransactionMessager {

    private final ZombiesPlayerActionBar actionBar;

    private final MiniMessage miniMessage;

    private final PlayerCoinsInfo coinsInfo;

    private Component lastMessage = null;

    private long lastMessageTick;

    private long ticks;

    public BasicTransactionMessager(@NotNull ZombiesPlayerActionBar actionBar, @NotNull MiniMessage miniMessage,
            @NotNull PlayerCoinsInfo coinsInfo) {
        this.actionBar = Objects.requireNonNull(actionBar, "actionBar");
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
        this.coinsInfo = Objects.requireNonNull(coinsInfo, "coinsInfo");
    }

    @Override
    public void sendMessage(@NotNull List<Component> displays, int change) {
        TagResolver positivePlaceholder = Formatter.choice("positive", change >= 0 ? 1 : 0);
        TagResolver changePlaceholder = Placeholder.component("change", Component.text(Math.abs(change)));
        TagResolver displaysPresentPlaceholder = MiniMessageUtils.optional("displays_present", !displays.isEmpty());
        Collection<Component> mappedDisplays = new ArrayList<>(displays.size());
        for (Component display : displays) {
            mappedDisplays.add(miniMessage.deserialize(coinsInfo.transactionDisplayFormat(),
                    Placeholder.component("display", display)));
        }
        TagResolver displaysPlaceholder = MiniMessageUtils.list("displays", mappedDisplays);

        lastMessage =
                miniMessage.deserialize(coinsInfo.transactionMessageFormat(), positivePlaceholder, changePlaceholder,
                        displaysPresentPlaceholder, displaysPlaceholder);
        lastMessageTick = ticks;
    }

    @Override
    public void tick(long time) {
        ++ticks;

        if (lastMessage != null) {
            if (ticks - lastMessageTick > coinsInfo.actionBarDuration()) {
                actionBar.sendActionBar(Component.empty(), ZombiesPlayerActionBar.COINS_PRIORITY);
                lastMessage = null;
            } else {
                float progress = (float)(ticks - lastMessageTick) / coinsInfo.actionBarDuration();
                actionBar.sendActionBar(lerpColorRecursive(lastMessage, progress).asComponent(), ZombiesPlayerActionBar.COINS_PRIORITY);
            }
        }
    }

    private ComponentLike lerpColorRecursive(Component component, float progress) {
        List<ComponentLike> children = new ArrayList<>(component.children().size());
        for (Component child : component.children()) {
            children.add(lerpColorRecursive(child, progress));
        }

        TextColor oldColor = component.color();
        if (oldColor == null) {
            oldColor = coinsInfo.gradientFrom();
        }

        TextColor newColor = TextColor.lerp(progress, oldColor, coinsInfo.gradientTo());
        return component.children(children).color(newColor);
    }

}
