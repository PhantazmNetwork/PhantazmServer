package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.Scene;
import org.phantazm.zombies.map.shop.PlayerInteraction;

@Model("zombies.map.shop.interactor.set_string_tag")
@Cache
public class SetStringTagInteractor extends InteractorBase<SetStringTagInteractor.Data> {
    private final Tag<String> tag;

    @FactoryMethod
    public SetStringTagInteractor(@NotNull Data data) {
        super(data);

        this.tag = Tag.String(data.tag);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Scene scene = interaction.player().getScene();
        TagHandler tagHandler = data.onScene ? scene.sceneTags() : scene.playerTags(interaction.player().getUUID());
        tagHandler.setTag(tag, data.value);

        return true;
    }

    @Default("""
        {
          onScene=true
        }
        """)
    @DataObject
    public record Data(@NotNull String tag,
        boolean onScene,
        @NotNull String value) {
    }
}
