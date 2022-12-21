package org.phantazm.zombies.mapeditor.client.ui;

import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.LogicUtils;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.map.SpawnpointInfo;
import org.phantazm.zombies.mapeditor.client.EditorSession;

import java.util.Objects;

/**
 * General UI for creating new {@link SpawnpointInfo} instances.
 */
public class NewSpawnpointGui extends NamedObjectGui {
    /**
     * Constructs a new instance of this GUI, which allows a user to create spawnpoints.
     *
     * @param session the current {@link EditorSession}
     */
    @SuppressWarnings("PatternValidation")
    public NewSpawnpointGui(@NotNull EditorSession session) {
        super(LogicUtils.nullCoalesce(session.lastSpawnrule(), Key::value));

        Objects.requireNonNull(session, "session");

        MapInfo currentMap = session.getMap();
        Vec3I firstSelected = session.getFirstSelection();
        buttonAdd.setOnClick(() -> {
            String value = textFieldName.getText();
            if (value.isEmpty()) {
                return;
            }

            Key spawnruleKey = Key.key(Namespaces.PHANTAZM, value);
            session.setLastSpawnrule(spawnruleKey);

            Vec3I origin = currentMap.settings().origin();

            currentMap.spawnpoints().add(new SpawnpointInfo(firstSelected.sub(origin), spawnruleKey));
            session.refreshSpawnpoints();
            ScreenUtils.closeCurrentScreen();
        });
    }
}
