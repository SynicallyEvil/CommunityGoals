package me.synicallyevil.communityGoals.managers;

import me.synicallyevil.communityGoals.CommunityGoals;

import java.util.List;

public class FundManager {

    private final CommunityGoals cg;
    private String name;
    private final int number;
    private int current;
    private int max;
    private boolean isDone;
    private List<String> commands;

    public FundManager(CommunityGoals cg, String name, int number, int current, int max, List<String> commands) {
        this.cg = cg;
        this.name = name;
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
        cg.getConfig().set("fund.goals." + number + ".current", current);
        cg.saveConfig();
    }

    public void updateFromConfig(org.bukkit.configuration.file.FileConfiguration config, int goal) {
        setCurrent(config.getInt("fund.goals." + goal + ".current", 0));
        setMax(config.getInt("fund.goals." + goal + ".max", 10000000));
        setName(config.getString("fund.goals." + goal + ".name"));
        setCommands(config.getStringList("fund.goals." + goal + ".console_commands"));
    }

    public static FundManager createFromConfig(CommunityGoals cg, org.bukkit.configuration.file.FileConfiguration config, int goal) {
        return new FundManager(
                cg,
                config.getString("fund.goals." + goal + ".name"),
                goal,
                config.getInt("fund.goals." + goal + ".current", 0),
                config.getInt("fund.goals." + goal + ".max", 10000000),
                config.getStringList("fund.goals." + goal + ".console_commands")
        );
    }
}

