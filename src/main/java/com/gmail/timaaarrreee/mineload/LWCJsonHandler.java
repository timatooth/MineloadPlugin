/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.timaaarrreee.mineload;

import com.alecgorge.minecraft.jsonapi.api.APIMethodName;
import com.alecgorge.minecraft.jsonapi.api.JSONAPICallHandler;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author tim
 */
public class LWCJsonHandler implements JSONAPICallHandler {

  private LWC lwc;

  public LWCJsonHandler(LWC instance) {
    this.lwc = instance;
  }

  @Override
  public boolean willHandle(APIMethodName methodName) {
    if (methodName.matches("mineload.getLWCPlayerChests")) {
      return true;
    }

    return false;

  }

  @Override
  public Object handle(APIMethodName methodName, Object[] args) {
    if (methodName.matches("mineload.getLWCPlayerChests")) {
      List<Protection> protections = lwc.getPhysicalDatabase().loadProtectionsByPlayer((String) args[0]);
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
    
    else {
      return null;
    }
  }
}
