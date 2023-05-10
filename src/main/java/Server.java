import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * This class represents a server that receives a message from the clients. The server is implemented as a thread. Each
 * time a client connects to the server, a new thread is created to handle the communication with the client.
 */
public class Server implements Runnable {

    /**
     * The path to the directory where the files are stored
     */
    public static final String FILE_PATH = "server/files";
    /**
     * The server socket
     */
    private final ServerSocket server;
    /**
     * Indicates if the server is connected
     */
    private final boolean isConnected;
    /**
     * The private RSA key
     */
    private final PrivateKey privateRSAKey;
    /**
     * The public RSA key
     */
    private final PublicKey publicRSAKey;

    /**
     * Constructs a Server object by specifying the port number. The server will be then created on the specified port.
     * The server will be accepting connections from all local addresses.
     *
     * @param port the port number
     *
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public Server ( int port ) throws Exception {
        server = new ServerSocket ( port );
        KeyPair keyPair = Encryption.generateKeyPair ( );
        this.privateRSAKey = keyPair.getPrivate ( );
        this.publicRSAKey = keyPair.getPublic ( );
        this.isConnected = true;
    }

    @Override
    public void run ( ) {
        try {
            while ( isConnected ) {
                Socket client = server.accept ( );
                // Process the request
                process ( client );
            }
            closeConnection ( );
        } catch ( Exception e ) {
            throw new RuntimeException ( e );
        }
    }

    /**
     * Processes the request from the client.
     *
     * @throws IOException if an I/O error occurs when reading stream header
     */
    private void process ( Socket client ) throws IOException, NoSuchAlgorithmException {
        ClientHandler clientHandler = new ClientHandler ( client , getPrivateRSAKey(), getPublicRSAKey());
        clientHandler.start ( );
    }

    /**
     * Closes the connection and the associated streams.
     */
    private void closeConnection ( ) {
        try {
            server.close ( );
        } catch ( IOException e ) {
            throw new RuntimeException ( e );
        }
    }

    /**
     * Get the private RSA key
     *
     * @return
     */
    public PrivateKey getPrivateRSAKey() {
        return privateRSAKey;
    }

    /**
     * Get the public RSA key
     *
     * @return
     */
    public PublicKey getPublicRSAKey() {
        return publicRSAKey;
    }
}