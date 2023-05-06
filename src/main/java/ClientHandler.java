import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * This class represents the client handler. It handles the communication with the client. It reads the file from the
 * server and sends it to the client.
 */
public class ClientHandler extends Thread {

    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Socket client;
    private final boolean isConnected;
    private int requestCount = 0; //each client request count
    private final PrivateKey privateRSAKey;
    private final PublicKey publicRSAKey;

    /**
     * Creates a ClientHandler object by specifying the socket to communicate with the client. All the processing is
     * done in a separate thread.
     *
     * @param client the socket to communicate with the client
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public ClientHandler (Socket client , PrivateKey privateRSAKey, PublicKey publicRSAKey ) throws IOException {
        this.client = client;
        in = new ObjectInputStream ( client.getInputStream ( ) );
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        this.privateRSAKey = privateRSAKey;
        this.publicRSAKey = publicRSAKey;
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
                if(!Integrity.verifyDigest(message.getSignature(),Integrity.generateDigest(decryptedMessage))){
                    throw new RuntimeException ( "The integrity of the message is not verified" );
                }else {
                    String request = new String(decryptedMessage);
                    // Reads the file and sends it to the client
                    byte[] content = FileHandler.readFile(RequestUtils.getAbsoluteFilePath(request));
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
     * Sends the file to the client
     *
     * @param content the content of the file to send
     *
     * @throws IOException when an I/O error occurs when sending the file
     */
    private void sendFile ( byte[] content , BigInteger sharedSecret) throws Exception {
        int tamanhoMax = 1024; // tamanho pacote 1024kb
        int numPacotes = (content.length + tamanhoMax - 1) / tamanhoMax; //calcula numero de pacotes

        for (int i = 0; i < numPacotes; i++) {
            int outtu = i * tamanhoMax; //intervalos de cada conteudo. 0-1024-2048...
            System.out.println(outtu);
            int compri = Math.min(tamanhoMax, content.length - outtu); //tamanho de cada pacote
            byte[] pacote = new byte[compri];
            System.arraycopy(content, outtu, pacote, 0, compri);

            byte[] digest = Integrity.generateDigest(pacote);

            byte[] encryptedMessage = Encryption.encryptMessage(pacote, sharedSecret.toByteArray());

            //Cria o pacote com a mensagem e outras infos (nr da mensagem, se é a ultima...)
            Message response;
            if(i== numPacotes-1){  //verifica se é o ultimo pacote ou nao
                response = new Message(encryptedMessage, digest, i+1, numPacotes, true);
            }else{
                response = new Message(encryptedMessage, digest, i+1, numPacotes, false);
            }
            out.writeObject(response);
            out.flush();
        }
        //byte[] digest = Integrity.generateDigest ( content );
        //byte[] encryptedMessage = Encryption.encryptMessage(content, sharedSecret.toByteArray());
        //Message response = new Message ( encryptedMessage , digest);
        //out.writeObject ( response );
        //out.flush ( );
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
