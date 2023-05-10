import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class represents the client handler. It handles the communication with the client. It reads the file from the
 * server and sends it to the client.
 */
public class ClientHandler extends Thread {

    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Socket client;
    private final boolean isConnected;
    /**
     * The private RSA key
     */
    private final PrivateKey privateRSAKey;
    /**
     * The public RSA key.
     */
    private final PublicKey publicRSAKey;
    /**
     * The message digest algorithm.
     */
    private static MessageDigest messageDigest;
    /**
     * The maximum size of a packet.
     */
    private final int MAX_SIZE = 1024;
    /**
     * The list of packets.
     */
    private static final ArrayList<byte[]> pacotes = new ArrayList<byte[]>();


    /**
     * Creates a ClientHandler object by specifying the socket to communicate with the client. All the processing is
     * done in a separate thread.
     *
     * @param client the socket to communicate with the client
     * @param privateRSAKey the private RSA key
     * @param publicRSAKey the public RSA key
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public ClientHandler (Socket client , PrivateKey privateRSAKey, PublicKey publicRSAKey ) throws IOException, NoSuchAlgorithmException {
        this.client = client;
        in = new ObjectInputStream ( client.getInputStream ( ) );
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        this.privateRSAKey = privateRSAKey;
        this.publicRSAKey = publicRSAKey;
        messageDigest = MessageDigest.getInstance ( "SHA-256" );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
    }

    @Override
    public void run ( ) {
        super.run ( );
        try {
            // Perform key distribution
            PublicKey senderPublicRSAKey = rsaKeyDistribution ( in );
            // Agree on a shared secret
            BigInteger sharedSecret = agreeOnSharedSecret ( senderPublicRSAKey );
            while ( isConnected ) {
                // Reads the message to extract the path of the file
                Message message = ( Message ) in.readObject ( );
                byte[] decryptedMessage = Encryption.decryptMessage ( message.getMessage ( ) , sharedSecret.toByteArray ( ) );
                //check integrity
                if(!Arrays.equals (message.getSignature(),HMAC.computeHMAC(decryptedMessage,sharedSecret.toByteArray(),256,messageDigest))){
                    throw new RuntimeException ( "The integrity of the message is not verified" );
                }else if(Arrays.equals(decryptedMessage,"handshake".getBytes())){
                    sharedSecret = agreeOnSharedSecret ( senderPublicRSAKey );
                }else {
                    String request = new String(decryptedMessage);
                    // Reads the file and sends it to the client
                    byte[] content = "".getBytes ( );
                    try {
                        content = FileHandler.readFile(RequestUtils.getAbsoluteFilePath(request));
                    } catch (IOException e) {
                        System.out.println("ERROR - The file does not exist");
                        content = "ERROR - The file does not exist".getBytes();
                    } catch (IllegalArgumentException e) {
                        System.out.println("ERROR - Invalid prompt");
                    }
                    sendFile(content, sharedSecret);
                }
            }
            // Close connection
            closeConnection ( );
        } catch (Exception e ) {
            // Close connection
            closeConnection ( );
        }
    }

    /**
     * Sends the file to client in packets
     *
     * @param content the content of the file to send
     * @param sharedSecret the shared secret
     *
     * @throws IOException when an I/O error occurs when sending the file
     */
    private void sendFile ( byte[] content , BigInteger sharedSecret) throws Exception {
        int numPacotes = (content.length + MAX_SIZE - 1) / MAX_SIZE; //calcula numero de pacotes
        for (int i = 0; i < numPacotes; i++) {
            int outtu = i * MAX_SIZE; //intervalos de cada conteudo. 0-1024-2048...
            int compri = Math.min(MAX_SIZE, content.length - outtu); //tamanho de cada pacote
            byte[] pacote = new byte[compri];
            System.arraycopy(content, outtu, pacote, 0, compri);
            byte[] digest = HMAC.computeHMAC(pacote,sharedSecret.toByteArray(),256,messageDigest);
            byte[] encryptedMessage = Encryption.encryptMessage(pacote, sharedSecret.toByteArray());

            boolean isLast=(i==numPacotes-1);

            Message  response = new Message(encryptedMessage, digest, i+1, numPacotes, isLast);

            out.writeObject(response);
            out.flush();

            pacotes.add(encryptedMessage); //usado depois no teste sendFile
        }
    }

    /**
     * Closes the connection by closing the socket and the streams.
     *
     * @throws RuntimeException when an I/O error occurs when closing the connection
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

    /**
     * Executes the key distribution protocol. The receiver will receive the public key of the sender and will send its
     * own public key.
     *
     * @param in the input stream
     *
     * @return the public key of the sender
     *
     * @throws Exception when the key distribution protocol fails
     */
    private PublicKey rsaKeyDistribution ( ObjectInputStream in ) throws Exception {
        // Extract the public key
        PublicKey senderPublicRSAKey = ( PublicKey ) in.readObject ( );
        // Send the public key
        sendPublicRSAKey ( );
        return senderPublicRSAKey;
    }

    /**
     * Sends the public key of the receiver to the sender.
     *
     * @throws IOException when an I/O error occurs when sending the public key
     */
    private void sendPublicRSAKey ( ) throws IOException {
        out.writeObject ( publicRSAKey );
        out.flush ( );
    }

    /**
     * Performs the Diffie-Hellman algorithm to agree on a shared private key.
     *
     * @param senderPublicRSAKey the public key of the sender
     *
     * @return the shared secret key
     *
     * @throws Exception when the key agreement protocol fails
     */
    private BigInteger agreeOnSharedSecret ( PublicKey senderPublicRSAKey ) throws Exception {
        // Generate a pair of keys
        BigInteger privateKey = DiffieHellman.generatePrivateKey ( );
        BigInteger publicKey = DiffieHellman.generatePublicKey ( privateKey );
        // Extracts the public key from the request
        BigInteger clientPublicKey = new BigInteger ( Encryption.decryptRSA ( ( byte[] ) in.readObject ( ) , senderPublicRSAKey ) );
        // Send the public key to the client
        sendPublicDHKey ( publicKey );
        // Generates the shared secret
        return DiffieHellman.computePrivateKey ( clientPublicKey , privateKey );
    }

    /**
     * Sends the public key to the sender.
     *
     * @param publicKey the public key to be sent
     *
     * @throws Exception when the public key cannot be sent
     */
    private void sendPublicDHKey ( BigInteger publicKey ) throws Exception {
        out.writeObject ( Encryption.encryptRSA ( publicKey.toByteArray ( ) , this.privateRSAKey ) );
    }

}
