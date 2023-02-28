package org.phantazm.zombies.equipment.perk.effect.shot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.map.action.Action;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Description("An entity action that applies a temporary attribute modification to an entity.")
@Model("zombies.perk.effect.shot_entity.apply_attribute")
@Cache(false)
public class ApplyAttributeShotEffect implements Action<Entity>, Tickable {
    private static final Map<Data, String> NAMES = new ConcurrentHashMap<>();

    private final Data data;
    private final UUID attributeUUID;
    private final String attributeName;

    private final Attribute attribute;

    private final Deque<LivingEntity> entities;
    private final Tag<Long> applyTime;

    @FactoryMethod
    public ApplyAttributeShotEffect(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
        this.attributeUUID = UUID.randomUUID();
        this.attributeName = this.attributeUUID.toString();

        this.attribute = Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL);

        String name = NAMES.computeIfAbsent(data, ignored -> UUID.randomUUID().toString());
        this.entities = new ConcurrentLinkedDeque<>();
        this.applyTime = Tag.Long(name).defaultValue(-1L);
    }

    @Override
    public void perform(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        long tag = livingEntity.getTag(applyTime);
        livingEntity.setTag(applyTime, System.currentTimeMillis());

        if (tag == -1) {
            livingEntity.getAttribute(attribute).addModifier(
                    new AttributeModifier(attributeUUID, attributeName, data.amount, data.attributeOperation));
            entities.add(livingEntity);
        }
    }

    @Override
    public void tick(long time) {
        entities.removeIf(entity -> process(entity, time));
    }

    private boolean process(LivingEntity livingEntity, long time) {
        if (livingEntity.isRemoved() || livingEntity.isDead() ||
                ((time - livingEntity.getTag(applyTime)) / MinecraftServer.TICK_MS) >= data.duration) {
            removeAttribute(livingEntity);
            return true;
        }

        return false;
    }

    private void removeAttribute(LivingEntity entity) {
        entity.getAttribute(attribute).removeModifier(attributeUUID);
        entity.removeTag(applyTime);
    }

    @DataObject
    public record Data(@NotNull @Description("The attribute to apply") String attribute,
                       @Description("The attribute amount") double amount,
                       @NotNull @Description("The attribute operation") AttributeOperation attributeOperation,
                       @Description("The duration the effect will exist") int duration) {
    }
}
