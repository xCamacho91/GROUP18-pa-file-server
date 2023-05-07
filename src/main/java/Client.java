import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;
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
    private final String pkiDir = System.getProperty("user.dir") + "/pki/public_keys/";
    private static String userDir;
    private final String userName;
    private static int requestsMade = 0; //number of requests
    private final int MAX_REQUESTS = 5; //max of requests before new handshake
    private static PublicKey publicRSAKey;
    private static PrivateKey privateRSAKey;
    private static PublicKey receiverPublicRSAKey;
    private static BigInteger sharedSecret;

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
        this.userName = userName;
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        in = new ObjectInputStream ( client.getInputStream ( ) );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
        // Create a temporary directory for putting the request files

        KeyPair keyPair = Encryption.generateKeyPair ( );
        this.privateRSAKey = keyPair.getPrivate ( );
        this.publicRSAKey = keyPair.getPublic ( );
        this.receiverPublicRSAKey = rsaKeyDistribution ( );
        this.sharedSecret = agreeOnSharedSecret ( receiverPublicRSAKey );

        validateDetailsUser();
    }

    /**
     *  Creates the files for the user and validates the user
     *
     * @throws Exception
     */
    private void validateDetailsUser() throws Exception {
        userDir = FileManager.validateFile(userName);
        FileManager.createFile( pkiDir, this.userName + "PuK.key", this.publicRSAKey.toString());
        FileManager.createFile( userDir + "/../", "private.txt", this.privateRSAKey.toString());

        FileManager.getConfigFile("config/" +userName + ".txt");
    }


    /**
     * Executes the client. It reads the file from the console and sends it to the server. It waits for the response and
     * writes the file to the temporary directory.
     */
    public void execute ( ) {
        Scanner usrInput = new Scanner ( System.in );
        try {
            // Agree on a shared secret
            while ( isConnected ) {
                FileManager.saveConfigFile(this.userName, this.requestsMade);
                checkRequest();
                System.out.println("Request number: "+ this.requestsMade);
                // Reads the message to extract the path of the file
                System.out.println ( "Write the path of the file" );
                String request = usrInput.nextLine ( );
                // Request the file
                sendMessage ( request , this.sharedSecret);
                // Waits for the response

                try {
                    processResponse ( RequestUtils.getFileNameFromRequest ( request ) , this.sharedSecret);
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
    private void checkRequest () {

        if (requestsMade+1 >= MAX_REQUESTS) {
            System.out.println("Reached 5 requests, making new handshake");
            //try {
                //this.sharedSecret = agreeOnSharedSecret ( this.receiverPublicRSAKey );
            // catch (Exception e) {
                //System.out.println ( "Impossivel gerar novo handshake" );
            //}
            this.requestsMade = 0;
        } else {
            this.requestsMade++; //nao sei depois como será feito. incrementar so depois de ele meter o input, senao vai contar como pedido ele escrever quit para sair da sessao
        }
    }



    /**
     * Reads the response from the server and writes the file to the temporary directory.
     *
     * @param fileName the name of the file to write
     * @param sharedSecret symmetric key to decrypt message
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
                if(!Integrity.verifyDigest(response.getSignature(),Integrity.generateDigest(decryptedMessage))) {
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
        byte[] digest = Integrity.generateDigest ( filePath.getBytes ( ) );
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


    /**
     * used for tests
     * @return
     */
    public int getRequestsMade() {
        return requestsMade;
    }

}
