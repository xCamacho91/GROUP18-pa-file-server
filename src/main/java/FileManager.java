import java.io.*;
import java.util.Properties;

public class FileManager {

    /**
     * The server's configuration, imported from the configuration file when the server started.
     */
    private static Properties serverConfig;

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
     * @param path
     * @param name
     * @throws IOException
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
     * @param userDir
     * @return
     */
    public static Properties getProperties(String userDir) {
        try {
            serverConfig = new Properties();
            InputStream configPathInputStream = new FileInputStream(userDir + "/config.config");
            serverConfig.load(configPathInputStream);
            return serverConfig;
        } catch (IOException e) {
            System.out.println("Config file not found.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Validate the existence of the directory where the files will be stored.
     */
    public static String validateFile( String userName ) {
        String userDir = null;
        String absolutePath = System.getProperty("user.dir") + File.separator + "users\\" + userName + "\\files";
        File folder = new File(absolutePath);
        //File subfolder = new File(folder, "files");

        if (!folder.exists()) {
            folder.mkdirs();
            userDir = folder.getAbsolutePath();
            System.out.println("Folder created at path: " + folder.getAbsolutePath());
            System.out.println("Subfolder created at path: " + folder.getAbsolutePath());
        } else {
            userDir = folder.getAbsolutePath();
            System.out.println("Subfolder already exists at path: " + folder.getAbsolutePath());
        }

        return userDir;
    }

}
