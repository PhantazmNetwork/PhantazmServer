package org.phantazm.server.context;

import com.github.steanky.proxima.path.Pathfinder;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.loader.Loader;
import org.phantazm.mob2.MobCreator;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public record GameContext(@NotNull SongLoader songLoader,
    @NotNull Supplier<? extends Loader<MobCreator>> mobCreatorLoaderSupplier,
    @NotNull Supplier<? extends Map<Key, ExtensionHolder>> extensionMapSupplier,
    @NotNull Pathfinder pathfinder,
    @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSettingsFunction) {
}
