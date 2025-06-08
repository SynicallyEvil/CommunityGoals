package me.synicallyevil.communityGoals.goals;

import me.synicallyevil.communityGoals.CommunityGoals;
import org.bukkit.Bukkit;

import java.util.*;

public class GoalsManager {

    private final CommunityGoals cg;
    private final Map<Integer, Goal> goals = new HashMap<>();
    private int nextGoalID = 1;

    public GoalsManager(CommunityGoals cg){
        this.cg = cg;
    }

    public void loadGoals() {
        try{
            cg.getStorage().loadGoals();
        }catch (Exception ex){
            Bukkit.getLogger().severe("Failed to load goals: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void saveGoals() {
        try{
            cg.getStorage().saveGoals();
        }catch(Exception ex){
            Bukkit.getLogger().severe("Failed to save goals: " + ex.getMessage());
            ex.printStackTrace();
        }
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