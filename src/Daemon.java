import java.io.FileInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Properties;

/**
 * Network Discovery daemon that listens for client requests and handles pairing with the device
 * <p/>
 * Created by Ian on 1/5/2015.
 */
public class Daemon {
    public static final int UDPSOCKET = 4567;
    public static final String REQUEST = "DISCOVER_REMOTESERVER_REQUEST";
    public static final String RESPONSE = "DISCOVER_REMOTESERVER_RESPONSE";

    public String ip;
    public String serverName;

    public Daemon(String servername) {
        serverName = servername;
    }

    public String run() {
        DatagramSocket socket;
        boolean awaitingPair;

        try {
            socket = new DatagramSocket(UDPSOCKET, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                boolean requestSeen = false;
                byte[] receiveBuffer;
                DatagramPacket recPacket;
                String message;
                while (!requestSeen) {
                    InputStream input = new FileInputStream("config.properties");
                    Properties prop = new Properties();
                    prop.load(input);
                    serverName = prop.getProperty(SettingsWindow.serverNamePreferences);
                    input.close();
                    System.out.println("Ready to receive broadcasts");

                    receiveBuffer = new byte[15000];
                    recPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(recPacket);

                    System.out.println("Received packet from: " + recPacket.getAddress());
                    message = new String(recPacket.getData()).trim();
                    System.out.println("Packet message: " + message);

                    if (message.equals(REQUEST)) {
                        byte[] sendData = (RESPONSE + ":" + serverName).getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, recPacket.getAddress(), recPacket.getPort());
                        socket.send(sendPacket);
                        System.out.println("Response packet sent to: " + recPacket.getAddress() + " Message: " + new String(sendData));
                        requestSeen = true;
                    }
                }
                awaitingPair = true;
                while (awaitingPair) {
                    try {
                        System.out.println("Awaiting Pairing Response");

                        receiveBuffer = new byte[15000];
                        recPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(recPacket);

                        System.out.println("Received packet from: " + recPacket.getAddress());
                        message = new String(recPacket.getData()).trim();
                        System.out.println("Packet message: " + message);

                        if (message.equals("PAIR")) {
                            socket.close();
                            System.out.println("Paired with device at: " + recPacket.getAddress().toString());
                            ip = recPacket.getAddress().toString();
                            return ip;
                        } else if (message.equals("NOPAIR")) {
                            awaitingPair = false;
                        }
                    } catch (SocketTimeoutException e) {
                        awaitingPair = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
