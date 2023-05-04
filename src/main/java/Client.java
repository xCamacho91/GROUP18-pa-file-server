import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static String userDir;
    private static String userDir1;
    private final String userName;
    private int requestsMade = 0; //number of requests
    private static final int MAX_REQUESTS = 5; //max of requests before new handshake

    /**
     * Constructs a Client object by specifying the port to connect to. The socket must be created before the sender can
     * send a message.
     *
     * @param port the port to connect to
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public Client ( int port, String userName ) throws IOException {
        client = new Socket ( HOST , port );
        this.userName = userName;
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        in = new ObjectInputStream ( client.getInputStream ( ) );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
        // Create a temporary directory for putting the request files
        validateFile();

        // read number of requests from config file (se existir)
        File configFile = new File("config/" +userName + ".txt");
        if (configFile.exists()) {
            try (Scanner scanner = new Scanner(configFile)) {
                requestsMade = scanner.nextInt();
            }
        } else {
            requestsMade = 0; // ainda sem pedidos
        }
    }



    /**
     * Validate the existence of the directory where the files will be stored.
     *
     * @throws IOException if an I/O error occurs when writing stream header
     */
    public void validateFile() throws IOException {
        String absolutePath = System.getProperty("user.dir") + File.separator + "users\\" + this.userName + File.separator + "files";
        File folder = new File(absolutePath);

        if (!folder.exists()) {
            folder.mkdir();
            userDir = absolutePath;
            System.out.println ( "Temporary directory path " + userDir );
            System.out.println("Folder created at path: " + absolutePath);
        } else {
            userDir = absolutePath;
            System.out.println("Folder already exists at path: " + absolutePath);
        }
    }



    /**
     * Executes the client. It reads the file from the console and sends it to the server. It waits for the response and
     * writes the file to the temporary directory.
     */
    public void execute ( ) {
        Scanner usrInput = new Scanner ( System.in );
        try {
            while ( isConnected ) {
                saveConfig();
                if (requestsMade+1 >= MAX_REQUESTS){

                    //responde ao pedido pq é o quinto
                    System.out.println("Request number: "+ requestsMade);
                    // Reads the message to extract the path of the file
                    System.out.println ( "Write the path of the file" );
                    String request = usrInput.nextLine ( );
                    // Request the file
                    sendMessage ( request );
                    // Waits for the response
                    processResponse ( RequestUtils.getFileNameFromRequest ( request ) );

                    System.out.println("Reached 5 requests, making new handshake");
                    requestsMade=0;
                    //sair daqui, fazer novo handshake


                }else{
                    System.out.println("Request number: "+ requestsMade);
                    // Reads the message to extract the path of the file
                    System.out.println ( "Write the path of the file" );
                    String request = usrInput.nextLine ( );
                    // Request the file
                    sendMessage ( request );
                    // Waits for the response
                    processResponse ( RequestUtils.getFileNameFromRequest ( request ) );
                    requestsMade++; //nao sei depois como será feito. incrementar so depois de ele meter o input, senao vai contar como pedido ele escrever quit para sair da sessao
                }

            }
            closeConnection ( );
            //closeConnection ( );
        } catch ( IOException e ) {
            throw new RuntimeException ( e );
        }
        // Close connection
        closeConnection ( );
    }

    /**
     * Reads the response from the server and writes the file to the temporary directory.
     *
     * @param fileName the name of the file to write
     */
    private void processResponse ( String fileName ) {
        try {
            Message response = ( Message ) in.readObject ( );
            System.out.println ( "File received" );
            FileHandler.writeFile ( userDir + "/" + fileName , response.getMessage ( ) );

            FileHandler.displayFile(userDir + "/" + fileName);
            // TODO show the content of the file in the console
        } catch ( IOException | ClassNotFoundException e ) {
                //requestsMade--;
            System.out.println ( "ERROR - FILE NOT FOUND" );
        }
    }

    /**
     * Sends the path of the file to the server using the OutputStream of the socket. The message is sent as an object
     * of the {@link Message} class.
     *
     * @param filePath the message to send
     *
     * @throws IOException when an I/O error occurs when sending the message
     */
    public void sendMessage ( String filePath ) throws IOException {
        // Creates the message object
        Message messageObj = new Message ( filePath.getBytes ( ) );
        // Sends the message
        out.writeObject ( messageObj );
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


}
