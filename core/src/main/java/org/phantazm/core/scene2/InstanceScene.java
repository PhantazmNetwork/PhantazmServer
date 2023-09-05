package org.phantazm.core.scene2;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Further specialization of {@link SceneAbstract} that assumes a single instance devoted to this {@link Scene}. The
 * scene will unregister the instance during shutdown. As with {@link SceneAbstract}, subclasses that override
 * {@link InstanceScene#shutdown()} should make sure to call {@code super.shutdown()}.
 */
public abstract class InstanceScene extends SceneAbstract {
    protected final Instance instance;

    public InstanceScene(@NotNull Instance instance) {
        this.instance = Objects.requireNonNull(instance);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        MinecraftServer.getInstanceManager().forceUnregisterInstance(instance);
    }

    /**
     * Gets the instance managed by this InstanceScene.
     *
     * @return the instace for this InstanceScene
     */
    public @NotNull Instance instance() {
        return instance;
    }
}
