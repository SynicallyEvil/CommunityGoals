package me.synicallyevil.communityGoals.utils;

import me.synicallyevil.communityGoals.managers.FundManager;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

public class Utils {

    public static String getColor(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static boolean isNumber(String s){
        return s.matches("\\d+");
    }

    public static String getProgressBar(int current, int max, String symbol, int symbolamount, String completedColor, String remainingColor){
        float percent = (float) current / max;
        int bars = (int) (symbolamount * percent);
        int leftOver = (symbolamount - bars);

        StringBuilder sb = new StringBuilder();
        sb.append(getColor(completedColor));
        for (int i = 0; i < bars; i++)
            sb.append(symbol);

        sb.append(getColor(remainingColor));
        for (int i = 0; i < leftOver; i++)
            sb.append(symbol);

        return sb.toString();
    }

    public static String getPercentage(int current, int max){
        double percent = ((double)current * 100.0) / max;
        return String.format("%.2f%%", percent);
    }

    public static FundManager getCurrentFund(HashMap<Integer, FundManager> funds){
        int count = 0;
        for(int i = 0; i <= funds.size(); i++){
            if(funds.get(i) == null)
                continue;

            FundManager fund = funds.get(i);

            if(count == (funds.size()+1) && fund.isDone()){
                return null;
            }

            if(fund.isDone()) {
                count++;
                continue;
            }

            return fund;
        }

        return null;
    }

    public static <K,V extends Comparable<? super V>> List<Map.Entry<K, V>> entriesSortedByValues(Map<K,V> map) {
        List<Map.Entry<K,V>> sortedEntries = new ArrayList<Map.Entry<K,V>>(map.entrySet());
        Collections.sort(sortedEntries, (e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        return sortedEntries;
    }

}
