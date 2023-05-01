import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * This class represents the client handler. It handles the communication with the client. It reads the file from the
 * server and sends it to the client.
 */
public class ClientHandler extends Thread {

    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Socket client;
    private final boolean isConnected;
    private static final String SECRET_KEY = "G-KaPdSgVkYp3s6v";

    /**
     * Creates a ClientHandler object by specifying the socket to communicate with the client. All the processing is
     * done in a separate thread.
     *
     * @param client the socket to communicate with the client
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public ClientHandler ( Socket client ) throws IOException {
        this.client = client;
        in = new ObjectInputStream ( client.getInputStream ( ) );
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
    }

    @Override
    public void run ( ) {
        super.run ( );
        try {
            while ( isConnected ) {
                // Reads the message to extract the path of the file
                Message message = ( Message ) in.readObject ( );
                String request = new String ( message.getMessage ( ) );
                // Reads the file and sends it to the client
                byte[] content = FileHandler.readFile ( RequestUtils.getAbsoluteFilePath ( request ) );
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                String hmac = ("5v8y/B?E");
                byte [] HmacFile = HMAC.computeHMAC(content, hmac.getBytes(), 64, messageDigest);
                byte [] response = FileEncryption.encryptMessage(content, SECRET_KEY.getBytes());
                System.out.println("Encrypted Message: " + new String(response));
                sendFile ( response );
                sendHMAC(HmacFile);
                sendFile ( content );
            }
            // Close connection
            closeConnection ( );
        } catch ( IOException | ClassNotFoundException e ) {
            // Close connection
            closeConnection ( );
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    private void sendFile ( byte[] content ) throws IOException {
        Message response = new Message ( content );
        out.writeObject ( response );
        out.flush ( );
    }

    private void sendHMAC ( byte[] content ) throws IOException {
        Message Hmac = new Message ( content );
        out.writeObject ( Hmac );
        out.flush ( );
    }


    /**
     * Closes the connection by closing the socket and the streams.
     */
    private void closeConnection ( ) {
        try {
            client.close ( );
            out.close ( );
            in.close ( );
        } catch ( IOException e ) {
            throw new RuntimeException ( e );
        }
    }

}
