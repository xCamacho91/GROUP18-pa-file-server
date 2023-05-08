import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class TestClientHandler {

        @Test
        public void testSendFile() {
            byte[] content = new byte[5000]; // Cria file de 5000kb
            BigInteger sharedSecret = new BigInteger("1234567890");
            int tamanhoMax = 1024; // Tamanho máximo de cada pacote

            try {
                Socket client = new Socket("localhost", 12345);
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                PrivateKey privateRSAKey = keyPair.getPrivate();
                PublicKey publicRSAKey = keyPair.getPublic();

                ClientHandler send = new ClientHandler(client, privateRSAKey, publicRSAKey);
                send.sendFile(content, sharedSecret);

                int numPacotes = (content.length + tamanhoMax - 1) / tamanhoMax; // Calcula o número de pacotes

                // Verifica se o número de pacotes é correto
                assertEquals(numPacotes, 5);

                // Verifica se cada pacote tem o tamanho esperado
                for (int i = 0; i < numPacotes; i++) {
                    byte[] pacote = send.getPacotes(i);

                    int tamanhoEsperado = Math.min(tamanhoMax, content.length - i * tamanhoMax);
                    assertEquals(tamanhoEsperado, pacote.length);
                }
            } catch (Exception e) {
                fail("Erro: " + e.getMessage());
            }
        }



}
