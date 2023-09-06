package org.phantazm.core.npc;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.BasicComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.npc.interactor.Interactor;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Model("npc.entity")
@Cache
public class EntityNPC implements BasicComponent<NPC> {
    private final Data data;
    private final BasicComponent<Supplier<Entity>> entity;
    private final BasicComponent<Consumer<Entity>> settings;
    private final BasicComponent<EntityTicker> ticker;
    private final BasicComponent<Interactor> interactor;

    @FactoryMethod
    public EntityNPC(@NotNull Data data, @NotNull @Child("entity") BasicComponent<Supplier<Entity>> entity,
        @NotNull @Child("settings") BasicComponent<Consumer<Entity>> settings,
        @NotNull @Child("ticker") BasicComponent<EntityTicker> ticker,
        @NotNull @Child("interactor") BasicComponent<Interactor> interactor) {
        this.data = data;
        this.entity = entity;
        this.settings = settings;
        this.ticker = ticker;
        this.interactor = interactor;
    }

    @Override
    public NPC apply(@NotNull InjectionStore injectionStore) {
        return new Internal(data, entity.apply(injectionStore), settings.apply(injectionStore),
            ticker.apply(injectionStore), interactor.apply(injectionStore));
    }

    private static final class Internal implements NPC {
        private final Data data;
        private final Supplier<? extends Entity> entity;
        private final Consumer<? super Entity> settings;
        private final EntityTicker ticker;
        private final Interactor interactor;

        private Entity npc;

        private Internal(Data data, Supplier<Entity> entity, Consumer<Entity> settings, EntityTicker ticker,
            Interactor interactor) {
            this.data = data;
            this.entity = entity;
            this.settings = settings;
            this.ticker = ticker;
            this.interactor = interactor;
        }

        @Override
        public void handleInteraction(@NotNull Player interactor) {
            this.interactor.interact(interactor);
        }

        @Override
        public void spawn(@NotNull Instance instance) {
            Entity npc = this.npc;
            if (npc != null) {
                npc.remove();
            }

            npc = entity.get();
            settings.accept(npc);

            npc.setInstance(instance, data.location).join();
            this.npc = npc;
        }

        @Override
        public void despawn() {
            Entity npc = this.npc;
            if (npc != null) {
                npc.remove();
            }
        }

        @Override
        public @Nullable UUID uuid() {
            Entity npc = this.npc;
            return npc == null ? null : npc.getUuid();
        }

        @Override
        public void tick(long time) {
            Entity npc = this.npc;
            if (npc != null) {
                ticker.accept(time, npc);
            }
        }

    }

    @DataObject
    public record Data(
        @NotNull Pos location,
        @NotNull @ChildPath("entity") String entity,
        @NotNull @ChildPath("settings") String settings,
        @NotNull @ChildPath("ticker") String ticker,
        @NotNull @ChildPath("interactor") String interactor) {
    }
}
