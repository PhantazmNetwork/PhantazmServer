package org.phantazm.zombies.stage;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.tick.Activable;

public class StageTransition implements Activable {

    private final Stage[] stages;

    private Stage currentStage;

    private int currentStageIndex;

    public StageTransition(@NotNull Stage @NotNull ... stages) {
        this.stages = stages.clone();
    }

    public void start() {
        setCurrentStageIndex(0);
    }

    @Override
    public void tick(long time) {
        if (isComplete()) {
            return;
        }

        if (currentStage.shouldContinue()) {
            setCurrentStageIndex(currentStageIndex + 1);
            return;
        }
        if (currentStage.shouldRevert()) {
            setCurrentStageIndex(currentStageIndex - 1);
            return;
        }
        if (currentStage.shouldAbort()) {
            setCurrentStageIndex(stages.length);
            return;
        }

        currentStage.tick(time);
    }

    public boolean isComplete() {
        return currentStageIndex == stages.length;
    }

    private void setCurrentStageIndex(int currentStageIndex) {
        if (currentStageIndex < 0 || currentStageIndex > stages.length) {
            throw new IllegalArgumentException("Invalid stage index: " + currentStageIndex);
        }

        this.currentStageIndex = currentStageIndex;
        if (currentStage != null) {
            currentStage.end();
        }

        if (currentStageIndex != stages.length) {
            currentStage = stages[currentStageIndex];
            currentStage.start();
        } else {
            currentStage = null;
        }
    }

    public @Nullable Stage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(@NotNull Key stageKey) {
        for (int i = 0; i < stages.length; i++) {
            if (stages[i].key().equals(stageKey)) {
                setCurrentStageIndex(i);
                return;
            }
        }
    }

    public void end() {
        if (currentStageIndex != stages.length) {
            setCurrentStageIndex(stages.length);
        }
    }
}
