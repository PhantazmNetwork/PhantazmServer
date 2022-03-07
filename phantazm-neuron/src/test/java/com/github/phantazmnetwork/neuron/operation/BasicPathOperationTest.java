package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.MazeReader;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.agent.Explorer;
import com.github.phantazmnetwork.neuron.node.Destination;
import com.github.phantazmnetwork.neuron.node.Node;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class BasicPathOperationTest {
    @SuppressWarnings("SameParameterValue")
    private static PathOperation makeOperation(Vec3I destination, Vec3I startPosition, Iterable<Vec3I> walkDirections,
                                               Calculator calculator, Collection<Vec3I> solids) {
        Explorer mockExplorer = Mockito.mock(Explorer.class);
        Mockito.when(mockExplorer.walkVectors(any())).thenAnswer(invocation -> {
            Node node = invocation.getArgument(0);

            int x = node.getX();
            int y = node.getY();
            int z = node.getZ();

            //rudimentary simulation of collision checking: filter all directions that would collide with a "solid"
            return StreamSupport.stream(walkDirections.spliterator(), false).filter(direction -> solids.stream()
                    .noneMatch(solid -> Vec3I.equals(solid.getX(), solid.getY(), solid.getZ(), x + direction.getX(),
                            y + direction.getY(), z + direction.getZ()))).collect(Collectors.toList());
        });

        Agent mockAgent = Mockito.mock(Agent.class);
        Mockito.when(mockAgent.getStartPosition()).thenReturn(startPosition);
        Mockito.when(mockAgent.getCalculator()).thenReturn(calculator);
        Mockito.when(mockAgent.reachedDestination(eq(destination.getX()), eq(destination.getY()),
                eq(destination.getZ()), any())).thenReturn(true);
        Mockito.when(mockAgent.getWalker()).thenReturn(mockExplorer);

        Destination mockDestination = Mockito.mock(Destination.class);
        Mockito.when(mockDestination.getX()).thenReturn(destination.getX());
        Mockito.when(mockDestination.getY()).thenReturn(destination.getY());
        Mockito.when(mockDestination.getZ()).thenReturn(destination.getZ());

        PathContext mockContext = Mockito.mock(PathContext.class);
        Mockito.when(mockContext.getAgent()).thenReturn(mockAgent);
        Mockito.when(mockContext.getDestination()).thenReturn(mockDestination);

        return new BasicPathOperation(mockContext);
    }

    private static void assertPathMatches(Vec3I[] expectedPath, PathOperation.State expectedCompletionState,
                                          PathOperation operationToRun) {
        PathResult result = operationToRun.runToCompletion();
        assertSame(expectedCompletionState, operationToRun.getState());

        List<Node> nodes = new ArrayList<>();
        for(Node node : result.getPath()) {
            nodes.add(node);
        }

        assertEquals(Arrays.asList(expectedPath), nodes.stream().map(node -> new ImmutableVec3I(node.getX(),
                node.getY(), node.getZ())).collect(Collectors.toList()));
        assertSame(expectedPath.length, nodes.size());
    }

    @Nested
    class UpwardDestination {
        private static final Vec3I[] OPTIMAL_PATH = new Vec3I[] {
                new ImmutableVec3I(0, 0, 0),
                new ImmutableVec3I(0, 1, 0),
                new ImmutableVec3I(0, 2, 0),
                new ImmutableVec3I(0, 3, 0),
                new ImmutableVec3I(0, 4, 0),
                new ImmutableVec3I(0, 5, 0),
                new ImmutableVec3I(0, 6, 0),
                new ImmutableVec3I(0, 7, 0),
                new ImmutableVec3I(0, 8, 0),
                new ImmutableVec3I(0, 9, 0),
                new ImmutableVec3I(0, 10, 0)
        };

        private static final Vec3I UPWARD_DESTINATION = new ImmutableVec3I(0, 10, 0);
        private static final Collection<Vec3I> UPWARD_MOVEMENT = List.of(new ImmutableVec3I(0, 1, 0));
        private static final Collection<Vec3I> CARDINAL_MOVEMENT = List.of(new ImmutableVec3I(0, 1, 0),
                new ImmutableVec3I(0, -1, 0), new ImmutableVec3I(1, 0, 0),
                new ImmutableVec3I(-1, 0, 0), new ImmutableVec3I(0, 0, 1),
                new ImmutableVec3I(0, 0, -1));

        @Nested
        class Unblocked {
            private static final Collection<Vec3I> NO_SOLIDS = Collections.emptyList();

            @Test
            void upwardMovement() {
                assertPathMatches(OPTIMAL_PATH, PathOperation.State.SUCCEEDED, makeOperation(UPWARD_DESTINATION,
                        Vec3I.ORIGIN, UPWARD_MOVEMENT, Calculator.SQUARED_DISTANCE, NO_SOLIDS));
            }

            @Test
            void cardinalMovement() {
                assertPathMatches(OPTIMAL_PATH, PathOperation.State.SUCCEEDED, makeOperation(UPWARD_DESTINATION,
                        Vec3I.ORIGIN, CARDINAL_MOVEMENT, Calculator.SQUARED_DISTANCE, NO_SOLIDS));
            }
        }

        @Nested
        class Blocked {
            @Nested
            class Fully {
                private static final Collection<Vec3I> SURROUNDED_BY_SOLIDS = CARDINAL_MOVEMENT;
                private static final Vec3I[] FULLY_BLOCKED_PATH = new Vec3I[] {
                        new ImmutableVec3I(0, 0, 0)
                };

                @Test
                void cardinalMovement() {
                    assertPathMatches(FULLY_BLOCKED_PATH, PathOperation.State.FAILED, makeOperation(UPWARD_DESTINATION,
                            Vec3I.ORIGIN, CARDINAL_MOVEMENT, Calculator.SQUARED_DISTANCE, SURROUNDED_BY_SOLIDS));
                }
            }

            @Nested
            class Partially {
                private static final Collection<Vec3I> POSITIVE_X_MISSING_SOLIDS = new ArrayList<>(CARDINAL_MOVEMENT);
                private static final Vec3I DESTINATION_X_MISSING = new ImmutableVec3I(1, 10, 0);
                private static final Vec3I[] OPTIMAL_PATH_X_MISSING = new Vec3I[] {
                        new ImmutableVec3I(0, 0, 0),
                        new ImmutableVec3I(1, 0, 0),
                        new ImmutableVec3I(1, 1, 0),
                        new ImmutableVec3I(1, 2, 0),
                        new ImmutableVec3I(1, 3, 0),
                        new ImmutableVec3I(1, 4, 0),
                        new ImmutableVec3I(1, 5, 0),
                        new ImmutableVec3I(1, 6, 0),
                        new ImmutableVec3I(1, 7, 0),
                        new ImmutableVec3I(1, 8, 0),
                        new ImmutableVec3I(1, 9, 0),
                        new ImmutableVec3I(1, 10, 0),
                };

                static {
                    POSITIVE_X_MISSING_SOLIDS.remove(new ImmutableVec3I(1, 0, 0));
                }

                @Test
                void cardinalMovement() {
                    assertPathMatches(OPTIMAL_PATH_X_MISSING, PathOperation.State.SUCCEEDED,
                            makeOperation(DESTINATION_X_MISSING, Vec3I.ORIGIN, CARDINAL_MOVEMENT, Calculator.SQUARED_DISTANCE,
                                    POSITIVE_X_MISSING_SOLIDS));
                }
            }
        }
    }

    @Nested
    class Maze {
        private static final Collection<Vec3I> FLAT_CARDINAL_MOVEMENT = List.of(
                new ImmutableVec3I(1, 0, 0), new ImmutableVec3I(-1, 0, 0),
                new ImmutableVec3I(0, 0, 1), new ImmutableVec3I(0, 0, -1));
        private static final Calculator CALCULATOR = Calculator.SQUARED_DISTANCE;

        @Test
        void first() throws IOException {
            MazeReader.Data data = MazeReader.readMaze("maze_1");
            assertPathMatches(data.correctPath(), PathOperation.State.SUCCEEDED, makeOperation(data.end(), data.start(),
                    FLAT_CARDINAL_MOVEMENT, CALCULATOR, data.solids()));
        }

        @Test
        void second() throws IOException {
            MazeReader.Data data = MazeReader.readMaze("maze_2");
            assertPathMatches(data.correctPath(), PathOperation.State.SUCCEEDED, makeOperation(data.end(), data.start(),
                    FLAT_CARDINAL_MOVEMENT, CALCULATOR, data.solids()));
        }

        @Test
        void third() throws IOException {
            MazeReader.Data data = MazeReader.readMaze("maze_3");
            assertPathMatches(data.correctPath(), PathOperation.State.FAILED, makeOperation(data.end(), data.start(),
                    FLAT_CARDINAL_MOVEMENT, CALCULATOR, data.solids()));
        }

        @Test
        void fourth() throws IOException {
            MazeReader.Data data = MazeReader.readMaze("maze_4");
            assertPathMatches(data.correctPath(), PathOperation.State.SUCCEEDED, makeOperation(data.end(), data.start(),
                    FLAT_CARDINAL_MOVEMENT, CALCULATOR, data.solids()));
        }
    }
}