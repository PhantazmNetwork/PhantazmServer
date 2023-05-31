package org.phantazm.zombies.mapeditor.client.ui;

import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3I;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.widget.WButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.map.WindowInfo;
import org.phantazm.zombies.mapeditor.client.EditorSession;
import org.phantazm.zombies.mapeditor.client.TranslationKeys;

import java.util.Objects;

/**
 * General UI for choosing what kind of object to create.
 */
public class NewObjectGui extends SimplePanelGui {
    /**
     * Constructs a new instance of this GUI, which allows the user to pick a kind of object they'd like to create.
     *
     * @param session the current {@link EditorSession}
     */
    public NewObjectGui(@NotNull EditorSession session) {
        super(100, 150);

        Objects.requireNonNull(session, "session");

        WButton newRoom = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_NEW_ROOM));
        WButton newDoor = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_NEW_DOOR));
        WButton newWindow = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_NEW_WINDOW));
        WButton newSpawnpoint = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_NEW_SPAWNPOINT));
        WButton newShop = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_NEW_SHOP));

        gridPanelRoot.add(newRoom, 0, 0, 5, 1);
        gridPanelRoot.add(newDoor, 0, 1, 5, 1);
        gridPanelRoot.add(newWindow, 0, 2, 5, 1);
        gridPanelRoot.add(newSpawnpoint, 0, 3, 5, 1);
        gridPanelRoot.add(newShop, 0, 4, 5, 1);

        MapInfo currentMap = session.getMap();
        Vec3I origin = currentMap.settings().origin();
        Bounds3I selected = session.getSelection();

        newRoom.setOnClick(
                () -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(new NewRoomGui(session))));
        newDoor.setOnClick(
                () -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(new NewDoorGui(session))));
        newWindow.setOnClick(() -> {
            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
            if (playerEntity == null) {
                return;
            }

            currentMap.windows().add(new WindowInfo(selected.shift(origin.mul(-1))));

            session.refreshWindows();
            ScreenUtils.closeCurrentScreen();
        });
        newSpawnpoint.setOnClick(
                () -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(new NewSpawnpointGui(session))));
        newShop.setOnClick(
                () -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(new NewShopGui(session))));
    }
}
