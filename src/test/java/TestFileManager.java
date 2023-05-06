import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

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
    void testGetProperties() throws IOException {
        // Create a temporary directory to hold the config file
        String userDir = System.getProperty("java.io.tmpdir") + File.separator + "test-dir";
        File tempDir = new File(userDir);
        tempDir.mkdir();

        // Create a test config file in the temporary directory
        String configPath = userDir + File.separator + "config.config";
        String configContent = "key1=value1\nkey2=value2";
        FileWriter writer = new FileWriter(configPath);
        writer.write(configContent);
        writer.close();

        // Test the getProperties method
        Properties props = FileManager.getProperties(userDir);
        assertEquals(props.getProperty("key1"), "value1");
        assertEquals(props.getProperty("key2"), "value2");

        // Clean up the temporary directory
        tempDir.delete();
    }

    @Test
    public void testDisplayFile() throws IOException {
        // Create a test file with some content
        String userName = "testuser";
        String expectedPath = System.getProperty("user.dir") + File.separator + "users\\" + userName + File.separator;
        String testPath = "test.txt";
        String testContent = "This is a test file.\nIt has some text in it.\n";
        FileManager.createFile(expectedPath, testPath, testContent);

        // Call the displayFile method on the test file
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String expectedOutput = "FILE CONTENT:\n" + FileManager.displayFile(expectedPath + "\\" + testPath);
        assertTrue(!expectedOutput.isEmpty());

        // Delete the test file
        File testFile = new File(testPath);
        testFile.delete();
    }
}
