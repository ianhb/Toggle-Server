import javax.swing.*;
import java.io.*;
import java.util.Properties;

/**
 * Settings menu to be expanded in the future but presently allows the user to customize the server name
 * <p/>
 * Created by Ian on 1/17/2015.
 */
public class SettingsWindow {
    public static final String serverNamePreferences = "preference_server_name";

    String serverName;
    Properties prop;
    OutputStream output;
    InputStream input;

    public SettingsWindow() {
        prop = new Properties();
        output = null;
        input = null;
        System.out.println("Starting Settings");
        try {
            if (!new File("config.properties").exists()) {
                output = new FileOutputStream("config.properties");
                prop.setProperty(serverNamePreferences, "");
                prop.store(output, null);
            }
            input = new FileInputStream("config.properties");
            prop.load(input);
            serverName = prop.getProperty(serverNamePreferences);
            if (serverName.equals("")) {
                serverName = (String) JOptionPane.showInputDialog(null, "Server Name: ", null, JOptionPane.PLAIN_MESSAGE, null, null, serverName);
                try {
                    output = new FileOutputStream("config.properties");
                    prop.setProperty(serverNamePreferences, serverName);
                    prop.store(output, null);
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void changeSettings() {
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);
            serverName = prop.getProperty(serverNamePreferences);
            serverName = (String) JOptionPane.showInputDialog(null, "Server Name: ", null, JOptionPane.PLAIN_MESSAGE, null, null, serverName);
            output = new FileOutputStream("config.properties");
            prop.setProperty(serverNamePreferences, serverName);
            prop.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getServerName() {
        return serverName;
    }
}
