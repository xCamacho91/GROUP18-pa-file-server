//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//
//public class ClientTest {
//
//    private static final int PORT = 8080;
//    private static final String USERNAME = "TestUser";
//    private static final int MAX_REQUESTS = 5;
//
//    @Test
//    public void testMaxRequests() {
//        try {
//            // Start the server
//            Thread serverThread = new Thread(() -> {
//                try {
//                    Server server = new Server(PORT);
//                    server.run();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            serverThread.start();
//
//            // Wait for the server to start
//            TimeUnit.MILLISECONDS.sleep(100);
//
//            // Create a client and connect to the server
//            Client client = new Client(PORT, USERNAME);
//
//            // Make the maximum number of requests
//            for (int i = 0; i < MAX_REQUESTS; i++) {
//                client.sendMessage("file.txt");
//                client.processResponse("file.txt");
//            }
//
//            // Verify that the client made the maximum number of requests
//            Assertions.assertEquals(MAX_REQUESTS, client.getRequestsMade());
//
//            // Wait for the handshake to complete
//            TimeUnit.MILLISECONDS.sleep(100);
//
//            // Make another request
//            client.sendMessage("file.txt");
//            client.processResponse("file.txt");
//
//            // Verify that the client can make another request after the handshake
//            Assertions.assertEquals(MAX_REQUESTS + 1, client.getRequestsMade());
//
//            // Close the connection
//            client.closeConnection();
//
//            // Stop the server
//            serverThread.interrupt();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
