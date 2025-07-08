package org.phantazm.zombies.map.shop.predicate;

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
import org.phantazm.zombies.map.shop.Shop;

import java.util.Objects;

@Model("zombies.map.shop.predicate.string_tag")
@Cache
public class StringTagPredicate extends PredicateBase<StringTagPredicate.Data> {
    private final Tag<String> tag;

    @FactoryMethod
    public StringTagPredicate(@NotNull Data data) {
        super(data);

        this.tag = Tag.String(data.tag);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        Scene scene = interaction.player().getScene();
        TagHandler tagHandler = data.onScene ? scene.sceneTags() : scene.playerTags(interaction.player().getUUID());

        return Objects.equals(tagHandler.getTag(tag), data.value);
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
