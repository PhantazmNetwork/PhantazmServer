package org.phantazm.zombies.player.upgrade.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Model("zombies.upgrade.validator.stored_uuid")
@Cache
public class StoredUUIDValidator implements ValidatorComponent {
    private final Data data;

    @FactoryMethod
    public StoredUUIDValidator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(Tag.UUID(data.tag).list().defaultValue(List.of()));
    }

    private record Internal(Tag<List<UUID>> tag) implements Validator {
        @Override
        public boolean test(@NotNull Entity entity, @NotNull PlayerUpgrade playerUpgrade,
            @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            return ZombiesTagUtils.sceneLocalTags(zombiesPlayer).getTag(tag).contains(entity.getUuid());
        }
    }

    @DataObject
    public record Data(@NotNull String tag) {
    }
}
