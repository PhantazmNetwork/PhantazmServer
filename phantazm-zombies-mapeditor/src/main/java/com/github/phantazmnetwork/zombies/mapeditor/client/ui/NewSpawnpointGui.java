package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.LogicUtils;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.map.SpawnpointInfo;
import com.github.phantazmnetwork.zombies.mapeditor.client.EditorSession;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

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

            Vec3I origin = currentMap.info().origin();

            currentMap.spawnpoints().add(new SpawnpointInfo(firstSelected.sub(origin), spawnruleKey));
            session.refreshSpawnpoints();
            ScreenUtils.closeCurrentScreen();
        });
    }
}
