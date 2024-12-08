package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.List;
import java.util.UUID;

@Model("zombies.upgrade.filter.condition.stored_uuid_alive")
@Cache
public class StoredUUIDAliveCondition implements EventConditionComponent {
    private final Data data;
    private final Tag<List<UUID>> tag;

    @FactoryMethod
    public StoredUUIDAliveCondition(@NotNull Data data) {
        this.data = data;
        this.tag = Tag.UUID(data.tag).list().defaultValue(List.of());
    }

    @Override
    public @NotNull EventCondition<?> apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, tag, zombiesPlayer);
    }

    private static class Internal implements EventCondition<Event> {
        private final Data data;
        private final Tag<List<UUID>> tag;
        private final ZombiesPlayer zombiesPlayer;

        private Internal(Data data, Tag<List<UUID>> tag, ZombiesPlayer zombiesPlayer) {
            this.data = data;
            this.tag = tag;
            this.zombiesPlayer = zombiesPlayer;
        }

        @Override
        public @NotNull Class<Event> eventType() {
            return Event.class;
        }

        @Override
        public boolean filter(@NotNull Event event) {
            List<UUID> uuids = ZombiesTagUtils.sceneLocalTags(zombiesPlayer).getTag(tag);
            if (uuids.isEmpty()) return false;

            for (UUID uuid : uuids) {
                Entity entity = Entity.getEntity(uuid);
                boolean isDead = entity == null || entity.isRemoved() || !entity.isActive();

                if (isDead && data.allMustMatch) return false;
                else if (!isDead && !data.allMustMatch) return true;
            }

            return data.allMustMatch;
        }
    }

    @Default("""
        {
          allMustMatch=true
        }
        """)
    @DataObject
    public record Data(@NotNull String tag,
        boolean allMustMatch) {
    }
}
