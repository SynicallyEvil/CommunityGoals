package me.synicallyevil.communityGoals.utils;

import me.synicallyevil.communityGoals.managers.GoalManager;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

public class Utils {

    public static String getColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static boolean isNumber(String s) {
        return s.matches("\\d+");
    }

    public static String getProgressBar(int current, int max, String symbol, int symbolAmount, String completedColor, String remainingColor) {
        int bars = (int) (symbolAmount * ((float) current / max));
        int leftOver = symbolAmount - bars;

        return getColor(completedColor) + symbol.repeat(bars) + getColor(remainingColor) + symbol.repeat(leftOver);
    }

    public static String getPercentage(int current, int max) {
        return String.format("%.2f%%", ((double) current * 100.0) / max);
    }

    public static GoalManager getCurrentGoal(Map<Integer, GoalManager> funds) {
        return funds.values().stream()
                .filter(fund -> !fund.isDone())
                .findFirst()
                .orElse(null);
    }

    public static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        return map.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .toList();
    }
}
