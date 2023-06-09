package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.UUID;

@Model("mob.skill.attribute_modifying")
@Cache(false)
public class AttributeModifyingSkill implements Skill {
    private final Data data;
    private final UUID uuid;
    private final String uuidString;
    private final Attribute attribute;

    @FactoryMethod
    public AttributeModifyingSkill(@NotNull Data data) {
        this.data = data;
        this.uuid = UUID.randomUUID();
        this.uuidString = this.uuid.toString();
        this.attribute = Attribute.fromKey(data.attribute);
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        if (attribute != null) {
            self.entity().getAttribute(attribute)
                    .addModifier(new AttributeModifier(uuid, uuidString, data.amount, data.attributeOperation));
        }

    }

    @Override
    public void end(@NotNull PhantazmMob self) {
        if (attribute != null) {
            self.entity().getAttribute(attribute).removeModifier(uuid);
        }
    }

    @DataObject
    public record Data(@NotNull String attribute, float amount, @NotNull AttributeOperation attributeOperation) {
    }
}
