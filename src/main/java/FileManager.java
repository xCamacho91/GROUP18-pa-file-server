import java.io.*;
import java.util.Scanner;

public class FileManager {

    /**
     * Displays the content of a text file in the console.
     *
     * @param path the path of the file to display
     *
     * @throws IOException when an I/O error occurs when reading the file
     */
    public static String displayFile ( String path ) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        System.out.println("FILE CONTENT:");
        String line;
        String content = "";
        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }
        reader.close();
        return content;
    }

    /**
     * Creates a file in the specified path with the specified name.
     *
     * @param path the path where the file will be created
     * @param name the name of the file to create
     * @param content the content of the file to create
     * @throws IOException when an I/O error occurs when creating the file
     */
    public static void createFile ( String path, String name, String content ) throws IOException {
        File file = new File ( path + name);
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
    }

    /**
     * Returns the server's configuration.
     *
     * @param userDir the path of the file to read
     * @return the number of requests made by the user
     */
    public static int getConfigFile(String userDir) {
        File configFile = new File(userDir);
        if (configFile.exists()) {
            try (Scanner scanner = new Scanner(configFile)) {
                return scanner.nextInt();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    /**
     * Saving in txt file's the number o requests of each client
     *
     * @param userName the name of the user
     * @param requestsMade the number of requests made by the user
     * @throws IOException when an I/O error occurs when writing the file
     */
    public static void saveConfigFile(String userName, int requestsMade) {
        try {
            File dir = new File("config");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, userName + ".txt");
            PrintWriter writer = new PrintWriter(file);
            writer.println(requestsMade);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validate the existence of the directory where the files will be stored.
     *
     * @param userName the name of the user
     * @return the absolute path of the directory where the files will be stored
     */
    public static String validateFile( String userName ) {
        String absolutePath = System.getProperty("user.dir") + File.separator + "users\\" + userName + "\\files";
        File folder = new File(absolutePath);

        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder.getAbsolutePath();
    }

}
