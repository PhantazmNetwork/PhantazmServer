package org.phantazm.core.npc;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.npc.join.Interactor;

import java.util.UUID;

@Model("npc.entity")
@Cache(false)
public class EntityJoinNPC implements NPC {
    private final Data data;
    private final Interactor interactor;

    private Entity npc;

    @FactoryMethod
    public EntityJoinNPC(@NotNull Data data, @NotNull @Child("interactor") Interactor interactor) {
        this.data = data;
        this.interactor = interactor;
    }

    @Override
    public void handleInteraction(@NotNull Player interactor) {
        this.interactor.join(interactor);
    }

    @Override
    public void spawn(@NotNull Instance instance) {
        Entity npc = this.npc;
        if (npc != null) {
            npc.remove();
        }

        npc = new Entity(data.entityType);

        if (!data.displayName.equals(Component.empty())) {
            npc.getEntityMeta().setCustomName(data.displayName);
            npc.getEntityMeta().setCustomNameVisible(true);
        }

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
    public @Nullable UUID entityUUID() {
        Entity npc = this.npc;
        return npc == null ? null : npc.getUuid();
    }

    @DataObject
    public record Data(@NotNull EntityType entityType,
                       @NotNull Pos location,
                       @NotNull @ChildPath("interactor") String interactor,
                       @NotNull Component displayName) {
        @Default("displayName")
        public static @NotNull ConfigElement displayNameDefault() {
            return ConfigPrimitive.of("");
        }
    }
}
