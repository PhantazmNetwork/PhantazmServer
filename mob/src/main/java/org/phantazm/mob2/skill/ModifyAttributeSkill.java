package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
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
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Model("mob.skill.attribute_modifying")
@Cache
public class ModifyAttributeSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    private static class Extension {
        private final List<Pair<LivingEntity, CancellableState<Entity>>> affectedEntities = new ArrayList<>();
    }

    @FactoryMethod
    public ModifyAttributeSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, holder.requestKey(Extension.class), selector.apply(holder));
    }

    @Default("""
        {
          trigger=null,
          stage=null
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull String attribute,
        float amount,
        @NotNull AttributeOperation attributeOperation,
        @Nullable Key stage) {
    }

    private static final class Internal implements Skill {
        private final Data data;
        private final ExtensionHolder.Key<Extension> key;
        private final UUID uuid;
        private final String uuidString;
        private final Attribute attribute;
        private final Selector selector;

        private Internal(Data data, ExtensionHolder.Key<Extension> key, Selector selector) {
            this.data = data;
            this.key = key;
            this.uuid = UUID.randomUUID();
            this.uuidString = this.uuid.toString();
            this.attribute = Attribute.fromKey(data.attribute);
            this.selector = selector;
        }

        private void removeFromEntity(LivingEntity entity, CancellableState<Entity> state) {
            if (data.stage != null && state != null) {
                entity.stateHolder().removeState(data.stage, state);
                return;
            }

            entity.getAttribute(attribute).removeModifier(uuid);
        }

        private void applyToEntity(Mob self, LivingEntity entity) {
            Extension ext = self.extensions().get(key);

            if (data.stage != null && !(entity instanceof Mob mob && !mob.useStateHolder())) {
                CancellableState<Entity> state = CancellableState.state(entity, start -> {
                    ((LivingEntity) start).getAttribute(attribute).addModifier(new AttributeModifier(uuid, uuidString,
                        data.amount, data.attributeOperation));
                }, end -> {
                    ((LivingEntity) end).getAttribute(attribute).removeModifier(uuid);
                });

                entity.stateHolder().registerState(data.stage, state);
                ext.affectedEntities.add(Pair.of(entity, state));
                return;
            }

            entity.getAttribute(attribute)
                .addModifier(new AttributeModifier(uuid, uuidString, data.amount, data.attributeOperation));
            ext.affectedEntities.add(Pair.of(entity, null));
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }

        @Override
        public void init(@NotNull Mob mob) {
            mob.extensions().set(key, new Extension());
        }

        @Override
        public void use(@NotNull Mob mob) {
            if (attribute == null) {
                return;
            }

            Target target = selector.select(mob);
            mob.getAcquirable().sync(ignored -> {
                target.forType(LivingEntity.class, targetEntity -> {
                    applyToEntity(mob, targetEntity);
                });
            });
        }

        @Override
        public void end(@NotNull Mob mob) {
            if (attribute == null) {
                return;
            }

            Extension ext = mob.extensions().get(key);
            mob.getAcquirable().sync(ignored -> {
                for (Pair<LivingEntity, CancellableState<Entity>> pair : ext.affectedEntities) {
                    removeFromEntity(pair.first(), pair.second());
                }

                ext.affectedEntities.clear();
            });
        }
    }
}
