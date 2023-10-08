package org.phantazm.zombies.modifier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.coin.ModifierSourceGroups;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.event.mob.ZombiesMobSetupEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Model("zombies.modifier.mob_attribute")
@Cache
public class MobAttributeModifier implements DualComponent<ZombiesScene, Modifier> {
    private final Data data;

    @FactoryMethod
    public MobAttributeModifier(@NotNull Data data) {
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
            Attribute attribute = Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL);
            if (attribute.equals(Attribute.MAX_HEALTH)) {
                scene.map().mapObjects().module().modifierSource().addModifier(ModifierSourceGroups.MOB_COIN_GAIN,
                    Transaction.modifier(Component.text("Health Modifier"),
                        convert(data.attributeOperation), -data.amount));
            }

            scene.addListener(ZombiesMobSetupEvent.class, event -> {
                LivingEntity entity = event.getEntity();


                entity.getAttribute(attribute)
                    .addModifier(new AttributeModifier(uuid, uuid.toString(), data.amount, data.attributeOperation));

                if (attribute.equals(Attribute.MAX_HEALTH)) {
                    entity.heal();
                }
            });
        }
    }

    private static Transaction.Modifier.Action convert(AttributeOperation operation) {
        return switch (operation) {
            case ADDITION -> Transaction.Modifier.Action.ADD;
            case MULTIPLY_BASE, MULTIPLY_TOTAL -> Transaction.Modifier.Action.MULTIPLY;
        };
    }

    @DataObject
    public record Data(@NotNull String attribute,
        double amount,
        @NotNull AttributeOperation attributeOperation) {
    }
}
