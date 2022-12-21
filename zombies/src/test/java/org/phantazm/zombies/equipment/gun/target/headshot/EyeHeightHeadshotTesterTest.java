package org.phantazm.zombies.equipment.gun.target.headshot;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EyeHeightHeadshotTesterTest {

    private static final double EYE_HEIGHT = 1.5;

    private final EyeHeightHeadshotTester headshotTester = new EyeHeightHeadshotTester();

    private Entity shooter;

    private Pos shooterPos;

    private Entity target;

    private Pos targetPos;

    @BeforeEach
    public void setup() {
        shooter = mock(Entity.class);
        shooterPos = new Pos(0, 0, 0);
        when(shooter.getEyeHeight()).thenReturn(EYE_HEIGHT);
        when(shooter.getPosition()).thenReturn(shooterPos);

        target = mock(Entity.class);
        targetPos = new Pos(1, 0, 0);
        when(target.getEyeHeight()).thenReturn(EYE_HEIGHT);
        when(target.getPosition()).thenReturn(targetPos);
    }

    @Test
    public void testHeadshot() {
        assertTrue(headshotTester.isHeadshot(shooter, target, targetPos.withY(targetPos.y() + EYE_HEIGHT + 0.1F)));
    }

    @Test
    public void testNonHeadshot() {
        assertFalse(headshotTester.isHeadshot(shooter, target, targetPos.withY(targetPos.y() + EYE_HEIGHT - 0.1F)));
    }

}
