package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

import java.util.ArrayList;
import java.util.List;

@Model("mob.selector.group")
@Cache
public class GroupSelector implements SelectorComponent {
    private final List<SelectorComponent> delegates;

    @FactoryMethod
    public GroupSelector(@NotNull @Child("delegates") List<SelectorComponent> delegates) {
        this.delegates = delegates;
    }

    @Override
    public @NotNull Selector apply(@NotNull ExtensionHolder holder) {
        List<Selector> delegates = new ArrayList<>(this.delegates.size());
        for (SelectorComponent selectorComponent : this.delegates) {
            delegates.add(selectorComponent.apply(holder));
        }

        return new Internal(delegates);
    }

    private record Internal(List<Selector> delegates) implements Selector {
        @Override
        public @NotNull Target select(@NotNull Mob mob) {
            List<Target.TargetEntry> entries = new ArrayList<>();
            for (Selector delegate : delegates) {
                entries.addAll(delegate.select(mob).entries());
            }

            return Target.entries(entries);
        }
    }
}
