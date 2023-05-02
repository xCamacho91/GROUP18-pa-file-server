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
    private final String userName;

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
    }


    /**
     * Validate the existence of the directory where the files will be stored.
     *
     * @throws IOException if an I/O error occurs when writing stream header
     */
    public void validateFile() throws IOException {
        String absolutePath = System.getProperty("user.dir") + File.separator + "users\\" + this.userName;
        File folder = new File(absolutePath);
        File subfolder = new File(folder, "files");

        if (!folder.exists()) {
            subfolder.mkdirs();
            userDir = subfolder.getAbsolutePath();
            System.out.println("Folder created at path: " + folder.getAbsolutePath());
            System.out.println("Subfolder created at path: " + subfolder.getAbsolutePath());
        } else {
            userDir = subfolder.getAbsolutePath();
            System.out.println("Subfolder already exists at path: " + subfolder.getAbsolutePath());
        }

    }


    public void configFile () {
        // TODO generate the config file whem the user folder is created
        // TODO charge the config file if the user folder already exists
        // TODO set the amount request variable with the amount of files in the folder
    }


    /**
     * Executes the client. It reads the file from the console and sends it to the server. It waits for the response and
     * writes the file to the temporary directory.
     */
    public void execute ( ) {
        Scanner usrInput = new Scanner ( System.in );
        try {
            while ( isConnected ) {
                // Reads the message to extract the path of the file
                System.out.println ( "Write the path of the file" );
                String request = usrInput.nextLine ( );
                // Request the file
                sendMessage ( request );
                // Waits for the response
                processResponse ( RequestUtils.getFileNameFromRequest ( request ) );
            }
            // Close connection
            closeConnection ( );
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

}
