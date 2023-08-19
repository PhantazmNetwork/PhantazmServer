package org.phantazm.zombies.sidebar;

import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.zombies.sidebar.lineupdater.ConstantSidebarLineUpdater;
import org.phantazm.zombies.sidebar.lineupdater.SidebarLineUpdater;
import org.phantazm.zombies.sidebar.section.CollectionSidebarSection;
import org.phantazm.zombies.sidebar.section.SidebarSection;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SidebarUpdaterTest {

    private Sidebar sidebar;

    private SidebarUpdater updater;

    @BeforeEach
    public void setup() {
        sidebar = new Sidebar(Component.text("Test Sidebar"));
        updater = null;
    }

    @Test
    public void testInitiallyEmptyWithNoSections() {
        updater = new SidebarUpdater(sidebar, Collections.emptyList());

        updater.start();
        updater.tick(0);

        assertEquals(0, sidebar.getLines().size());
    }

    @Test
    public void testInitiallyEmptyWithEmptySection() {
        updater = new SidebarUpdater(sidebar,
            Collections.singleton(new CollectionSidebarSection(Collections.emptyList())));

        updater.start();
        updater.tick(0);

        assertEquals(0, sidebar.getLines().size());
    }

    @Test
    public void testOneSectionOneLine() {
        Component message = Component.text("Hello, World!");
        SidebarLineUpdater lineUpdater = new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(message));
        SidebarSection section = new CollectionSidebarSection(Collections.singleton(lineUpdater));
        updater = new SidebarUpdater(sidebar, Collections.singleton(section));

        updater.start();
        updater.tick(0);

        assertEquals(1, sidebar.getLines().size());
        Sidebar.ScoreboardLine line = sidebar.getLines().iterator().next();
        assertEquals(message, line.getContent());
    }

    @Test
    public void testLinesInOrder() {
        Component firstMessage = Component.text("First Line");
        SidebarLineUpdater firstUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(firstMessage));
        Component secondMessage = Component.text("Second Line");
        SidebarLineUpdater secondUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(secondMessage));
        SidebarSection section = new CollectionSidebarSection(List.of(firstUpdater, secondUpdater));
        updater = new SidebarUpdater(sidebar, Collections.singleton(section));

        updater.start();
        updater.tick(0);

        assertEquals(2, sidebar.getLines().size());
        List<Sidebar.ScoreboardLine> lines = new ArrayList<>(sidebar.getLines());
        lines.sort(Comparator.comparingInt(Sidebar.ScoreboardLine::getLine).reversed());
        assertEquals(firstMessage, lines.get(0).getContent());
        assertEquals(secondMessage, lines.get(1).getContent());
    }

    @Test
    public void testTooManyLines() {
        Collection<SidebarLineUpdater> lineUpdaters = new ArrayList<>(SidebarUpdater.MAX_SIDEBAR_ROWS + 1);
        for (int i = 0; i <= SidebarUpdater.MAX_SIDEBAR_ROWS; ++i) {
            Component message = Component.text("Line " + i);
            SidebarLineUpdater updater = new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(message));
            lineUpdaters.add(updater);
        }
        SidebarSection section = new CollectionSidebarSection(lineUpdaters);
        updater = new SidebarUpdater(sidebar, Collections.singleton(section));

        assertDoesNotThrow(() -> {
            updater.start();
            updater.tick(0);
        });

        assertEquals(SidebarUpdater.MAX_SIDEBAR_ROWS, sidebar.getLines().size());
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class MutableSidebarSection implements SidebarSection {

        private final Collection<? extends SidebarLineUpdater> lineUpdaters;

        public MutableSidebarSection(@NotNull Collection<? extends SidebarLineUpdater> lineUpdaters) {
            this.lineUpdaters = Objects.requireNonNull(lineUpdaters);
        }

        @Override
        public void invalidateCache() {
            for (SidebarLineUpdater lineUpdater : lineUpdaters) {
                lineUpdater.invalidateCache();
            }
        }

        @Override
        public int getSize() {
            return lineUpdaters.size();
        }

        @Override
        public @NotNull List<Optional<Component>> update(long time) {
            List<Optional<Component>> updates = new ArrayList<>(lineUpdaters.size());
            for (SidebarLineUpdater lineUpdater : lineUpdaters) {
                updates.add(lineUpdater.tick(time));
            }
            return updates;
        }
    }

    @Test
    public void testChangingSectionLengthInIntermediateSection() {
        Component firstMessage = Component.text("First Line");
        SidebarLineUpdater firstUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(firstMessage));
        Component secondMessage = Component.text("Second Line");
        SidebarLineUpdater secondUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(secondMessage));
        Component thirdMessage = Component.text("Third Line");
        SidebarLineUpdater thirdUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(thirdMessage));
        List<SidebarLineUpdater> intermediateLines = new ArrayList<>(2);
        intermediateLines.add(firstUpdater);
        SidebarSection firstSection = new MutableSidebarSection(intermediateLines);
        SidebarSection secondSection = new CollectionSidebarSection(Collections.singleton(thirdUpdater));
        updater = new SidebarUpdater(sidebar, List.of(firstSection, secondSection));

        updater.start();
        updater.tick(0);

        intermediateLines.add(secondUpdater);
        updater.tick(1);

        assertEquals(3, sidebar.getLines().size());
        List<Sidebar.ScoreboardLine> lines = new ArrayList<>(sidebar.getLines());
        lines.sort(Comparator.comparingInt(Sidebar.ScoreboardLine::getLine).reversed());
        assertEquals(firstMessage, lines.get(0).getContent());
        assertEquals(secondMessage, lines.get(1).getContent());
        assertEquals(thirdMessage, lines.get(2).getContent());
    }

    @Test
    public void testChangingSectionLengthInFinalSection() {
        Component firstMessage = Component.text("First Line");
        SidebarLineUpdater firstUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(firstMessage));
        Component secondMessage = Component.text("Second Line");
        SidebarLineUpdater secondUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(secondMessage));
        Component thirdMessage = Component.text("Third Line");
        SidebarLineUpdater thirdUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(thirdMessage));
        List<SidebarLineUpdater> finalLines = new ArrayList<>(2);
        finalLines.add(secondUpdater);
        SidebarSection firstSection = new CollectionSidebarSection(Collections.singleton(firstUpdater));
        SidebarSection secondSection = new MutableSidebarSection(finalLines);
        updater = new SidebarUpdater(sidebar, List.of(firstSection, secondSection));

        updater.start();
        updater.tick(0);

        finalLines.add(thirdUpdater);
        updater.tick(1);

        assertEquals(3, sidebar.getLines().size());
        List<Sidebar.ScoreboardLine> lines = new ArrayList<>(sidebar.getLines());
        lines.sort(Comparator.comparingInt(Sidebar.ScoreboardLine::getLine).reversed());
        assertEquals(firstMessage, lines.get(0).getContent());
        assertEquals(secondMessage, lines.get(1).getContent());
        assertEquals(thirdMessage, lines.get(2).getContent());
    }

    @Test
    public void testRemoveStartFromSingleSection() {
        Component firstMessage = Component.text("First Line");
        SidebarLineUpdater firstUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(firstMessage));
        Component secondMessage = Component.text("Second Line");
        SidebarLineUpdater secondUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(secondMessage));
        Deque<SidebarLineUpdater> lineUpdaters = new ArrayDeque<>();
        lineUpdaters.push(firstUpdater);
        lineUpdaters.push(secondUpdater);
        SidebarSection section = new MutableSidebarSection(lineUpdaters);
        updater = new SidebarUpdater(sidebar, Collections.singleton(section));

        updater.start();
        updater.tick(0);

        lineUpdaters.removeLast();
        updater.tick(1);

        assertEquals(1, sidebar.getLines().size());
        assertEquals(secondMessage, sidebar.getLines().iterator().next().getContent());
    }

    @Test
    public void testRemoveEndFromSingleSection() {
        Component firstMessage = Component.text("First Line");
        SidebarLineUpdater firstUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(firstMessage));
        Component secondMessage = Component.text("Second Line");
        SidebarLineUpdater secondUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(secondMessage));
        Deque<SidebarLineUpdater> lineUpdaters = new ArrayDeque<>(2);
        lineUpdaters.push(firstUpdater);
        lineUpdaters.push(secondUpdater);
        SidebarSection section = new MutableSidebarSection(lineUpdaters);
        updater = new SidebarUpdater(sidebar, Collections.singleton(section));

        updater.start();
        updater.tick(0);

        lineUpdaters.removeFirst();
        updater.tick(1);

        assertEquals(1, sidebar.getLines().size());
        assertEquals(firstMessage, sidebar.getLines().iterator().next().getContent());
    }

    @Test
    public void testRemoveMiddleFromSingleSection() {
        Component firstMessage = Component.text("First Line");
        SidebarLineUpdater firstUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(firstMessage));
        Component secondMessage = Component.text("Second Line");
        SidebarLineUpdater secondUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(secondMessage));
        Component thirdMessage = Component.text("Third Line");
        SidebarLineUpdater thirdUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(thirdMessage));
        List<SidebarLineUpdater> lineUpdaters = new ArrayList<>(3);
        lineUpdaters.add(firstUpdater);
        lineUpdaters.add(secondUpdater);
        lineUpdaters.add(thirdUpdater);
        SidebarSection section = new MutableSidebarSection(lineUpdaters);
        updater = new SidebarUpdater(sidebar, Collections.singleton(section));

        updater.start();
        updater.tick(0);

        lineUpdaters.remove(1);
        updater.tick(1);

        assertEquals(2, sidebar.getLines().size());
        List<Sidebar.ScoreboardLine> lines = new ArrayList<>(sidebar.getLines());
        lines.sort(Comparator.comparingInt(Sidebar.ScoreboardLine::getLine).reversed());
        assertEquals(firstMessage, lines.get(0).getContent());
        assertEquals(thirdMessage, lines.get(1).getContent());
    }

    @Test
    public void testRemoveMiddleFromMultipleSections() {
        Component firstMessage = Component.text("First Line");
        SidebarLineUpdater firstUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(firstMessage));
        Component secondMessage = Component.text("Second Line");
        SidebarLineUpdater secondUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(secondMessage));
        Component thirdMessage = Component.text("Third Line");
        SidebarLineUpdater thirdUpdater =
            new ConstantSidebarLineUpdater(new ConstantSidebarLineUpdater.Data(thirdMessage));
        Deque<SidebarLineUpdater> firstUpdaters = new ArrayDeque<>(2);
        firstUpdaters.push(firstUpdater);
        firstUpdaters.push(secondUpdater);
        SidebarSection firstSection = new MutableSidebarSection(firstUpdaters);
        SidebarSection secondSection = new CollectionSidebarSection(Collections.singleton(thirdUpdater));
        updater = new SidebarUpdater(sidebar, List.of(firstSection, secondSection));

        updater.start();
        updater.tick(0);

        firstUpdaters.removeFirst();
        updater.tick(1);

        assertEquals(2, sidebar.getLines().size());
        List<Sidebar.ScoreboardLine> lines = new ArrayList<>(sidebar.getLines());
        lines.sort(Comparator.comparingInt(Sidebar.ScoreboardLine::getLine).reversed());
        assertEquals(firstMessage, lines.get(0).getContent());
        assertEquals(thirdMessage, lines.get(1).getContent());
    }

    @AfterEach
    public void tearDown() {
        if (updater != null) {
            updater.end();
        }
    }

}
