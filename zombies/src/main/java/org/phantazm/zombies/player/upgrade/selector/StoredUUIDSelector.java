package org.phantazm.zombies.player.upgrade.selector;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import net.minestom.server.thread.Acquired;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;
import org.phantazm.zombies.player.upgrade.validator.Validator;
import org.phantazm.zombies.player.upgrade.validator.ValidatorComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Model("zombies.upgrade.selector.stored_uuid")
@Cache
public class StoredUUIDSelector implements SelectorComponent {
    private final Data data;
    private final ValidatorComponent validator;

    @FactoryMethod
    public StoredUUIDSelector(@NotNull Data data, @NotNull @Child("validator") ValidatorComponent validator) {
        this.data = data;
        this.validator = validator;
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, Tag.UUID(data.tag).list().defaultValue(List.of()),
            validator.apply(injectionStore, zombiesPlayer));
    }

    private record Internal(Data data,
        Tag<List<UUID>> tag,
        Validator validator) implements Selector {

        @Override
        public @NotNull Target select(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            List<UUID> uuids = ZombiesTagUtils.sceneLocalTags(zombiesPlayer).getTag(tag);
            if (uuids.isEmpty()) return Target.NONE;

            List<Entity> entities = new ArrayList<>(uuids.size());
            for (UUID uuid : uuids) {
                Entity entity = Entity.getEntity(uuid);
                if (entity == null) continue;

                Acquired<?> acquired = entity.getAcquirable().lock();
                try {
                    if (!entity.isActive() || entity.isRemoved() ||
                        entity.getInstance() != zombiesPlayer.getScene().instance()) continue;
                } finally {
                    acquired.unlock();
                }

                if (!validator.test(entity, upgrade, zombiesPlayer, triggerData)) continue;

                entities.add(entity);
            }

            return Target.entities(entities);
        }
    }

    @DataObject
    @Default("""
        {
          validator={type='zombies.upgrade.validator.always'}
        }
        """)
    public record Data(@NotNull String tag) {
    }
}
