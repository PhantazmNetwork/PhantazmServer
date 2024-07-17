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
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.TagUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.event.player.ZombiesPlayerModifyAttributeEvent;
import org.phantazm.zombies.event.mob.MobAttributeWearOffEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Model("zombies.perk.effect.shot_entity.apply_attribute")
@Cache(false)
public class ApplyAttributeShotEffect implements ShotEffect, Tickable {
    private static final Map<Data, String> NAMES = new ConcurrentHashMap<>();

    private final Data data;
    private final ZombiesScene scene;
    private final UUID attributeUUID;
    private final String attributeName;

    private final Attribute attribute;

    private final Deque<LivingEntity> entities;
    private final Tag<Long> applyTicksTag;

    @FactoryMethod
    public ApplyAttributeShotEffect(@NotNull Data data, @NotNull ZombiesScene scene) {
        this.data = data;
        this.scene = scene;
        this.attributeUUID = UUID.randomUUID();
        this.attributeName = this.attributeUUID.toString();

        this.attribute = Objects.requireNonNullElse(Attributes.get(data.attribute), Attributes.NIL);

        String name = NAMES.computeIfAbsent(data, ignored -> TagUtils.uniqueTagName());
        this.entities = new ConcurrentLinkedDeque<>();
        this.applyTicksTag = Tag.Long(name).defaultValue(0L);
    }

    @Override
    public void perform(@NotNull Entity entity, @NotNull ZombiesPlayer zombiesPlayer) {
        if (!(entity instanceof Mob mob)) {
            return;
        }

        if (data.amount < 0 && attribute.equals(Attribute.MOVEMENT_SPEED) &&
            mob.data().extra().getBooleanOrDefault(ExtraNodeKeys.RESIST_SLOW_DOWN, false)) {
            return;
        }

        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return;
        }

        scene.broadcastCancellable(new ZombiesPlayerModifyAttributeEvent(playerOptional.get(), zombiesPlayer,
            this, mob, attribute, attributeUUID, (float) data.amount), event -> {
            mob.getAttribute(attribute).addModifier(
                new AttributeModifier(attributeUUID, attributeName, event.attributeAmount(), data.attributeOperation));
            entities.add(mob);
        });

    }

    @Override
    public void tick(long time) {
        entities.removeIf(this::process);
    }

    private boolean process(LivingEntity livingEntity) {
        if (livingEntity.isRemoved() || livingEntity.isDead() || TagUtils.sceneLocalTags(livingEntity, scene)
            .getAndUpdateTag(applyTicksTag, tick -> tick + 1) >= data.duration) {
            removeAttribute(livingEntity);
            return true;
        }

        return false;
    }

    private void removeAttribute(LivingEntity entity) {
        AttributeModifier modifier = entity.getAttribute(attribute).removeModifier(attributeUUID);
        TagUtils.removeSceneLocalTag(entity, scene, applyTicksTag);

        if (modifier != null) {
            scene.broadcastEvent(new MobAttributeWearOffEvent(this, entity, attribute, modifier));
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
