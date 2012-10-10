package com.gmail.timaaarrreee.mineload;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
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

  private DataCollector data;
  private String xmlData;

  public XmlFeed(DataCollector dc) {
    long startTime = System.currentTimeMillis();
    data = dc;
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
    for (Plugin s : data.getPlugins()) {
      Element plugin = doc.createElement("plugin");
      plugin.appendChild(doc.createTextNode(s.getName()));
      plugin.setAttribute("enabled", String.valueOf(s.isEnabled()));
      plugins.appendChild(plugin);
      InputStream is = s.getResource("plugin.yml");
      Yaml yaml = new Yaml();

      Map<String, Object> config = (Map<String, Object>) yaml.load(is);
      plugin.setAttribute("version", config.get("version").toString());
      if(config.containsKey("author")){
        plugin.setAttribute("author", config.get("author").toString());
      }if(config.containsKey("authors")){
        plugin.setAttribute("author", config.get("authors").toString().replace("[", "").replace("]", ""));
      }if(config.containsKey("description")){
        plugin.setAttribute("description", config.get("description").toString());
      }if(config.containsKey("website")){
        plugin.setAttribute("website", config.get("website").toString());
      }
    }
    rootElement.appendChild(plugins);

    Element players = doc.createElement("players");
    for (Player s : data.getPlayers()) {
      Element player = doc.createElement("player");
      //player.appendChild(doc.createTextNode(s.getName()));
      player.setAttribute("world", s.getWorld().getName());
      player.setAttribute("ip", s.getAddress().getAddress().getHostAddress());

      player.setAttribute("xyz", String.valueOf(s.getLocation().getBlockX())
              + "," + String.valueOf(s.getLocation().getBlockY())
              + "," + String.valueOf(s.getLocation().getBlockZ()));
      player.setAttribute("inhand", s.getItemInHand().getType().toString());
      player.setAttribute("allowedflight", String.valueOf(s.getAllowFlight()));
      player.setAttribute("op", String.valueOf(s.isOp()));
      player.setAttribute("gamemode", s.getGameMode().toString());
      player.setAttribute("health", String.valueOf(s.getHealth()));
      player.setAttribute("name", s.getName());
      player.setAttribute("displayname", s.getDisplayName());

      Element inventory = doc.createElement("inventory");
      PlayerInventory inven = s.getInventory();
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
    for (World s : data.getWorlds()) {
      Element world = doc.createElement("world");
      world.appendChild(doc.createTextNode(s.getName()));
      world.setAttribute("time", String.valueOf(s.getTime()));
      world.setAttribute("type", s.getWorldType().toString());
      world.setAttribute("difficulty", s.getDifficulty().toString());
      world.setAttribute("seed", String.valueOf(s.getSeed()));
      world.setAttribute("entities", String.valueOf(s.getEntities().size()));
      world.setAttribute("moblimit", String.valueOf(s.getMonsterSpawnLimit()));


      worlds.appendChild(world);
    }
    rootElement.appendChild(worlds);

    Element tps = doc.createElement("tps");
    tps.appendChild(doc.createTextNode(String.valueOf(data.getTPS())));
    rootElement.appendChild(tps);
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
