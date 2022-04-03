package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.MazeReader;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.agent.Explorer;
import com.github.phantazmnetwork.neuron.node.Node;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BasicPathOperationTest {
    @SuppressWarnings("SameParameterValue")
    private static PathOperation makeOperation(Vec3I destination, Vec3I startPosition, Iterable<Vec3I> walkDirections,
                                               Calculator calculator, Collection<Vec3I> solids) {
        Explorer mockExplorer = mock(Explorer.class);
        when(mockExplorer.walkVectors(any())).thenAnswer(invocation -> {
            Node node = invocation.getArgument(0);
            Vec3I nodePos = node.getPosition();

            int x = nodePos.getX();
            int y = nodePos.getY();
            int z = nodePos.getZ();

            //rudimentary simulation of collision checking: filter all directions that would collide with a "solid"
            return StreamSupport.stream(walkDirections.spliterator(), false).filter(direction -> solids.stream()
                    .noneMatch(solid -> Vec3I.equals(solid.getX(), solid.getY(), solid.getZ(), x + direction.getX(),
                            y + direction.getY(), z + direction.getZ()))).collect(Collectors.toList());
        });

        Agent mockAgent = mock(Agent.class);
        when(mockAgent.hasStartPosition()).thenReturn(true);
        when(mockAgent.getStartPosition()).thenReturn(startPosition);

        return new BasicPathOperation(startPosition, destination, (vec) -> vec.equals(destination), calculator,
                mockExplorer);
    }

    private static void assertPathMatches(Vec3I[] expectedPath, PathOperation.State expectedCompletionState,
                                          PathOperation operationToRun) {
        while (!operationToRun.isComplete()) {
            operationToRun.step();
        }

        PathResult result = operationToRun.getResult();

        assertSame(expectedCompletionState, operationToRun.getState());

        List<Node> nodes = new ArrayList<>();
        for(Node node : result.getPath()) {
            nodes.add(node);
        }

        assertEquals(Arrays.asList(expectedPath), nodes.stream().map(Node::getPosition).collect(Collectors.toList()));
    }

    @Nested
    class UpwardDestination {
        private static final Vec3I[] OPTIMAL_PATH = new Vec3I[] {
                Vec3I.ORIGIN,
                Vec3I.of(0, 1, 0),
                Vec3I.of(0, 2, 0),
                Vec3I.of(0, 3, 0),
                Vec3I.of(0, 4, 0),
                Vec3I.of(0, 5, 0),
                Vec3I.of(0, 6, 0),
                Vec3I.of(0, 7, 0),
                Vec3I.of(0, 8, 0),
                Vec3I.of(0, 9, 0),
                Vec3I.of(0, 10, 0)
        };

        private static final Vec3I UPWARD_DESTINATION = Vec3I.of(0, 10, 0);
        private static final Collection<Vec3I> UPWARD_MOVEMENT = List.of(Vec3I.of(0, 1, 0));
        private static final Collection<Vec3I> CARDINAL_MOVEMENT = List.of(Vec3I.of(0, 1, 0),
                Vec3I.of(0, -1, 0), Vec3I.of(1, 0, 0), Vec3I.of(-1, 0, 0), Vec3I.of(0, 0, 1),
                Vec3I.of(0, 0, -1));

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
                        Vec3I.ORIGIN
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
                private static final Vec3I DESTINATION_X_MISSING = Vec3I.of(1, 10, 0);
                private static final Vec3I[] OPTIMAL_PATH_X_MISSING = new Vec3I[] {
                        Vec3I.ORIGIN,
                        Vec3I.of(1, 0, 0),
                        Vec3I.of(1, 1, 0),
                        Vec3I.of(1, 2, 0),
                        Vec3I.of(1, 3, 0),
                        Vec3I.of(1, 4, 0),
                        Vec3I.of(1, 5, 0),
                        Vec3I.of(1, 6, 0),
                        Vec3I.of(1, 7, 0),
                        Vec3I.of(1, 8, 0),
                        Vec3I.of(1, 9, 0),
                        Vec3I.of(1, 10, 0),
                };

                static {
                    POSITIVE_X_MISSING_SOLIDS.remove(Vec3I.of(1, 0, 0));
                }

                @Test
                void cardinalMovement() {
                    assertPathMatches(OPTIMAL_PATH_X_MISSING, PathOperation.State.SUCCEEDED,
                            makeOperation(DESTINATION_X_MISSING, Vec3I.ORIGIN, CARDINAL_MOVEMENT,
                                    Calculator.SQUARED_DISTANCE, POSITIVE_X_MISSING_SOLIDS));
                }
            }
        }
    }

    @Nested
    class Maze {
        private static final Collection<Vec3I> FLAT_CARDINAL_MOVEMENT = List.of(Vec3I.of(1, 0, 0),
                Vec3I.of(-1, 0, 0), Vec3I.of(0, 0, 1), Vec3I.of(0, 0, -1));
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