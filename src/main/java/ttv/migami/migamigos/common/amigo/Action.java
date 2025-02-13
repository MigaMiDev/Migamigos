package ttv.migami.migamigos.common.amigo;

import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.List;
import java.util.function.Consumer;

public class Action {
    private final List<Integer> keyframeTimings;
    private final List<Consumer<AmigoEntity>> attackActions;
    private int currentStage = 0;

    public Action(List<Integer> keyframeTimings, List<Consumer<AmigoEntity>> attackActions) {
        this.keyframeTimings = keyframeTimings;
        this.attackActions = attackActions;
    }

    public boolean isComplete() {
        return currentStage >= keyframeTimings.size();
    }

    public int getNextKeyframe() {
        return isComplete() ? -1 : keyframeTimings.get(currentStage);
    }

    public void executeNextStage(AmigoEntity actor) {
        if (!isComplete()) {
            attackActions.get(currentStage).accept(actor);
            currentStage++;
        }
    }
}