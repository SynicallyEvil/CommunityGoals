package me.synicallyevil.communityGoals.managers;

import me.synicallyevil.communityGoals.CommunityGoals;

import java.util.List;

public class FundManager {

    private CommunityGoals cg;
    private String name;
    private int number;
    private int current;
    private int max;
    private boolean isDone;

    private List<String> commands;

    public FundManager(CommunityGoals cg, String name, int number, int current, int max, List<String> commands){
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
        if(current >= this.current)
            this.current = current;

        checkDone();
    }

    public int getCurrent() {
        return current;
    }

    public void addAmount(int amount){
        cg.getConfig().set("fund.goals." + this.number + ".current", current + amount);
        cg.saveConfig();
        setCurrent(current + amount);
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public void setName(String name){
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

    public void checkDone(){
        isDone = (this.current >= this.max);
    }

    public int getRemaining(){
        return (max - current);
    }

    public void reset(){
        isDone = false;
        current = 0;
        cg.getConfig().set("fund.goals." + this.number + ".current", 0);
        cg.saveConfig();
    }
}

