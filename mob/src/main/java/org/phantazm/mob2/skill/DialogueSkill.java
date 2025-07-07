package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import it.unimi.dsi.fastutil.ints.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Model("mob.skill.dialogue")
@Cache
public class DialogueSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public DialogueSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull Skill get() {
        return new Internal(ExtensionHolder.requestKey(Extension.class), selector.get(), data);
    }

    @Default("""
        {
          trigger=null,
          allowedRepetitions=0
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger,
        double baseChance,
        int allowedRepetitions,
        @NotNull List<DialogueEntry> dialogue) {
    }

    private static final Comparator<DialogueEntry> WEIGHT_COMPARATOR =
        Comparator.comparingInt((DialogueEntry dialogueEntry) -> dialogueEntry.weight).reversed();

    public record DialogueEntry(@NotNull Component message,
        int weight) {
    }

    private static final class Extension {
        private final int maxLen;
        private final IntArrayList lastMessages;

        private Extension(int len) {
            this.maxLen = len;
            this.lastMessages = new IntArrayList(len);
        }
    }

    private static final class Internal implements Skill {
        private final ExtensionHolder.Key<Extension> key;
        private final Selector selector;
        private final Data data;
        private final List<DialogueEntry> dialogue;
        private final int cap;

        private Internal(ExtensionHolder.Key<Extension> key, Selector selector, Data data) {
            this.key = key;
            this.selector = selector;
            this.data = data;

            List<DialogueEntry> dialogue = new ArrayList<>(data.dialogue);
            dialogue.sort(WEIGHT_COMPARATOR);

            int cap = 0;
            for (DialogueEntry entry : dialogue) cap += entry.weight;

            this.cap = cap;
            this.dialogue = dialogue;
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }

        @Override
        public void init(@NotNull Mob mob) {
            int len;

            // if negative: repetition checking disabled
            // if 0: (dialogue length - 1) repetitions allowed
            // if positive: repetition length is the minimum of allowedRepetitions and (dialogue length - 1)
            if (data.allowedRepetitions < 0) len = -1;
            else if (data.allowedRepetitions == 0) len = data.dialogue.size() - 1;
            else len = Math.min(data.dialogue.size() - 1, data.allowedRepetitions);

            mob.extensions().set(key, new Extension(len));
        }

        @Override
        public void use(@NotNull Mob mob) {
            if (dialogue.isEmpty() || Math.random() > data.baseChance) return;

            selector.select(mob).forType(Player.class, player -> handlePlayer(player, mob));
        }

        private void handlePlayer(Player player, Mob mob) {
            int size = dialogue.size();

            // simple case, there is only one message
            if (size == 1) {
                player.sendMessage(dialogue.get(0).message);
                return;
            }

            int index = rollDialog(mob.extensions().get(key));
            player.sendMessage(dialogue.get(index).message);
        }

        // should only be called when dialogue.size() > 1
        private int rollDialog(Extension ext) {
            int roll = (int) Math.rint(Math.random() * cap);
            int newCap = cap;

            boolean repetitionCheckEnabled = ext.maxLen > 0;

            if (repetitionCheckEnabled) {
                IntIterator choiceIterator = ext.lastMessages.intIterator();
                while (choiceIterator.hasNext()) newCap -= dialogue.get(choiceIterator.nextInt()).weight;
            }

            int adjustedRoll = roll % newCap;

            int sum = 0;
            for (int i = 0; i < dialogue.size(); i++) {
                if (repetitionCheckEnabled && ext.lastMessages.contains(i)) continue;

                DialogueEntry entry = this.dialogue.get(i);
                sum += entry.weight;

                if (adjustedRoll <= sum) {
                    if (repetitionCheckEnabled) {
                        if (!ext.lastMessages.isEmpty() && ext.lastMessages.size() >= ext.maxLen)
                            ext.lastMessages.removeInt(ext.lastMessages.size() - 1);
                        ext.lastMessages.add(0, i);
                    }

                    return i;
                }
            }

            // this shouldn't be necessary unless there's a bug or a problem in config.
            // the above loop should always find at least one match
            if (repetitionCheckEnabled) {
                for (int i = 0; i < dialogue.size(); i++) {
                    if (ext.lastMessages.contains(i)) continue;

                    return i;
                }
            }

            return 0;
        }
    }
}
