import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

/**
 * Main execution class
 * <p/>
 * Created by Ian on 1/5/2015.
 */
public class Main {

    final public static int VERSION = 2;

    public static void main(String[] args) {

        checkVersion();
        SettingsWindow window = new SettingsWindow();
        setupMenu(window);
        while (true) {
            try {
                Daemon daemon = new Daemon(window.getServerName());
                daemon.run();
                new CommandListener();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void setupMenu(final SettingsWindow window) {
        if (SystemTray.isSupported()) {
            final PopupMenu popupMenu = new PopupMenu();
            try {
                URL url = ClassLoader.getSystemClassLoader().getResource("logo.png");
                final TrayIcon trayIcon = new TrayIcon(new ImageIcon(url).getImage(), "Toggle Server");
                final SystemTray tray = SystemTray.getSystemTray();
                trayIcon.setImageAutoSize(true);
                MenuItem changeNameItem = new MenuItem("Change Server Name");
                changeNameItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        window.changeSettings();
                    }
                });
                MenuItem clearDevicesItem = new MenuItem("Clear Menu Devices");
                clearDevicesItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        (new File("devices.properties")).delete();
                    }
                });
                MenuItem exitItem = new MenuItem("Exit");
                exitItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tray.remove(trayIcon);
                        System.exit(0);
                    }
                });
                popupMenu.add(changeNameItem);
                popupMenu.add(clearDevicesItem);
                popupMenu.add(exitItem);
                trayIcon.setToolTip("Toggle Server");
                trayIcon.displayMessage("Toggle Server", "Running", TrayIcon.MessageType.INFO);
                trayIcon.setPopupMenu(popupMenu);
                tray.add(trayIcon);
            } catch (Exception e) {
                System.out.println("Couldn't create menu");
            }
        }
    }

    private static void checkVersion() {
        try {
            final URI toggleSite = new URI("http://810labs.me/toggle");
            URL url = new URL("http://810labs.me/toggle/version_check");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            int currentVersion = Integer.parseInt(in.readLine());
            if (currentVersion > VERSION) {
                JFrame frame = new JFrame("New Version Available");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setLayout(new FlowLayout());
                JLabel label = new JLabel("Toggle Server Version " + currentVersion + " is available");
                JButton button = new JButton();
                button.setText("Download Here");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(toggleSite);
                            } catch (IOException z) {
                                z.printStackTrace();
                            }
                        }
                        System.exit(0);
                    }
                });
                frame.getContentPane().add(label);
                frame.getContentPane().add(button);
                frame.pack();
                frame.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
