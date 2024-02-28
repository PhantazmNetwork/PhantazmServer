package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
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
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(selector.apply(holder), data);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
        @NotNull Map<EquipmentSlot, ItemStack> equipment) {
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        private Internal(Selector selector, Data data) {
            super(selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            target.forType(EquipmentHandler.class, entity -> {
                for (Map.Entry<EquipmentSlot, ItemStack> entry : data.equipment.entrySet()) {
                    entity.setEquipment(entry.getKey(), entry.getValue());
                }
            });
        }
    }
}
