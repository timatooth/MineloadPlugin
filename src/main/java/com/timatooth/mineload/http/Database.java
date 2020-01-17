package com.timatooth.mineload.http;

import com.timatooth.mineload.MineloadPlugin;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Creates connection to DB and handles driver.
 * @author Tim Sullivan
 */
public class Database {

  private Driver driver;
  private Connection con;
  private Properties settings;
  /**
   * Create database object for interacting with H2/MySQL databases.
   */
  public Database() {
    initDriver();
    settings = new Properties();

    String engine = MineloadPlugin.getMineload().getConfig().getString("database.engine");

    try {
      if (engine.equalsIgnoreCase("h2")) {
        con = driver.connect("jdbc:h2:./plugins/MineloadPlugin/mineload", settings);

      } else if (engine.equalsIgnoreCase("mysql")) {
        FileConfiguration config = MineloadPlugin.getMineload().getConfig();
        settings.put("user", config.getString("mysql.username"));
        settings.put("password", config.getString("mysql.password"));
        settings.put("autoReconnect", "true");
        con = driver.connect("jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getInt("mysql.port") + "/mineload", settings);
      }


    } catch (SQLException ex) {
      Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
    }

    initDb();

  }

  /**
   * Check the database and populate tables.
   */
  private void initDb() {
    try {
      con.prepareStatement("CREATE TABLE IF NOT EXISTS ml_users ("
              + "user_id INT PRIMARY KEY AUTO_INCREMENT,"
              + "username VARCHAR(255),"
              + "password VARCHAR(255)"
              + ")").execute();

      con.prepareStatement("CREATE TABLE IF NOT EXISTS ml_sessions ("
              + "session_id VARCHAR(500) PRIMARY KEY,"
              + "ip VARCHAR(15),"
              + "create_time DATETIME"
              + ")").execute();

      con.prepareStatement("CREATE TABLE IF NOT EXISTS ml_session_data ("
              + "data_id int PRIMARY KEY AUTO_INCREMENT,"
              + "session_id VARCHAR(500),"
              + "data_key VARCHAR(255),"
              + "data_value VARCHAR(60000),"
              + "FOREIGN KEY(session_id) REFERENCES ml_sessions(session_id)"
              + ")").execute();

      con.prepareStatement("CREATE TABLE IF NOT EXISTS ml_performance ("
              + "time TIMESTAMP,"
              + "playercount INT,"
              + "memused INT,"
              + "tps INT,"
              + "ticktime BIGINT,"
              + "heartbeat BIGINT,"
              + "tx BIGINT,"
              + "rx BIGINT"
              + ")").execute();
      
      con.prepareStatement("CREATE TABLE IF NOT EXISTS ml_groups ("
              + "group_id INT PRIMARY KEY AUTO_INCREMENT,"
              + "group_name VARCHAR(255),"
              + "jsonapi_user VARCHAR(255),"
              + "jsonapi_password VARCHAR(255)"
              + ")").execute();
      
      con.prepareStatement("CREATE TABLE IF NOT EXISTS ml_options ("
              + "option_id INT PRIMARY KEY AUTO_INCREMENT,"
              + "option_name VARCHAR(255),"
              + "option_value VARCHAR(6000)"
              + ")").execute();

    } catch (SQLException se) {
      se.printStackTrace();
    }
  }
  /**
   * Downloads database driver and loads it into the classpath.
   */
  private void initDriver() {
    File libdir = new File("plugins/MineloadPlugin/lib");
    if (!libdir.exists()) {
      if (!libdir.mkdir()) {
        System.out.println("Mineload plugins/MineloadPlugin/lib was not created. Bad.");
      }
    }
    //what DBMS are we using?
    String engine = MineloadPlugin.getMineload().getConfig().getString("database.engine");
    File lib = null;
    if (engine.equals("h2")) {
      lib = new File(libdir, "h2-1.4.200.jar");
    } else if (engine.equalsIgnoreCase("mysql")) {
      lib = new File(libdir, "mysql-connector-java-5.1.26-bin.jar");
    } else {
      System.out.println("Mineload: INVALID DATABASE ENGINE: " + engine);
    }
    //yeah this bit isn't complicated ae...
    if (!lib.exists()) {
      try {
        System.out.println("Mineload: " + engine + " Database Driver not here. Downloading it. Please be excited...");
        if (engine.equalsIgnoreCase("h2")) {
          URL website = new URL("https://repo1.maven.org/maven2/com/h2database/h2/1.4.200/h2-1.4.200.jar");
          ReadableByteChannel rbc = Channels.newChannel(website.openStream());
          FileOutputStream fos = new FileOutputStream(lib);
          fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        } else if (engine.equalsIgnoreCase("mysql")) {
          //mysql file must be unzipped...
          URL website = new URL("http://cdn.mysql.com/Downloads/Connector-J/mysql-connector-java-5.1.26.zip");
          ReadableByteChannel rbc = Channels.newChannel(website.openStream());
          File tempZipFile = new File("plugins/MineloadPlugin/mysql-connector-java-5.1.26.zip");
          FileOutputStream fos = new FileOutputStream(tempZipFile);
          fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
          System.out.println("Mineload: MySQL mysql-connector-java-5.1.26.zip saved... Extracting...");
          OutputStream out = new FileOutputStream(lib); //want to save to the found jar
          FileInputStream fin = new FileInputStream(tempZipFile);
          BufferedInputStream bin = new BufferedInputStream(fin);
          ZipInputStream zin = new ZipInputStream(bin);
          ZipEntry ze = null;
          //looking for the bin jar.
          while ((ze = zin.getNextEntry()) != null) {
            if (ze.getName().equals("mysql-connector-java-5.1.26-bin.jar")) {
              byte[] buffer = new byte[2000000]; //2MB
              int len;
              while ((len = zin.read(buffer)) != -1) {
                out.write(buffer, 0, len);
              }
              out.close();
              break;
            }
          }
          fin.close();
          tempZipFile.delete();
          System.out.println("Mineload: mysql-connector-java-5.1.26-bin.jar Extracted!");
        }

        System.out.println("Mineload: Hopefully that went OK and now we have a database driver.");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    try {
      ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("jar:file:" + lib + "!/")});
      try {
        if (engine.equalsIgnoreCase("mysql")) {
          driver = (Driver) classLoader.loadClass("com.mysql.jdbc.Driver").newInstance();
        } else if (engine.equalsIgnoreCase("h2")) {
          driver = (Driver) classLoader.loadClass("org.h2.Driver").newInstance();
        }
      } catch (Exception ex) {
        Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
      }
    } catch (MalformedURLException ex) {
      Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
    }

  }
  /**
   * Get active database connection for queries.
   * @return Connection to SQL server.
   */
  public Connection getConnection() {
    return con;
  }
}
