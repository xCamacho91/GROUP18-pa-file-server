import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class TestFileManager {

    @Test
    public void testValidateFile() {
        String userName = "testuser";
        String expectedPath = System.getProperty("user.dir") + File.separator + "users\\" + userName + "\\files";

        try {
            // Test case where folder and subfolder don't exist
            String actualPath1 = FileManager.validateFile(userName);
            assertEquals(expectedPath, actualPath1);
            assertTrue(new File(actualPath1).exists());

            // Test case where folder and subfolder already exist
            String actualPath2 = FileManager.validateFile(userName);
            assertEquals(expectedPath, actualPath2);
            assertTrue(new File(actualPath2).exists());

            // Cleanup: delete the test folder and subfolder
            File testFolder = new File(expectedPath);
            testFolder.delete();
            assertFalse(testFolder.exists());
        } catch (Exception e) {
            fail("An exception occurred while running the test: " + e.getMessage());
        }
    }

    @Test
    public void testCreateFile() {
        // Define the test file path, name, and content
        String testPath = System.getProperty("user.dir") + File.separator;
        String testName = "testfile.txt";
        String testContent = "This is some test content.";

        try {
            // Create the test file
            FileManager.createFile(testPath, testName, testContent);

            // Check that the file was created and contains the expected content
            File testFile = new File(testPath + testName);
            assertTrue(testFile.exists());
            assertEquals(testContent, new String(Files.readAllBytes(Paths.get(testPath + testName))));

            // Delete the test file
            testFile.delete();
        } catch (IOException e) {
            fail("An exception occurred while running the test: " + e.getMessage());
        }
    }

    @Test
    public void testGetConfigFile() {
        // Set up test data
        String userDir = System.getProperty("user.dir") + File.separator + "config";
        String configFile = userDir + File.separator + "config.txt";
        int requestsMade = 5;
        // Create config file
        try {
            FileWriter writer = new FileWriter(configFile);
            writer.write(String.valueOf(requestsMade));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Call method and assert result
        int result = FileManager.getConfigFile(configFile);
        assertEquals(requestsMade, result);

        // Clean up test data
        File file = new File(configFile);
        file.delete();
        File folder = new File(userDir);
        folder.delete();
    }

    @Test
    public void testSaveConfigFile() {
        // Set up test inputs
        String userName = "testuser";
        int requestsMade = 10;

        // Call the method to be tested
        FileManager.saveConfigFile(userName, requestsMade);

        // Verify that the file was created and contains the expected content
        File configFile = new File("config/" + userName + ".txt");
        assertTrue(configFile.exists());
        try (Scanner scanner = new Scanner(configFile)) {
            int actualRequestsMade = scanner.nextInt();
            assertEquals(requestsMade, actualRequestsMade);
        } catch (FileNotFoundException e) {
            fail("Config file not found: " + e.getMessage());
        }

        // Clean up test environment
        configFile.delete();
        File configDir = new File("config");
        configDir.delete();
    }

    @Test
    public void testDisplayFile() throws IOException {
        // create a test file
        String fileName = "test_file.txt";
        String content = "Hello, world!\nThis is a test file.";
        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();

        // call the method and check the output
        String output = FileManager.displayFile(fileName);
        String expectedOutput = "Hello, world!\nThis is a test file.\n";
        assertEquals(expectedOutput, output);

        // delete the test file
        file.delete();
    }

}
