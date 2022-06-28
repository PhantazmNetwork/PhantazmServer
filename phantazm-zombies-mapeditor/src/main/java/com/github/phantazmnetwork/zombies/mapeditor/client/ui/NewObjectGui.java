package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.WindowInfo;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.mapeditor.client.MapeditorSession;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NewObjectGui extends LightweightGuiDescription {
    public NewObjectGui(@NotNull MapeditorSession session) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(100, 150);
        root.setInsets(Insets.ROOT_PANEL);

        WButton newRoom = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_ROOM));
        WButton newDoor = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_DOOR));
        WButton newWindow = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_WINDOW));
        WButton newSpawnpoint = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_SPAWNPOINT));
        WButton newShop = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_SHOP));
        WLabel feedback = new WLabel(new LiteralText(StringUtils.EMPTY));

        root.add(newRoom, 0, 0, 5, 1);
        root.add(newDoor, 0, 1, 5, 1);
        root.add(newWindow, 0, 2, 5, 1);
        root.add(newSpawnpoint, 0, 3, 5, 1);
        root.add(newShop, 0, 4, 5, 1);

        root.add(feedback, 0, 5);

        newRoom.setOnClick(() -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(new NewRoomGui(session))));
        newDoor.setOnClick(() -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(new NewDoorGui(session))));
        newWindow.setOnClick(() -> {
            ZombiesMap currentMap = session.getMap();
            Vec3I origin = currentMap.info().origin();
            Region3I selected = session.getSelection();

            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
            if(playerEntity == null) {
                return;
            }

            List<String> blockData = new ArrayList<>(selected.volume());
            for(Vec3I position : selected) {
                //convert to world coordinate space
                BlockState state = playerEntity.world.getBlockState(new BlockPos.Mutable(position.getX() + origin
                        .getX(), position.getY() + origin.getY(), position.getZ() + origin.getZ()));
                blockData.add(NbtHelper.fromBlockState(state).toString());
            }

            currentMap.windows().add(new WindowInfo(selected, blockData));
            session.refreshWindows();
            ScreenUtils.closeCurrentScreen();
        });
        newSpawnpoint.setOnClick(() -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(
                new NewSpawnpointGui(session))));
        newShop.setOnClick(() -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(new NewShopGui(session))));
    }
}
