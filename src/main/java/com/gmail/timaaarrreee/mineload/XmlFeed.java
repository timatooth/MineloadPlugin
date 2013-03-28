package com.gmail.timaaarrreee.mineload;

import java.io.StringWriter;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bukkit.Bukkit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
      Bukkit.getLogger().log(Level.SEVERE, "Couldn't load the DOM builder.");
      ex.printStackTrace();
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

    Element tps = doc.createElement("tps");
    tps.appendChild(doc.createTextNode(String.valueOf(data.getTPS())));
    rootElement.appendChild(tps);

    Element heartbeat = doc.createElement("heartbeat");
    heartbeat.appendChild(doc.createTextNode(String.valueOf(lastContactMainThread)));
    heartbeat.setAttribute("ticktime", String.valueOf(MineloadPlugin.getTickTime()));
    rootElement.appendChild(heartbeat);

    Element tx = doc.createElement("tx");
    tx.appendChild(doc.createTextNode(String.valueOf(data.getNetwork().getTx())));
    rootElement.appendChild(tx);

    Element rx = doc.createElement("rx");
    rx.appendChild(doc.createTextNode(String.valueOf(data.getNetwork().getRx())));
    rootElement.appendChild(rx);

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
      Bukkit.getLogger().log(Level.SEVERE, "Couldn't generate mineload xml structure.");
      e.printStackTrace();
    }
  }

  public String getXmlData() {
    return xmlData;
  }
}
