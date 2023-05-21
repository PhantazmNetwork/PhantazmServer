package org.phantazm.zombies.mapeditor.client.ui;

import com.github.steanky.vector.Vec3I;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import net.kyori.adventure.key.Key;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.LogicUtils;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.map.SpawnpointInfo;
import org.phantazm.zombies.mapeditor.client.EditorSession;
import org.phantazm.zombies.mapeditor.client.TranslationKeys;

import java.util.Objects;

/**
 * General UI for creating new {@link SpawnpointInfo} instances.
 */
public class NewSpawnpointGui extends NamedObjectGui {
    /**
     * Toggles whether the spawnpoint will link to a specific window.
     */
    protected final WToggleButton toggleButtonLinkWindow;

    protected final WLabel linkDisplayLabel;

    /**
     * Constructs a new instance of this GUI, which allows a user to create spawnpoints.
     *
     * @param session the current {@link EditorSession}
     */
    @SuppressWarnings("PatternValidation")
    public NewSpawnpointGui(@NotNull EditorSession session) {
        super(LogicUtils.nullCoalesce(session.lastSpawnrule(), Key::value));

        Objects.requireNonNull(session, "session");

        super.gridPanelRoot.setSize(200, 120);

        this.toggleButtonLinkWindow = new WToggleButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_LINK_WINDOW));
        this.toggleButtonLinkWindow.setToggle(true);

        MapInfo currentMap = session.getMap();
        Vec3I firstSelected = session.getFirstSelection();

        this.linkDisplayLabel = new WLabel(Text.of(StringUtils.EMPTY));

        gridPanelRoot.add(toggleButtonLinkWindow, 0, 3, 10, 1);
        gridPanelRoot.add(linkDisplayLabel, 0, 4, 10, 1);

        Vec3I mapOrigin = session.getMap().settings().origin();

        Vec3I linkTarget;
        if (session.getSelection().volume() != 1) {
            linkTarget = session.getSecondSelection().sub(mapOrigin);
        }
        else {
            linkTarget = null;
        }


        toggleButtonLinkWindow.setOnToggle(state -> {
            if (state) {
                if (linkTarget != null) {
                    this.linkDisplayLabel.setText(
                            Text.translatable(TranslationKeys.GUI_MAPEDITOR_EXPLICIT_WINDOW_LINK, linkTarget.x(),
                                    linkTarget.y(), linkTarget.z()));
                }
                else {
                    this.linkDisplayLabel.setText(Text.translatable(TranslationKeys.GUI_MAPEDITOR_NEAREST_WINDOW_LINK));
                }
            }
            else {
                this.linkDisplayLabel.setText(Text.of(StringUtils.EMPTY));
            }
        });
        Objects.requireNonNull(toggleButtonLinkWindow.getOnToggle()).accept(true);

        buttonAdd.setOnClick(() -> {
            String value = textFieldName.getText();
            if (value.isEmpty()) {
                return;
            }

            Key spawnruleKey = Key.key(Namespaces.PHANTAZM, value);
            session.setLastSpawnrule(spawnruleKey);

            Vec3I origin = currentMap.settings().origin();
            boolean linkToWindow = toggleButtonLinkWindow.getToggle();

            currentMap.spawnpoints()
                    .add(new SpawnpointInfo(firstSelected.sub(origin), spawnruleKey, linkToWindow, linkTarget));
            session.refreshSpawnpoints();
            ScreenUtils.closeCurrentScreen();
        });
    }

    @Override
    protected void addToPanel() {
        gridPanelRoot.add(textFieldName, 0, 0, 10, 1);
        gridPanelRoot.add(buttonAdd, 0, 2, 10, 1);
    }
}
