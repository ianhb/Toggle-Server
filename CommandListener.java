import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
            run = true;
            deviceConnected = false;
            try {
                bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                bufferIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                System.out.println("Connection Initialized");
                System.out.println("Starting to monitor socket");
                while (run) {
                    System.out.println("Waiting for command");
                    if (!deviceConnected) {
                        serverMessage = bufferIn.readLine();
                        if (login(serverMessage)) {
                            deviceConnected = true;
                            System.out.println("Logged In as " + deviceID);
                        }
                    } else {
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
                }
                clientSocket.close();
                System.out.println("Restarting Daemon");
            } catch (Exception e) {
                e.printStackTrace();
                run = false;
            }
        }

        private boolean login(String string) {
            if (getCommand(string).equals(LOGIN)) {
                deviceID = getMessage(string);
                return true;
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

