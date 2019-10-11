package socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import settings.Code;
import settings.Function;
import settings.Protocol;

public class Server {

    private final int MAX_TIME = 20000;
    private final long ERROR_TIME = 15000;

    /**
     * Method responsible for create the response message
     * @param socket 
     */
    public void createMessage(Socket socket) {
        new Thread(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                Protocol message = new Protocol();

                boolean isFunction = true;
                long delay;

                while (true) {
                    delay = new Random().nextInt(MAX_TIME);

                    Protocol response = (Protocol) in.readObject();
                    System.out.println("Received from client: " + response);

                    int id = (int) response.getParam("ID");

                    if (response.getParam("TYPE").equals(Code.REQ)) {
                        message.setParam("TYPE", Code.ACK);
                        message.setParam("ID", id);

                        out.writeObject(message);
                        out.flush();
                        out.reset();

                        double x = (double) response.getParam("PARAM1");
                        double y = (double) response.getParam("PARAM2");
                        double result = 0;

                        Function f = new Function(x, y);

                        switch (response.getParam("FUNC").toString()) {
                            case "+":
                                result = f.sum();
                                break;
                            case "-":
                                result = f.sub();
                                break;
                            case "*":
                                result = f.mul();
                                break;
                            case "/":
                                double value = (double) response.getParam("PARAM2");
                                if (value == 0) {
                                    System.out.println("é");
                                    isFunction = false;
                                } else {
                                    result = f.div();
                                }
                                break;
                            default:
                                isFunction = false;
                                break;
                        }

                        /**
                         * A delay has been set to simulate a timeout error
                         */
                        if (!isFunction | delay >= ERROR_TIME) {
                            message.setParam("TYPE", Code.TA);
                            message.setParam("ID", id);

                            Thread.sleep(delay);

                            out.writeObject(message);
                            out.flush();
                            out.reset();

                            isFunction = true;
                        } else {
                            message.setParam("TYPE", Code.RES);
                            message.setParam("ID", id);
                            message.setParam("FUNC", response.getParam("FUNC"));
                            message.setParam("PARAM1", x);
                            message.setParam("PARAM2", y);
                            message.setParam("RESULT", result);

                            Thread.sleep(delay);

                            out.writeObject(message);
                            out.flush();
                            out.reset();
                        }
                    }

                    if (response.getParam("TYPE").equals(Code.AYA)) {
                        message.setParam("TYPE", Code.IAA);
                        message.setParam("ID", id);

                        out.writeObject(message);
                        out.flush();
                        out.reset();
                    }

                    if (response.getParam("TYPE").equals(Code.ACK)) {
                        socket.close();
                    }

                    message.clear();
                }
            } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                System.out.println("Client " + socket.getInetAddress() + " disconnected!");
                System.out.println("\nWaiting for connection...");
            }
        }).start();
    }

    /**
     * Init server to listen on port 5000
     * @param args 
     */
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Waiting for connection...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected, IP: " + socket.getInetAddress());
                new Server().createMessage(socket);
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
