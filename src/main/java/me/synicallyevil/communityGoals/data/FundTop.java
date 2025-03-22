package me.synicallyevil.communityGoals.data;

import me.synicallyevil.communityGoals.CommunityGoals;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FundTop {

    private CommunityGoals cg;
    private File top;

    public FundTop(CommunityGoals cg){
        this.cg = cg;
        this.top = new File(cg.getDataFolder(), File.separator + "fundtops" + File.separator);

        if(!(top.exists()))
            top.mkdirs();

        File file = new File(top, "top.yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        if(!(file.exists())){
            try {
                file.createNewFile();

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        save(data, file);
    }

    private void save(FileConfiguration data, File file){
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPaid(UUID uuid, int amount){
        File file = new File(top, "top.yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        if(data.isSet(String.valueOf(uuid))){
            int a = data.getInt(String.valueOf(uuid));
            data.set(String.valueOf(uuid), a+amount);
        }else
            data.set(String.valueOf(uuid), amount);

        save(data, file);
    }

    public FileConfiguration getConfig(){
        File file = new File(top, "top.yml");
        return YamlConfiguration.loadConfiguration(file);
    }
}
