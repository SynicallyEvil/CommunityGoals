package me.synicallyevil.communityGoals.goals;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.storage.GoalsStorage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GoalsManager {

    private final CommunityGoals cg;
    private final GoalsStorage storage;
    private final Map<Integer, Goal> goals = new HashMap<>();
    private int nextGoalID = 1;

    public GoalsManager(CommunityGoals cg){
        this.cg = cg;
        this.storage = new GoalsStorage(cg, this);
    }

    public void loadGoals() {
        storage.loadGoals();
    }

    public void saveGoals() {
        storage.saveGoals();
    }

    public Goal getCurrentGoal() {
        return goals.values().stream()
                .filter(goal -> !goal.isCompleted())
                .min(Comparator.comparingInt(Goal::getId))
                .orElse(null);
    }

    public Collection<Goal> getAllGoals() {
        return Collections.unmodifiableCollection(goals.values());
    }

    public void incrementGoalProgress(GoalType type, int amount) {
        for (Goal goal : goals.values()) {
            if (goal.getType() == type && !goal.isCompleted()) {
                goal.increment(amount);
                break;
            }
        }
    }

    public void resetGoals() {
        goals.values().forEach(goal -> {
            goal.increment(-goal.getCurrent()); // reset to 0
        });
    }

    public int getNextGoalID() {
        return nextGoalID;
    }

    public void setNextGoalID(int nextGoalID) {
        this.nextGoalID = nextGoalID;
    }

    public Map<Integer, Goal> getGoals(){
        return goals;
    }
}