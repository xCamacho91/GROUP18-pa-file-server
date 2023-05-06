import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;
import java.util.Scanner;

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
    private int requestsMade = 0; //number of requests
    private static final int MAX_REQUESTS = 5; //max of requests before new handshake
    private final PublicKey publicRSAKey;
    private final PrivateKey privateRSAKey;
    private final PublicKey receiverPublicRSAKey;

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
        KeyPair keyPair = Encryption.generateKeyPair ( );
        this.privateRSAKey = keyPair.getPrivate ( );
        this.publicRSAKey = keyPair.getPublic ( );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
        // Create a temporary directory for putting the request files

        this.receiverPublicRSAKey = rsaKeyDistribution ( );

        //TODO : Create a function that to the following actions
        userDir = FileManager.validateFile(userName);
        FileManager.createFile( userDir + "/../", "config.config", "server.request = 5");
        Properties pro = FileManager.getProperties(userDir + "/../");
        FileManager.createFile( pkiDir, this.userName + "PuK.txt", this.publicRSAKey.toString());
        FileManager.createFile( userDir + "/../", "private.txt", this.privateRSAKey.toString());

    }


    /**
     * Executes the client. It reads the file from the console and sends it to the server. It waits for the response and
     * writes the file to the temporary directory.
     */
    public void execute ( ) {
        Scanner usrInput = new Scanner ( System.in );
        try {
            // Agree on a shared secret
            BigInteger sharedSecret = agreeOnSharedSecret ( receiverPublicRSAKey );
            while ( isConnected ) {
                saveConfig();
                if (requestsMade+1 >= MAX_REQUESTS){

                    //responde ao pedido pq é o quinto
                    System.out.println("Request number: "+ requestsMade);
                    // Reads the message to extract the path of the file
                    System.out.println ( "Write the path of the file" );
                    String request = usrInput.nextLine ( );
                    // Request the file
                    sendMessage ( request , sharedSecret);
                    // Waits for the response
                    processResponse ( RequestUtils.getFileNameFromRequest ( request ) , sharedSecret);

                    System.out.println("Reached 5 requests, making new handshake");
                    requestsMade=0;
                    //sair daqui, fazer novo handshake


                }else{
                    System.out.println("Request number: "+ requestsMade);
                    // Reads the message to extract the path of the file
                    System.out.println ( "Write the path of the file" );
                    String request = usrInput.nextLine ( );
                    // Request the file
                    sendMessage ( request , sharedSecret);
                    // Waits for the response
                    processResponse ( RequestUtils.getFileNameFromRequest ( request ) , sharedSecret);
                    requestsMade++; //nao sei depois como será feito. incrementar so depois de ele meter o input, senao vai contar como pedido ele escrever quit para sair da sessao
                }

            }
            closeConnection ( );
            //closeConnection ( );
        } catch ( IOException e ) {
        } catch (Exception e ) {
            throw new RuntimeException ( e );
        }
        // Close connection
        closeConnection ( );
    }

    /**
     * Reads the response from the server and writes the file to the temporary directory.
     *
     * @param fileName the name of the file to write
     * @param sharedSecret symmetric key to decrypt message
     */
    private void processResponse ( String fileName , BigInteger sharedSecret) {
        try {
            Message response = ( Message ) in.readObject ( );
            byte[] decryptedMessage = Encryption.decryptMessage ( response.getMessage ( ) , sharedSecret.toByteArray ( ) );
            if(!Integrity.verifyDigest(response.getSignature(),Integrity.generateDigest(decryptedMessage))){
                throw new RuntimeException ( "The integrity of the message is not verified" );
            }else {
                System.out.println("File received");
                FileHandler.writeFile(userDir + "/" + fileName, decryptedMessage);

                System.out.println( FileManager.displayFile(userDir + "/" + fileName) );
                // TODO show the content of the file in the console
            }
        } catch ( IOException | ClassNotFoundException e ) {
                //requestsMade--;
            System.out.println ( "ERROR - FILE NOT FOUND" );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        Message messageObj = new Message ( encryptedMessage , digest);
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
     * Saving in txt file's the number o requests of each client
     * @throws IOException
     */
    public void saveConfig() throws IOException {
        {
            try {
                File dir = new File("config");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, this.userName + ".txt");
                PrintWriter writer = new PrintWriter(file);
                writer.println(this.requestsMade);
                writer.close();
                requestsMade=this.requestsMade;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * used for tests
     * @return
     */
    public int getRequestsMade() {
        return requestsMade;
    }

}
