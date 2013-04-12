package com.timatooth.mineload;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.dynamic.API_Method;
import com.alecgorge.minecraft.jsonapi.dynamic.JSONAPIMethodProvider;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class LWCJsonProvider implements JSONAPIMethodProvider {
    private LWC lwcInstance;

    public LWCJsonProvider() {
        LWCPlugin lwcplugin = (LWCPlugin) Bukkit.getServer().getPluginManager().getPlugin("LWC");
        this.lwcInstance = lwcplugin.getLWC();
        
        if (lwcInstance == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Something went wrong getting LWC instance.");
        }
        
        //get jsonapi and register this as a method provider
        JSONAPI jsonapiplugin = (JSONAPI) Bukkit.getServer().getPluginManager().getPlugin("JSONAPI");
        jsonapiplugin.registerMethods(this);
    }

    /**
     * Returns formatted string of world,x.y.z of chests related to a Player
     *     
     * @param playername
     * @return formatted string of world,x.y.z of chests related to a Player
     * @since MineloadPlugin 0.0.5
     */
    @API_Method(
            namespace = "mineload",
            description = "Get chest locations related to player",
            returnDescription = "array of [world,x,y,x] strings",
            argumentDescriptions = {
        "Playername to get chests"
    })
    public String[] getPlayerLWCChests(String playername) {
        List<Protection> protections = lwcInstance.getPhysicalDatabase().loadProtectionsByPlayer(playername);
        List<Protection> chests = new ArrayList<Protection>();

        for (Protection p : protections) {
            if (p.getBlock().getType() == Material.CHEST) {
                chests.add(p);
            }
        }

        String[] toReturn = new String[chests.size()];
        for (int i = 0; i < chests.size(); i++) {
            int x = chests.get(i).getX();
            int y = chests.get(i).getY();
            int z = chests.get(i).getZ();
            String world = chests.get(i).getWorld();
            toReturn[i] = world + "," + String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z);
        }
        return toReturn;
    }
}