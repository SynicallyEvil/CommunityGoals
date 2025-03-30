package me.synicallyevil.communityGoals.managers;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.utils.GoalTypes;

import java.util.List;

public class GoalManager {

    private final CommunityGoals cg;
    private String name;
    private GoalTypes type;
    private final int number;
    private int current;
    private int max;
    private boolean isDone;
    private List<String> commands;

    public GoalManager(CommunityGoals cg, String name, GoalTypes type, int number, int current, int max, List<String> commands) {
        this.cg = cg;
        this.name = name;
        this.type = type;
        this.number = number;
        this.max = max;
        this.commands = commands;
        setCurrent(current);
        checkDone();
    }

    public boolean isDone() {
        return isDone;
    }

    public void setCurrent(int current) {
        this.current = Math.max(current, this.current);
        checkDone();
    }

    public int getCurrent() {
        return current;
    }

    public void addAmount(int amount) {
        setCurrent(current + amount);
        updateConfig();
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public GoalTypes getType(){
        return type;
    }

    public void setType(GoalTypes type) {
        this.type = type;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public List<String> getCommands() {
        return commands;
    }

    public int getRemaining() {
        return Math.max(0, max - current);
    }

    public void reset() {
        isDone = false;
        current = 0;
        updateConfig();
    }

    public void checkDone() {
        isDone = current >= max;
    }

    private void updateConfig() {
        cg.getConfig().set("goal.goals." + number + ".current", current);
        cg.saveConfig();
    }

    public void updateFromConfig(org.bukkit.configuration.file.FileConfiguration config, int goal) {
        setCurrent(config.getInt("goal.goals." + goal + ".current", 0));
        setMax(config.getInt("goal.goals." + goal + ".max", 10000000));
        setName(config.getString("goal.goals." + goal + ".name"));
        setType(GoalTypes.valueOf(config.getString("goal.goals." + goal + ".type").toUpperCase()));
        setCommands(config.getStringList("goal.goals." + goal + ".console_commands"));
    }

    public static GoalManager createFromConfig(CommunityGoals cg, org.bukkit.configuration.file.FileConfiguration config, int goal) {
        return new GoalManager(
                cg,
                config.getString("goal.goals." + goal + ".name"),
                GoalTypes.valueOf(config.getString("goal.goals." + goal + ".type").toUpperCase()),
                goal,
                config.getInt("goal.goals." + goal + ".current", 0),
                config.getInt("goal.goals." + goal + ".max", 10000000),
                config.getStringList("goal.goals." + goal + ".console_commands")
        );
    }
}

