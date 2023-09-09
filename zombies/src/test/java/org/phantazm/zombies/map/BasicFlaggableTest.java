package org.phantazm.zombies.map;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.commons.Namespaces;
import org.phantazm.commons.flag.BasicFlaggable;
import org.phantazm.commons.flag.Flaggable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicFlaggableTest {

    private static final Key TEST_FLAG = Key.key(Namespaces.PHANTAZM, "test_flag");

    private Flaggable flaggable;

    @BeforeEach
    public void setup() {
        flaggable = new BasicFlaggable();
    }

    @Test
    public void testHasFlagAfterFlagIsSet() {
        flaggable.setFlag(TEST_FLAG);

        assertTrue(flaggable.hasFlag(TEST_FLAG));
    }

    @Test
    public void testHasFlagAfterFlagIsToggled() {
        flaggable.toggleFlag(TEST_FLAG);

        assertTrue(flaggable.hasFlag(TEST_FLAG));
    }

    @Test
    public void testDoesNotHaveFlagAfterFlagIsSetAndThenCleared() {
        flaggable.setFlag(TEST_FLAG);
        flaggable.clearFlag(TEST_FLAG);

        assertFalse(flaggable.hasFlag(TEST_FLAG));
    }

    @Test
    public void testDoesNotHaveFlagAfterFlagIsSetAndThenToggled() {
        flaggable.setFlag(TEST_FLAG);
        flaggable.toggleFlag(TEST_FLAG);

        assertFalse(flaggable.hasFlag(TEST_FLAG));
    }

    @Test
    public void testDoesNotHaveFlagAfterFlagIsToggledAndThenCleared() {
        flaggable.toggleFlag(TEST_FLAG);
        flaggable.clearFlag(TEST_FLAG);

        assertFalse(flaggable.hasFlag(TEST_FLAG));
    }

    @Test
    public void testDoesNotHaveFlagAfterFlagIsToggledAndThenToggled() {
        flaggable.toggleFlag(TEST_FLAG);
        flaggable.toggleFlag(TEST_FLAG);

        assertFalse(flaggable.hasFlag(TEST_FLAG));
    }

}
