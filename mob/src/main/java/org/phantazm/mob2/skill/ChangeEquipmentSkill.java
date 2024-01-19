package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Map;

@Model("mob.skill.change_equipment")
@Cache
public class ChangeEquipmentSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public ChangeEquipmentSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Impl(mob, selector.apply(mob, injectionStore), data);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
        @NotNull Map<EquipmentSlot, ItemStack> equipment) {
    }

    private static class Impl extends TargetedSkill {
        private final Data data;

        private Impl(Mob self, Selector selector, Data data) {
            super(self, selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            target.forType(LivingEntity.class, entity -> {
                for (Map.Entry<EquipmentSlot, ItemStack> entry : data.equipment.entrySet()) {
                    entity.setEquipment(entry.getKey(), entry.getValue());
                }
            });
        }
    }
}
