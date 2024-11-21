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

import java.util.Objects;

@Model("zombies.upgrade.validator.boolean_tag")
@Cache
public class BooleanTagValidator implements ValidatorComponent {
    private final Data data;

    @FactoryMethod
    public BooleanTagValidator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(Tag.Boolean(data.tag).defaultValue(false));
    }

    private record Internal(Tag<Boolean> tag) implements Validator {
        @Override
        public boolean test(@NotNull Entity entity, @NotNull PlayerUpgrade playerUpgrade,
            @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            return ZombiesTagUtils.sceneLocalTags(zombiesPlayer.getScene(), entity).getTag(tag);
        }
    }

    @DataObject
    public record Data(@NotNull String tag) {
    }
}
