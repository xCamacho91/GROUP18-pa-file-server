import java.io.*;

/**
 * This class represents the file handler. It was the methods for reading and writing text files.
 */
public class FileHandler {


    /**
     * Reads a text file and returns the result in bytes.
     *
     * @param path the path of the file to read
     *
     * @return the content of the file in bytes
     *
     * @throws IOException when an I/O error occurs when reading the file
     */
    public static byte[] readFile ( String path ) throws IOException {
        File file = new File ( path );
        byte[] fileBytes = new byte[ ( int ) file.length ( ) ];
        FileInputStream fileInputStream = new FileInputStream ( file );
        fileInputStream.read ( fileBytes );
        fileInputStream.close ( );
        return fileBytes;
    }

    /**
     * Writes a text file and returns the result in bytes
     */
    public static void writeFile ( String path , byte[] content ) throws IOException {
        File file = new File ( path );
        FileOutputStream fileOutputStream = new FileOutputStream ( file );
        fileOutputStream.write ( content );
        fileOutputStream.close ( );
    }

    /**
     * Displays the content of a text file in the console.
     *
     * @param path the path of the file to display
     *
     * @throws IOException when an I/O error occurs when reading the file
     */
    public static void displayFile ( String path ) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        System.out.println("FILE CONTENT:");
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
    }

}
