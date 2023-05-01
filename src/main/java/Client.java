import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * This class represents the client. The client sends the messages to the server
 * by means of a socket. The use of Object
 * streams enables the sender to send any kind of object.
 */
public class Client {

    private static final String HOST = "0.0.0.0";
    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final boolean isConnected;
    private final String userDir;
    private static final String SECRET_KEY = "G-KaPdSgVkYp3s6v";

    /**
     * Constructs a Client object by specifying the port to connect to. The socket
     * must be created before the sender can
     * send a message.
     *
     * @param port the port to connect to
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public Client(int port) throws IOException {
        client = new Socket(HOST, port);
        out = new ObjectOutputStream(client.getOutputStream());
        in = new ObjectInputStream(client.getInputStream());
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
        // Create a temporary directory for putting the request files
        userDir = Files.createTempDirectory("fileServer").toFile().getAbsolutePath();
        System.out.println("Temporary directory path " + userDir);
    }

    /**
     * Executes the client. It reads the file from the console and sends it to the
     * server. It waits for the response and
     * writes the file to the temporary directory.
     * @throws Exception
     */
    public void execute() throws Exception {
        Scanner usrInput = new Scanner(System.in);
        try {
            while (isConnected) {
                // Reads the message to extract the path of the file
                System.out.println("Write the path of the file");
                String request = usrInput.nextLine();
                // Request the file
                sendMessage(request);
                // Waits for the response
                if (verify_HMAC()) {
                    processResponse(RequestUtils.getFileNameFromRequest(request));
                } else {
                    System.out.println("File has been modified");
                }
            }
            // Close connection
            closeConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Close connection
        closeConnection();
    }

    /**
     * Reads the response from the server and writes the file to the temporary
     * directory.
     *
     * @param fileName the name of the file to write
     */

    private boolean verify_HMAC() throws Exception {
        try {
            Message response = (Message) in.readObject();
            Message Hmac = (Message) in.readObject();
            byte [] decryptedMessage = FileEncryption.decryptMessage(response.getMessage(), SECRET_KEY.getBytes());
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            String hmac = ("5v8y/B?E");
            byte[] message_enc = HMAC.computeHMAC(decryptedMessage, hmac.getBytes(), 64, messageDigest);
            System.out.println("Server HMAC: " + new String(Hmac.getMessage()));
            System.out.println("Client HMAC: " + new String(message_enc));
            if (Arrays.equals(Hmac.getMessage(), message_enc)) {
                System.out.println("File is not modified");
                System.out.println("Output File :");
                System.out.println(new String(decryptedMessage));
                return true;
            } else {
                return false;
            }
        } catch (ClassNotFoundException | IOException | NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    private void processResponse(String fileName) {
        try {
            Message response = (Message) in.readObject();
            System.out.println("File received");
            FileHandler.writeFile(userDir + "/" + fileName, response.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the path of the file to the server using the OutputStream of the
     * socket. The message is sent as an object
     * of the {@link Message} class.
     *
     * @param filePath the message to send
     *
     * @throws IOException when an I/O error occurs when sending the message
     */
    public void sendMessage(String filePath) throws IOException {
        // Creates the message object
        Message messageObj = new Message(filePath.getBytes());
        // Sends the message
        out.writeObject(messageObj);
        out.flush();
    }

    /**
     * Closes the connection by closing the socket and the streams.
     */
    private void closeConnection() {
        try {
            client.close();
            out.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
