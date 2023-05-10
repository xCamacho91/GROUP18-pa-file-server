import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;
import java.util.*;

/**
 * This class represents the client. The client sends the messages to the server by means of a socket. The use of Object
 * streams enables the sender to send any kind of object.
 */
public class Client {

    private static final String HOST = "0.0.0.0";
    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final boolean isConnected;
    /**
     * The directory of the public keys
     */
    private final String pkiDir = System.getProperty("user.dir") + "/pki/public_keys/";
    /**
     * The directory of the user
     */
    private static String userDir;
    /**
     * The username of the client
     */
    private final String userName;
    /**
     * The maximum size of a packet.
     */
    public static int requestsMade = 0; //number of requests
    /**
     * The maximum number of requests before a new handshake
     */
    private final int MAX_REQUESTS = 5; //max of requests before new handshake
    /**
     * The public RSA key.
     */
    private static PublicKey publicRSAKey;
    /**
     * The private RSA key
     */
    private static PrivateKey privateRSAKey;
    /**
     * The public RSA key of the receiver
     */
    private static PublicKey receiverPublicRSAKey;
    /**
     * The shared secret
     */
    private static BigInteger sharedSecret;
    /**
     * The message digest algorithm.
     */
    private static MessageDigest messageDigest;

    /**
     * Constructs a Client object by specifying the port to connect to. The socket must be created before the sender can
     * send a message.
     *
     * @param port the port to connect to
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public Client ( int port, String userName ) throws Exception {
        client = new Socket ( HOST , port );
        this.userName = userName.trim();
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        in = new ObjectInputStream ( client.getInputStream ( ) );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
        // Create a temporary directory for putting the request files

        KeyPair keyPair = Encryption.generateKeyPair ( );
        privateRSAKey = keyPair.getPrivate ( );
        publicRSAKey = keyPair.getPublic ( );
        receiverPublicRSAKey = rsaKeyDistribution ( );
        sharedSecret = agreeOnSharedSecret ( receiverPublicRSAKey );
        messageDigest = MessageDigest.getInstance ( "SHA-256" );
        validateDetailsUser();
    }

    /**
     *  Creates the files for the user and validates the user
     *
     * @throws Exception if the user is not valid
     */
    private void validateDetailsUser() throws Exception {
        userDir = FileManager.validateFile(userName);
        FileManager.createFile( pkiDir, this.userName + "PuK.key", publicRSAKey.toString());
        FileManager.createFile( userDir + "/../", "private.txt", privateRSAKey.toString());

        requestsMade=FileManager.getConfigFile("config/" +userName + ".txt");
    }


    /**
     * Executes the client. It reads the file from the console and sends it to the server. It waits for the response and
     * writes the file to the temporary directory.
     *
     * @throws Exception if an error occurs when sending the message or when writing the file
     */
    public void execute ( ) {
        Scanner usrInput = new Scanner ( System.in );
        try {
            while ( isConnected ) {
                FileManager.saveConfigFile(this.userName, requestsMade);
                checkRequest();
                System.out.println(sharedSecret);
                System.out.println("Request number: "+ requestsMade);
                // Reads the message to extract the path of the file
                System.out.println ( "Write the path of the file" );
                String request = usrInput.nextLine ( );
                // Request the file
                sendMessage ( request , sharedSecret);
                // Waits for the response

                try {
                    processResponse ( RequestUtils.getFileNameFromRequest ( request ) , sharedSecret);
                } catch (IllegalArgumentException e ) {
                    System.out.println("ERROR - FORMAT IS INVALID");
                }
            }
            closeConnection ( );
        } catch (Exception e ) {
            throw new RuntimeException ( e );
        }
        // Close connection
        closeConnection ( );
    }

    /**
     * Checks if the client has no more requests to make
     */
    public void checkRequest () {
        if (requestsMade+1 >= MAX_REQUESTS) {
            System.out.println("Reached 5 requests, making new handshake");
            try {
                sendMessage("handshake",sharedSecret);
                sharedSecret = agreeOnSharedSecret(receiverPublicRSAKey);
            }
             catch(Exception e){
                    System.out.println("Cannot generate new handshake");
             }
            requestsMade = 0;
        } else {
            requestsMade++;
        }
    }

    /**
     * Reads the response from the server and writes the file to the temporary directory.
     *
     * @param fileName the name of the file to write
     * @param sharedSecret symmetric key to decrypt message
     *
     * throws IOException
     */
    private void processResponse ( String fileName , BigInteger sharedSecret) {
        try {
            List<byte[]> listaPacotes = new ArrayList<>();
            int expectedPackets = -1;
            int receivedPackets = 0;

            while (true) {
                Message response = (Message) in.readObject();
                if (response.getMessageNumber() == 1) { //verifica se é a 1 mensagem, se for guarda o total de mensagens
                    expectedPackets = response.getTotalMessages();
                }
                byte[] decryptedMessage = Encryption.decryptMessage(response.getMessage(), sharedSecret.toByteArray());
                if(!Integrity.verifyDigest(response.getSignature(),HMAC.computeHMAC(decryptedMessage,sharedSecret.toByteArray(),256,messageDigest))) {
                    throw new RuntimeException("The integrity of the message is not verified");
                }
                listaPacotes.add(decryptedMessage);
                receivedPackets++;
                if (receivedPackets == expectedPackets) {
                    System.out.println("File received");
                    byte[] content = concatPacks(listaPacotes);

                    FileHandler.writeFile(userDir + "/" + fileName, content);
                    System.out.println( FileManager.displayFile(userDir + "/" + fileName) );
                    break;
                }
            }
        } catch (Exception e ) {
            System.out.println ( "ERROR - FILE NOT FOUND" );
        }
    }

    /**
     * Join each package to the array,
     * @param listaPacotes
     * @return
     * @throws IOException
     */
    private byte[] concatPacks(List<byte[]> listaPacotes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] pack : listaPacotes) {
            outputStream.write(pack);
        }
        return outputStream.toByteArray();
    }

    /**
     * Sends the path of the file to the server using the OutputStream of the socket. The message is sent as an object
     * of the {@link Message} class.
     *
     * @param filePath the message to send
     * @param sharedSecret symmetric key to encrypt message
     *
     * @throws IOException when an I/O error occurs when sending the message
     */
    public void sendMessage ( String filePath , BigInteger sharedSecret) throws Exception {
        byte[] encryptedMessage = Encryption.encryptMessage ( filePath.getBytes ( ) , sharedSecret.toByteArray ( ) );
        byte[] digest = HMAC.computeHMAC(filePath.getBytes(),sharedSecret.toByteArray(),256,messageDigest);
        // Creates the message object
        Message messageObj = new Message ( encryptedMessage , digest, 0, 0, false);
        // Sends the message
        out.writeObject ( messageObj );
        out.flush ( );
    }

    /**
     * Closes the connection by closing the socket and the streams.
     */
    public void closeConnection ( ) {
        try {
            client.close ( );
            out.close ( );
            in.close ( );
        } catch ( IOException e ) {
            throw new RuntimeException ( e );
        }
    }

    /**
     * Performs the Diffie-Hellman algorithm to agree on a shared private key.
     *
     * @param receiverPublicRSAKey the public key of the receiver
     *
     * @return the shared private key
     *
     * @throws Exception when the Diffie-Hellman algorithm fails
     */
    private BigInteger agreeOnSharedSecret (PublicKey receiverPublicRSAKey ) throws Exception {
        // Generates a private key
        BigInteger privateDHKey = DiffieHellman.generatePrivateKey ( );
        BigInteger publicDHKey = DiffieHellman.generatePublicKey ( privateDHKey );
        // Sends the public key to the server encrypted
        sendPublicDHKey ( Encryption.encryptRSA ( publicDHKey.toByteArray ( ) , privateRSAKey ) );
        // Waits for the server to send his public key
        BigInteger serverPublicKey = new BigInteger ( Encryption.decryptRSA ( ( byte[] ) in.readObject ( ) , receiverPublicRSAKey ) );
        // Generates the shared secret
        return DiffieHellman.computePrivateKey ( serverPublicKey , privateDHKey );
    }

    /**
     * Sends the public key to the receiver.
     *
     * @param publicKey the public key to send
     *
     * @throws Exception when the public key cannot be sent
     */
    private void sendPublicDHKey ( byte[] publicKey ) throws Exception {
        out.writeObject ( publicKey );
    }

    /**
     * Executes the key distribution protocol. The sender sends its public key to the receiver and receives the public
     * key of the receiver.
     *
     * @return the public key of the sender
     *
     * @throws Exception when the key distribution protocol fails
     */
    private PublicKey rsaKeyDistribution ( ) throws Exception {
        // Sends the public key
        sendPublicRSAKey ( );
        // Receive the public key of the sender
        return ( PublicKey ) in.readObject ( );
    }

    /**
     * Sends the public key of the sender to the receiver.
     *
     * @throws IOException when an I/O error occurs when sending the public key
     */
    private void sendPublicRSAKey ( ) throws IOException {
        out.writeObject ( publicRSAKey );
        out.flush ( );
    }

}
