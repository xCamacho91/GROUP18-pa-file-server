import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestHandShake {
    @Test
    public void testRunMethod() throws Exception {
        //initialize RSA keys
        KeyPair keyPair = Encryption.generateKeyPair ( );
        PublicKey serverPublicRSAKey = keyPair.getPublic();
        PrivateKey serverPrivateRSAKey = keyPair.getPrivate();

        keyPair = Encryption.generateKeyPair ( );
        PublicKey clientPublicRSAKey = keyPair.getPublic();
        PrivateKey clientPrivateRSAKey = keyPair.getPrivate();

        //Diffiehellman
            //server
                // Generate a pair of keys
        BigInteger serverPrivateDHKey = DiffieHellman.generatePrivateKey ( );
        BigInteger serverPublicDHKey = DiffieHellman.generatePublicKey ( serverPrivateDHKey );
        byte[] encryptedServerPublicDHKey = Encryption.encryptRSA ( serverPublicDHKey.toByteArray ( ) , serverPrivateRSAKey );

            //client
                // Generate a pair of keys
        BigInteger clientPrivateDHKey = DiffieHellman.generatePrivateKey ( );
        BigInteger clientPublicDHKey = DiffieHellman.generatePublicKey ( clientPrivateDHKey );
        byte[] encryptedClientPublicDHKey = Encryption.encryptRSA ( clientPublicDHKey.toByteArray ( ) , clientPrivateRSAKey );

            //generate sharedKeys

        BigInteger decryptedClientPublicDHKey = new BigInteger ( Encryption.decryptRSA ( encryptedClientPublicDHKey , clientPublicRSAKey ) );
        BigInteger serverSharedKey = DiffieHellman.computePrivateKey ( decryptedClientPublicDHKey , serverPrivateDHKey );

        BigInteger decryptedServerPublicDHKey = new BigInteger ( Encryption.decryptRSA ( encryptedServerPublicDHKey , serverPublicRSAKey ) );
        BigInteger clientSharedKey = DiffieHellman.computePrivateKey ( decryptedServerPublicDHKey , clientPrivateDHKey );

        assertEquals(serverSharedKey,clientSharedKey);
    }
}
