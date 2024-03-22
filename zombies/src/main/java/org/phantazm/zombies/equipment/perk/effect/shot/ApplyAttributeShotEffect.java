package org.phantazm.zombies.equipment.perk.effect.shot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.Tickable;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.TagUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Description("""
    An entity action that applies a temporary attribute modification to an entity when it is hit by the player's
    weapon shots.
    """)
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

        this.attribute = Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL);

        String name = NAMES.computeIfAbsent(data, ignored -> TagUtils.uniqueTagName());
        this.entities = new ConcurrentLinkedDeque<>();
        this.applyTicksTag = Tag.Long(name).defaultValue(-1L);
    }

    @Override
    public void perform(@NotNull Entity entity, @NotNull ZombiesPlayer zombiesPlayer) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        if (!(livingEntity instanceof Mob mob)) {
            return;
        }

        if (data.amount < 0 && attribute.equals(Attribute.MOVEMENT_SPEED) &&
            mob.data().extra().getBooleanOrDefault(ExtraNodeKeys.RESIST_SLOW_DOWN, false)) {
            return;
        }

        TagHandler tags = TagUtils.sceneLocalTags(entity, scene);
        long tag = tags.getAndUpdateTag(applyTicksTag, oldValue -> oldValue + 1);

        if (tag == 0) {
            livingEntity.getAttribute(attribute).addModifier(
                new AttributeModifier(attributeUUID, attributeName, data.amount, data.attributeOperation));
            entities.add(livingEntity);
        }
    }

    @Override
    public void tick(long time) {
        entities.removeIf(this::process);
    }

    private boolean process(LivingEntity livingEntity) {
        if (livingEntity.isRemoved() || livingEntity.isDead()) {
            removeAttribute(livingEntity);
            return true;
        }

        return TagUtils.sceneLocalTags(livingEntity, scene).getTag(applyTicksTag) >= data.duration;
    }

    private void removeAttribute(LivingEntity entity) {
        entity.getAttribute(attribute).removeModifier(attributeUUID);
        TagUtils.removeSceneLocalTag(entity, scene, applyTicksTag);
    }

    @DataObject
    public record Data(
        @NotNull @Description("The attribute to apply") String attribute,
        @Description("The attribute amount") double amount,
        @NotNull @Description("The attribute operation") AttributeOperation attributeOperation,
        @Description("The duration the effect will exist") int duration) {
    }
}
