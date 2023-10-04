package org.phantazm.zombies.modifier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.event.ZombiesMobSetupEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.UUID;

@Model("zombies.modifier.mob_attribute")
@Cache
public class MobAttributeModifier extends ModifierComponentBase {
    private final Data data;

    @FactoryMethod
    public MobAttributeModifier(@NotNull Data data) {
        super(data.key);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Modifier apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene scene) {
        return new Impl(data, scene);
    }

    private record Impl(Data data,
        ZombiesScene scene) implements Modifier {
        @Override
        public void apply() {
            UUID uuid = UUID.randomUUID();
            scene.addListener(ZombiesMobSetupEvent.class, event -> {
                LivingEntity entity = event.getEntity();

                Attribute attribute;
                entity.getAttribute(attribute = Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL))
                    .addModifier(new AttributeModifier(uuid, uuid.toString(), data.amount, data.attributeOperation));

                if (attribute.equals(Attribute.MAX_HEALTH)) {
                    entity.heal();
                }
            });
        }
    }

    @DataObject
    public record Data(@NotNull Key key,
        @NotNull String attribute,
        double amount,
        @NotNull AttributeOperation attributeOperation) {

    }
}
