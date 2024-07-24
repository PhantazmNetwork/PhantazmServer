package org.phantazm.zombies.equipment.perk.effect.shot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.Tickable;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.event.player.ZombiesPlayerModifyAttributeEvent;
import org.phantazm.zombies.event.entity.EntityAttributeModifierRemoveEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

@Model("zombies.perk.effect.shot_entity.apply_attribute")
@Cache(false)
public class ApplyAttributeShotEffect implements ShotEffect, Tickable {
    private final Data data;
    private final ZombiesScene scene;
    private final UUID attributeUUID;
    private final String attributeName;

    private final Attribute attribute;

    private final Deque<Entry> entities;

    private record Entry(Reference<LivingEntity> target,
        AtomicInteger timer) {
    }

    @FactoryMethod
    public ApplyAttributeShotEffect(@NotNull Data data, @NotNull ZombiesScene scene) {
        this.data = data;
        this.scene = scene;
        this.attributeUUID = UUID.randomUUID();
        this.attributeName = this.attributeUUID.toString();

        this.attribute = Objects.requireNonNullElse(Attributes.get(data.attribute), Attributes.NIL);

        this.entities = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void perform(@NotNull Entity entity, @NotNull ZombiesPlayer zombiesPlayer, double scale) {
        if (!(entity instanceof Mob mob)) {
            return;
        }

        if (data.amount < 0 && attribute.equals(Attribute.MOVEMENT_SPEED) &&
            mob.hasProperty(ExtraNodeKeys.RESIST_SLOW_DOWN)) {
            return;
        }

        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return;
        }

        scene.broadcastCancellable(new ZombiesPlayerModifyAttributeEvent(playerOptional.get(), zombiesPlayer,
            this, mob, attribute, attributeUUID, (float) data.amount), event -> {
            mob.getAttribute(attribute).addModifier(
                new AttributeModifier(attributeUUID, attributeName, event.attributeAmount() * scale,
                    data.attributeOperation));
            entities.add(new Entry(new WeakReference<>(mob), new AtomicInteger((int) Math.round(data.duration * scale))));
        });

    }

    @Override
    public void tick(long time) {
        entities.removeIf(this::process);
    }

    private boolean process(Entry entry) {
        LivingEntity livingEntity = entry.target.get();
        if (livingEntity == null) {
            return true;
        }

        if (livingEntity.isRemoved() || livingEntity.isDead() || entry.timer.getAndDecrement() <= 0) {
            removeAttribute(livingEntity);
            return true;
        }

        return false;
    }

    private void removeAttribute(LivingEntity entity) {
        AttributeModifier modifier = entity.getAttribute(attribute).removeModifier(attributeUUID);

        if (modifier != null) {
            scene.broadcastEvent(new EntityAttributeModifierRemoveEvent(entity, attribute, modifier));
        }
    }

    @DataObject
    public record Data(
        String attribute,
        double amount,
        AttributeOperation attributeOperation,
        int duration) {
    }
}
