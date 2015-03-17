import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

/**
 * Main execution class
 * <p/>
 * Created by Ian on 1/5/2015.
 */
public class Main {

    public static void main(String[] args) {

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
}
