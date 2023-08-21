package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.state.CancellableState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.*;

@Model("mob.skill.attribute_modifying")
@Cache
public class ModifyAttributeSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public ModifyAttributeSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, mob, selector.apply(mob, injectionStore));
    }

    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        @NotNull String attribute,
        float amount,
        @NotNull AttributeOperation attributeOperation,
        @Nullable Key stage) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }

        @Default("stage")
        public static @NotNull ConfigElement defaultStage() {
            return ConfigPrimitive.NULL;
        }
    }

    private static final class Internal implements Skill {
        private final Data data;
        private final Mob self;
        private final UUID uuid;
        private final String uuidString;
        private final Attribute attribute;
        private final Selector selector;

        private final Set<Pair<LivingEntity, CancellableState<Entity>>> affectedEntities;

        private Internal(Data data, Mob self, Selector selector) {
            this.data = data;
            this.self = self;
            this.uuid = UUID.randomUUID();
            this.uuidString = this.uuid.toString();
            this.attribute = Attribute.fromKey(data.attribute);
            this.selector = selector;
            this.affectedEntities = Collections.newSetFromMap(new WeakHashMap<>());
        }

        private void removeFromEntity(LivingEntity entity, CancellableState<Entity> state) {
            if (data.stage != null && state != null) {
                entity.stateHolder().removeState(data.stage, state);
                return;
            }

            entity.getAttribute(attribute).removeModifier(uuid);
        }

        private void applyToEntity(LivingEntity entity) {
            if (data.stage != null) {
                CancellableState<Entity> state = CancellableState.state(entity, start -> {
                    ((LivingEntity) start).getAttribute(attribute).addModifier(new AttributeModifier(uuid, uuidString,
                        data.amount, data.attributeOperation));
                }, end -> {
                    ((LivingEntity) end).getAttribute(attribute).removeModifier(uuid);
                });

                entity.stateHolder().registerState(data.stage, state);

                affectedEntities.add(Pair.of(entity, state));
                return;
            }

            entity.getAttribute(attribute)
                .addModifier(new AttributeModifier(uuid, uuidString, data.amount, data.attributeOperation));
            affectedEntities.add(Pair.of(entity, null));
        }

        @Override
        public void use() {
            if (attribute == null) {
                return;
            }

            Target target = selector.select();
            self.getAcquirable().sync(ignored -> {
                target.forType(LivingEntity.class, this::applyToEntity);
            });
        }

        @Override
        public void end() {
            if (attribute == null) {
                return;
            }

            self.getAcquirable().sync(ignored -> {
                for (Pair<LivingEntity, CancellableState<Entity>> pair : affectedEntities) {
                    removeFromEntity(pair.first(), pair.second());
                }

                affectedEntities.clear();
            });
        }
    }
}
