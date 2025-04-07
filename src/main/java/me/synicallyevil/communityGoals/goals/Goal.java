package me.synicallyevil.communityGoals.goals;

import java.util.List;

public class Goal {

    private final int id;
    private final String name;
    private final GoalType type;
    private final int max;
    private final List<String> completionCommands;

    private int current;

    public Goal(int id, String name, int current, int max, GoalType type, List<String> completionCommands) {
        this.id = id;
        this.name = name;
        this.current = current;
        this.max = max;
        this.type = type;
        this.completionCommands = completionCommands;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCurrent() {
        return current;
    }

    public int getMax() {
        return max;
    }

    public GoalType getType() {
        return type;
    }

    public boolean isCompleted() {
        return current >= max;
    }

    public void increment(int amount) {
        if (!isCompleted()) {
            current += amount;
            if (current > max) current = max;
        }
    }

    public void reset() {
        this.current = 0;
    }

    public List<String> getCompletionCommands() {
        return completionCommands;
    }
}
