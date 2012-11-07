package com.gmail.timaaarrreee.mineload;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * Reads the data collection and spits out a string of nice xml for the socket
 * server.
 *
 * @author tim
 */
public class XmlFeed {

  private String xmlData;

  public XmlFeed() {
    long lastContactMainThread = System.currentTimeMillis() - MineloadPlugin.getHeartbeatTime();
    long startTime = System.currentTimeMillis();
    DataCollector data = MineloadPlugin.getData();
    //
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException ex) {
    }

    // root elements
    Document doc = docBuilder.newDocument();
    Element rootElement = doc.createElement("server");
    doc.appendChild(rootElement);

    Element motd = doc.createElement("motd");
    motd.appendChild(doc.createTextNode(data.getMotd()));
    rootElement.appendChild(motd);

    Element playercount = doc.createElement("playercount");
    playercount.appendChild(doc.createTextNode(String.valueOf(data.getPlayerCount())));
    rootElement.appendChild(playercount);

    Element maxplayers = doc.createElement("maxplayers");
    maxplayers.appendChild(doc.createTextNode(String.valueOf(data.getMaxPlayers())));
    rootElement.appendChild(maxplayers);

    Element totalplayers = doc.createElement("totalplayers");
    totalplayers.appendChild(doc.createTextNode(String.valueOf(data.getTotalPlayers())));
    rootElement.appendChild(totalplayers);

    Element memoryused = doc.createElement("memoryused");
    memoryused.appendChild(doc.createTextNode(String.valueOf(data.getMemoryUsed())));
    rootElement.appendChild(memoryused);

    Element maxmemory = doc.createElement("maxmemory");
    maxmemory.appendChild(doc.createTextNode(String.valueOf(data.getMaxMemory())));
    rootElement.appendChild(maxmemory);

    Element jvmversion = doc.createElement("jvmversion");
    jvmversion.appendChild(doc.createTextNode(System.getProperty("java.version")));
    rootElement.appendChild(jvmversion);

    Element osname = doc.createElement("osname");
    osname.appendChild(doc.createTextNode(System.getProperty("os.name")));
    rootElement.appendChild(osname);

    Element osversion = doc.createElement("osversion");
    osversion.appendChild(doc.createTextNode(System.getProperty("os.version")));
    rootElement.appendChild(osversion);

    Element cwd = doc.createElement("cwd");
    cwd.appendChild(doc.createTextNode(System.getProperty("user.dir")));
    rootElement.appendChild(cwd);

    Element bukkitversion = doc.createElement("bukkitversion");
    bukkitversion.appendChild(doc.createTextNode(Bukkit.getBukkitVersion()));
    rootElement.appendChild(bukkitversion);

    //display plugins
    Element plugins = doc.createElement("plugins");
    for (Iterator<Plugin> it = data.getPlugins().iterator(); it.hasNext();) {
      Plugin eachPlugin = it.next();
      Element plugin = doc.createElement("plugin");
      plugin.appendChild(doc.createTextNode(eachPlugin.getName()));
      plugin.setAttribute("enabled", String.valueOf(eachPlugin.isEnabled()));
      plugins.appendChild(plugin);
      InputStream is = eachPlugin.getResource("plugin.yml");
      Yaml yaml = new Yaml();
      Map<String, Object> config = (Map<String, Object>) yaml.load(is);
      plugin.setAttribute("version", config.get("version").toString());
      if (config.containsKey("author")) {
        plugin.setAttribute("author", config.get("author").toString());
      }
      if (config.containsKey("authors")) {
        plugin.setAttribute("author", config.get("authors").toString().replace("[", "").replace("]", ""));
      }
      if (config.containsKey("description")) {
        plugin.setAttribute("description", config.get("description").toString());
      }
      if (config.containsKey("website")) {
        plugin.setAttribute("website", config.get("website").toString());
      }
    }
    rootElement.appendChild(plugins);

    Element players = doc.createElement("players");
    for (Iterator<Player> it = data.getPlayers().iterator(); it.hasNext();) {
      Player eachPlayer = it.next();
      Element player = doc.createElement("player");
      player.setAttribute("world", eachPlayer.getWorld().getName());
      player.setAttribute("ip", eachPlayer.getAddress().getAddress().getHostAddress());
      player.setAttribute("xyz", String.valueOf(eachPlayer.getLocation().getBlockX())
              + "," + String.valueOf(eachPlayer.getLocation().getBlockY())
              + "," + String.valueOf(eachPlayer.getLocation().getBlockZ()));
      player.setAttribute("inhand", eachPlayer.getItemInHand().getType().toString());
      player.setAttribute("allowedflight", String.valueOf(eachPlayer.getAllowFlight()));
      player.setAttribute("op", String.valueOf(eachPlayer.isOp()));
      player.setAttribute("gamemode", eachPlayer.getGameMode().toString());
      player.setAttribute("health", String.valueOf(eachPlayer.getHealth()));
      player.setAttribute("name", eachPlayer.getName());
      player.setAttribute("displayname", eachPlayer.getDisplayName().replaceAll("\u00a7", "&amp;"));
      Element inventory = doc.createElement("inventory");
      PlayerInventory inven = eachPlayer.getInventory();
      for (int i = 0; i < inven.getSize(); i++) {
        if (inven.getItem(i) != null) {
          ItemStack is = inven.getItem(i);
          Element item = doc.createElement("item");
          item.setAttribute("slot", String.valueOf(i));
          item.setAttribute("id", String.valueOf(is.getTypeId()));
          item.setAttribute("name", is.getType().toString());
          item.setAttribute("amount", String.valueOf(is.getAmount()));
          inventory.appendChild(item);
        }
      }
      player.appendChild(inventory);
      players.appendChild(player);
    }
    rootElement.appendChild(players);

    Element worlds = doc.createElement("worlds");
    for (Iterator<World> it = data.getWorlds().iterator(); it.hasNext();) {
      World eachWorld = it.next();
      Element world = doc.createElement("world");
      world.appendChild(doc.createTextNode(eachWorld.getName()));
      world.setAttribute("players", String.valueOf(eachWorld.getPlayers().size()));
      world.setAttribute("time", String.valueOf(eachWorld.getTime()));
      world.setAttribute("type", eachWorld.getWorldType().toString());
      world.setAttribute("difficulty", eachWorld.getDifficulty().toString());
      world.setAttribute("seed", String.valueOf(eachWorld.getSeed()));
      world.setAttribute("entities", String.valueOf(eachWorld.getEntities().size()));
      world.setAttribute("moblimit", String.valueOf(eachWorld.getMonsterSpawnLimit()));
      worlds.appendChild(world);
    }
    rootElement.appendChild(worlds);

    Element tps = doc.createElement("tps");
    tps.appendChild(doc.createTextNode(String.valueOf(data.getTPS())));
    rootElement.appendChild(tps);

    Element heartbeat = doc.createElement("heartbeat");
    heartbeat.appendChild(doc.createTextNode(String.valueOf(lastContactMainThread)));
    heartbeat.setAttribute("ticktime", String.valueOf(MineloadPlugin.getTickTime()));
    rootElement.appendChild(heartbeat);

    double timeTaken = System.currentTimeMillis() - startTime;
    Element time = doc.createElement("generated");
    time.appendChild(doc.createTextNode(String.valueOf(timeTaken)));
    rootElement.appendChild(time);
    try {
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();

      //create string from xml tree
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      DOMSource source = new DOMSource(doc);
      trans.transform(source, result);
      String xmlString = sw.toString();
      xmlData = xmlString;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getXmlData() {
    return xmlData;
  }
}
