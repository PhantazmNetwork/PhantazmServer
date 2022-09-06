package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.zombies.mapeditor.client.TextPredicates;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * A simple GUI for named objects. Provides a single text field and button to confirm object creation.
 */
public class NamedObjectGui extends SimplePanelGui {
    /**
     * The {@link WTextField} containing the name of the object to be created.
     */
    protected final WTextField textFieldName;

    /**
     * The {@link WButton} which will create and add the object when clicked.
     */
    protected final WButton buttonAdd;

    /**
     * The initial name. This is the initial text for the name {@link WTextField}. If null, the text field will be
     * empty. The initial name must conform to the predicate {@link TextPredicates#validKeyPredicate()}. If it does not,
     * the initial text will be empty.
     *
     * @param initialName the initial name
     */
    public NamedObjectGui(@Nullable String initialName) {
        super(100, 150);

        textFieldName = new WTextField();
        buttonAdd = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_ADD));

        textFieldName.setMaxLength(512);
        textFieldName.setTextPredicate(TextPredicates.validKeyPredicate());
        textFieldName.setText(initialName == null ? StringUtils.EMPTY : initialName);

        gridPanelRoot.add(textFieldName, 0, 0, 5, 1);
        gridPanelRoot.add(buttonAdd, 0, 2, 5, 1);
    }
}
