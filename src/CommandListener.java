import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Handles the TCP connection to the client
 * <p/>
 * Created by Ian on 12/25/2014.
 */
public class CommandListener {

    public static final String CLOSE_CONNECTION = "CLOSE";
    public static final String LOGIN = "LOGIN";

    public static final int SOCKET = 8745;

    public static final String ACKNOWLEDGE = "ACK:";

    SystemController controller;

    public CommandListener() {
        try {
            System.out.println("Controller starting");
            controller = new SystemController();
            System.out.println("Opening Sockets");
            ServerSocket serverSocket = new ServerSocket(SOCKET);
            serverSocket.setSoTimeout(2000);
            Socket socket = serverSocket.accept();
            System.out.println("Sockets Opened");
            System.out.println("Starting Connection");
            new Connection(socket);
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Connection extends Thread {

        Socket clientSocket;
        PrintWriter bufferOut;
        BufferedReader bufferIn;
        boolean run;
        boolean deviceConnected;
        String deviceID;

        public Connection(Socket client) {
            clientSocket = client;
            this.start();
        }

        public void run() {

            String serverMessage;

            deviceConnected = false;
            try {
                bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                bufferIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                System.out.println("Connection Initialized");
                System.out.println("Starting to monitor socket");
                serverMessage = bufferIn.readLine();
                System.out.println("Received Login Request " + serverMessage);
                if (login(serverMessage)) {
                    deviceConnected = true;
                    run = true;
                    System.out.println("Logged In as " + deviceID);
                } else {
                    deviceConnected = false;
                    run = false;
                    System.out.println("Login Rejected");
                }
                while (run) {
                    System.out.println("Waiting for command");
                    serverMessage = bufferIn.readLine();
                    System.out.println("Received Message: " + serverMessage);
                    if (getCommand(serverMessage).equals(CLOSE_CONNECTION)) {
                        run = false;
                        System.out.println("Closing Connection");
                        clientSocket.close();
                    } else {
                        bufferOut.println(ACKNOWLEDGE + serverMessage);
                        bufferOut.flush();
                        System.out.println("Sent response: " + ACKNOWLEDGE + serverMessage);
                        String affirm = bufferIn.readLine();
                        System.out.println("Received Response: " + affirm);
                        if (affirm.equals(ACKNOWLEDGE)) {
                            System.out.println("Executing command: " + getCommand(serverMessage) + " with message: " + getMessage(serverMessage));
                            controller.executeCommand(getCommand(serverMessage), getMessage(serverMessage));
                        }
                    }
                }
                clientSocket.close();
                System.out.println("Restarting Daemon");
            } catch (Exception e) {
                e.printStackTrace();
                run = false;
            }
        }

        private boolean login(String string) {
            final String APPROVEDDEVICES = "approved_devices";
            if (getCommand(string).equals(LOGIN)) {
                System.out.println("Valid Login Request");
                try {
                    Properties prop = new Properties();
                    Gson gson = new Gson();
                    String deviceId = getCommand(getMessage(string));
                    String deviceName = getMessage(getMessage(string));
                    ArrayList<String> approvedDevices;
                    try {
                        InputStream propInStream = new FileInputStream("devices.properties");
                        prop.load(propInStream);
                        approvedDevices = gson.fromJson(prop.getProperty(APPROVEDDEVICES), new TypeToken<ArrayList<String>>() {
                        }.getType());
                        if (approvedDevices == null) {
                            approvedDevices = new ArrayList<>();
                        }
                        propInStream.close();
                    } catch (IOException e) {
                        approvedDevices = new ArrayList<>();
                    }
                    if (approvedDevices.contains(deviceId)) {
                        System.out.println(deviceName + " Connected and Approved");
                        bufferOut.println("ACCEPT");
                        bufferOut.flush();
                        deviceID = deviceId;
                        return true;
                    } else {
                        int reply = JOptionPane.showConfirmDialog(null, "Approve Device: " + deviceName + "?", "New Device Attempting to Connect", JOptionPane.YES_NO_OPTION);
                        if (reply == JOptionPane.YES_OPTION) {
                            System.out.println(deviceName + " Connected and Approved");
                            approvedDevices.add(deviceId);
                            bufferOut.println("ACCEPT");
                            bufferOut.flush();
                            deviceID = deviceId;
                            prop.setProperty(APPROVEDDEVICES, gson.toJson(approvedDevices));
                            OutputStream os = new FileOutputStream("devices.properties");
                            prop.store(os, null);
                            os.close();
                            return true;
                        } else {
                            System.out.println(deviceName + " Rejected");
                            bufferOut.println("REJECT");
                            bufferOut.flush();
                            return false;
                        }
                    }
                } catch (IOException e) {
                    return false;
                }
            } else {
                return false;
            }
        }

        private String getCommand(String message) {
            return message.substring(0, message.indexOf(":"));
        }

        private String getMessage(String message) {
            return message.substring(message.indexOf(":") + 1);
        }

    }
}

