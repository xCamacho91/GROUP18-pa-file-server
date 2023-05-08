import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest {
    @Test
    public void testCheckRequest() throws Exception {
        Client client = new Client(8080, "testUser");
        // pedido inicializa a 0
        assertEquals(0,  client.requestsMade);

        // ciclo de 5 pedidos
        for (int i = 1; i <= 5; i++) {
            client.checkRequest();
            assertEquals(i, client.requestsMade);
        }

        // apos 5 pedidos faz novo handshake
        client.checkRequest();
        assertEquals(0, client.requestsMade);
    }
}

