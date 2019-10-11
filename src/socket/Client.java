package socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import settings.Code;
import settings.Protocol;

public class Client {

    private long initTime;
    private long endTime;
    private boolean exit;
    private final int AYA_TIME = 10000;

    public void getTime() {
        new Thread(() -> {
            while (!exit) {
                initTime++;

                if (initTime >= endTime) {
                    exit = true;
                }
            }
        }).start();
    }

    /**
     * Method responsible for create a message according to the specified protocol
     * @param socket 
     */
    public void createMessage(Socket socket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Scanner input = new Scanner(System.in);
            Protocol message = new Protocol();

            message.setParam("TYPE", Code.REQ);

            System.out.print("ID: ");
            int id = input.nextInt();
            message.setParam("ID", id);

            System.out.print("FUNC ( + - * / ): ");
            message.setParam("FUNC", new Scanner(System.in).nextLine());

            System.out.print("PARAM1: ");
            message.setParam("PARAM1", input.nextDouble());

            System.out.print("PARAM2: ");
            message.setParam("PARAM2", input.nextDouble());

            out.writeObject(message);
            out.flush();
            out.reset();

            message.clear();

            initTime = System.currentTimeMillis();
            endTime = initTime + AYA_TIME;
            this.getTime();

            /**
             * Print the server response messages
             */
            while (true) {
                Protocol response = (Protocol) in.readObject();
                System.out.println("Received from server: " + response);

                if (response.getParam("TYPE").equals(Code.RES)) {
                    message.clear();
                    message.setParam("TYPE", Code.ACK);
                    message.setParam("ID", id);

                    out.writeObject(message);
                    out.flush();
                    out.reset();
                }

                if (response.getParam("TYPE").equals(Code.TA)) {
                    message.clear();
                    message.setParam("TYPE", Code.REQ);
                    message.setParam("ID", id);

                    System.out.print("FUNC ( + - * / ): ");
                    message.setParam("FUNC", new Scanner(System.in).nextLine());

                    System.out.print("PARAM1: ");
                    message.setParam("PARAM1", input.nextDouble());

                    System.out.print("PARAM2");
                    message.setParam("PARAM2", input.nextDouble());

                    out.writeObject(message);
                    out.flush();
                    out.reset();
                }

                /**
                 * If the client does not receive the response within a certain
                 * time, ask the server if it is alive
                 */
                if (initTime >= endTime) {
                    message.clear();
                    message.setParam("TYPE", Code.AYA);
                    message.setParam("ID", id);

                    out.writeObject(message);
                    out.flush();
                    out.reset();

                    initTime = System.currentTimeMillis();
                    endTime = initTime + AYA_TIME;
                }
            }

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("\nConnection closed!");
        }
    }

    /**
     * Init client to connect on port 5000
     * @param args 
     */
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000)) {
            new Client().createMessage(socket);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
