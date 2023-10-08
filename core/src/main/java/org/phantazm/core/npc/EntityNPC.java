package org.phantazm.core.npc;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.MonoComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.npc.interactor.NPCInteractor;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Model("npc.entity")
@Cache
public class EntityNPC implements MonoComponent<NPC> {
    private final Data data;
    private final MonoComponent<Supplier<Entity>> entity;
    private final MonoComponent<Consumer<Entity>> settings;
    private final MonoComponent<EntityTicker> ticker;
    private final MonoComponent<NPCInteractor> interactor;

    @FactoryMethod
    public EntityNPC(@NotNull Data data, @NotNull @Child("entity") MonoComponent<Supplier<Entity>> entity,
        @NotNull @Child("settings") MonoComponent<Consumer<Entity>> settings,
        @NotNull @Child("ticker") MonoComponent<EntityTicker> ticker,
        @NotNull @Child("interactor") MonoComponent<NPCInteractor> interactor) {
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
        private final NPCInteractor interactor;

        private Entity npc;

        private Internal(Data data, Supplier<Entity> entity, Consumer<Entity> settings, EntityTicker ticker,
            NPCInteractor interactor) {
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
