package me.synicallyevil.communityGoals.data;

import me.synicallyevil.communityGoals.CommunityGoals;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FundTop {

    private final CommunityGoals cg;
    private final File topDirectory;
    private final File topFile;

    public FundTop(CommunityGoals cg) {
        this.cg = cg;
        this.topDirectory = new File(cg.getDataFolder(), "fundtops");
        this.topFile = new File(topDirectory, "top.yml");

        ensureFileExists();
    }

    private void ensureFileExists() {
        if (!topDirectory.exists()) {
            topDirectory.mkdirs();
        }

        if (!topFile.exists()) {
            try {
                topFile.createNewFile();
                getConfig().save(topFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPaid(UUID uuid, int amount) {
        FileConfiguration data = getConfig();
        int currentAmount = data.getInt(uuid.toString(), 0);
        data.set(uuid.toString(), currentAmount + amount);
        save(data);
    }

    public FileConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(topFile);
    }

    private void save(FileConfiguration data) {
        try {
            data.save(topFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

