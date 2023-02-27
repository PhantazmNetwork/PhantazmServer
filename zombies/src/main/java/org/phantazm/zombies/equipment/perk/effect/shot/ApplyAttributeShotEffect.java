package org.phantazm.zombies.equipment.perk.effect.shot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.map.action.Action;

import java.util.Objects;
import java.util.UUID;

@Description("An entity action that applies a temporary attribute modification to an entity.")
@Model("zombies.perk.effect.shot_entity.apply_attribute")
@Cache(false)
public class ApplyAttributeShotEffect implements Action<Entity> {
    private final Data data;
    private final UUID attributeUUID;
    private final String attributeName;

    @FactoryMethod
    public ApplyAttributeShotEffect(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
        this.attributeUUID = UUID.randomUUID();
        this.attributeName = this.attributeUUID.toString();
    }

    @Override
    public void perform(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        Attribute attribute = Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL);
        AttributeInstance instance = livingEntity.getAttribute(attribute);

        AttributeModifier modifier =
                new AttributeModifier(attributeUUID, attributeName, data.amount, data.attributeOperation);
        instance.addModifier(modifier);

        if (instance.getModifiers().contains(modifier)) {
            livingEntity.scheduler()
                    .scheduleTask(() -> instance.removeModifier(attributeUUID), TaskSchedule.tick(data.duration),
                            TaskSchedule.stop());
        }
    }

    @DataObject
    public record Data(@NotNull @Description("The attribute to apply") String attribute,
                       @Description("The attribute amount") double amount,
                       @NotNull @Description("The attribute operation") AttributeOperation attributeOperation,
                       @Description("The duration the effect will exist") int duration) {
    }
}
