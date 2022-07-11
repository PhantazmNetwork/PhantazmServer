package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.game.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StageTransition implements Tickable {

    private final List<Stage> stages;

    private Stage currentStage;

    private int currentStageIndex;

    public StageTransition(@NotNull List<Stage> stages) {
        this.stages = List.copyOf(stages);
        this.currentStageIndex = 0;
        this.currentStage = stages.get(currentStageIndex);
    }

    @Override
    public void tick(long time) {
        if (isComplete()) {
            return;
        }

        if (currentStage.shouldEnd()) {
            setCurrentStageIndex(currentStageIndex + 1);
            return;
        }

        currentStage.tick(time);
    }

    public boolean isComplete() {
        return currentStageIndex == stages.size();
    }

    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    public @NotNull Stage getCurrentStage() {
        return currentStage;
    }

    public int getStageCount() {
        return stages.size();
    }

    public void setCurrentStageIndex(int currentStageIndex) {
        if (currentStageIndex < 0 || currentStageIndex >= stages.size()) {
            throw new IllegalArgumentException("Invalid stage index: " + currentStageIndex);
        }

        this.currentStageIndex = currentStageIndex;
        currentStage.end();
        currentStage = stages.get(currentStageIndex);
    }
}
