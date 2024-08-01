package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.permission.Permission;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.TagUtils;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerDamageEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerProcFireEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Optional;

public class DebugCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.debug");
    private static final Tag<Boolean> DEBUG_TAG = Tag.Boolean(TagUtils.uniqueTagName()).defaultValue(false);

    private final EventListener<ZombiesPlayerProcFireEvent> procFireListener;
    private final EventListener<ZombiesPlayerDamageEvent> damageEventListener;
    private final EventListener<EntityDamageByGunEvent> damageByGunListener;

    public DebugCommand() {
        super("debug", PERMISSION);
        this.procFireListener = EventListener.builder(ZombiesPlayerProcFireEvent.class).handler(this::onProcFire).build();
        this.damageEventListener = EventListener.builder(ZombiesPlayerDamageEvent.class).handler(this::onDamage).build();
        this.damageByGunListener = EventListener.builder(EntityDamageByGunEvent.class).handler(this::onDamageByGun).build();
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        scene.setLegit(false);

        sender.tagHandler().updateTag(DEBUG_TAG, current -> !current);

        if (scene.isDebug()) {
            return;
        }

        scene.setDebug(true);

        EventNode<Event> node = scene.sceneNode();
        node.addListener(procFireListener);
        node.addListener(damageEventListener);
        node.addListener(damageByGunListener);
    }

    private void onProcFire(ZombiesPlayerProcFireEvent event) {
        if (!event.getPlayer().getTag(DEBUG_TAG)) {
            return;
        }

        float damage = event.damageAmount();
        String player = event.getPlayer().getUsername();
        event.getPlayer().sendMessage(Component.text("[" + MinecraftServer.currentTick() +
            "] [" + player + "] Fire damage proc for " + damage, NamedTextColor.GRAY));
        event.getPlayer().sendMessage(Component.text("    - Target " + event.target().getEntityType(), NamedTextColor.DARK_GRAY));
    }

    private void onDamage(ZombiesPlayerDamageEvent event) {
        if (!event.getPlayer().getTag(DEBUG_TAG)) {
            return;
        }

        String player = event.getPlayer().getUsername();
        event.getPlayer().sendMessage(Component.text("[" + MinecraftServer.currentTick() +
            "] [" + player + "] Damaged for " + event.damage().getAmount(), NamedTextColor.GRAY));
        event.getPlayer().sendMessage(Component.text("    - Attacker " + Optional
            .ofNullable(event.damage().getAttacker())
            .map(Entity::getEntityType)
            .map(Object::toString).orElse("null"), NamedTextColor.DARK_GRAY));
        event.getPlayer().sendMessage(Component.text("    - Damage metadata " + event.damage().tagHandler().asCompound().toSNBT(), NamedTextColor.DARK_GRAY));
    }

    private void onDamageByGun(EntityDamageByGunEvent event) {
        if (!event.shooter().getTag(DEBUG_TAG) || !(event.shooter() instanceof Player player)) {
            return;
        }

        String username = player.getUsername();
        player.sendMessage(Component.text("[" + MinecraftServer.currentTick() +
            "] [" + username + "] Shot entity for " + event.damageAmount(), NamedTextColor.GRAY));
        player.sendMessage(Component.text("    - Equipment " + event.equipment().key(), NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("    - Instakill " + event.isInstakill(), NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("    - Headshot " + event.isHeadshot(), NamedTextColor.DARK_GRAY));
    }
}
