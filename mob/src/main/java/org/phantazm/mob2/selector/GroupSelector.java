package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

import java.util.ArrayList;
import java.util.List;

@Model("mob.selector.group")
@Cache
public class GroupSelector implements SelectorComponent {
    private final List<SelectorComponent> delegates;

    @FactoryMethod
    public GroupSelector(@NotNull List<SelectorComponent> delegates) {
        this.delegates = delegates;
    }

    @Override
    public @NotNull Selector apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        List<Selector> delegates = new ArrayList<>(this.delegates.size());
        for (SelectorComponent selectorComponent : this.delegates) {
            delegates.add(selectorComponent.apply(mob, injectionStore));
        }

        return new Internal(delegates);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("delegates") List<String> delegates) {
    }

    private record Internal(List<Selector> delegates) implements Selector {
        @Override
        public @NotNull Target select() {
            List<Target.TargetEntry> entries = new ArrayList<>();
            for (Selector delegate : delegates) {
                entries.addAll(delegate.select().entries());
            }

            return Target.entries(entries);
        }
    }
}
