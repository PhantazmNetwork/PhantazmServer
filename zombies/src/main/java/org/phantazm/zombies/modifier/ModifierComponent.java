package org.phantazm.zombies.modifier;

import net.kyori.adventure.key.Keyed;
import org.phantazm.commons.DualComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

public interface ModifierComponent extends DualComponent<ZombiesScene, Modifier>, Keyed {
}
