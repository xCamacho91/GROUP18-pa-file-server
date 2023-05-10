import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class Encryption {

    public static KeyPair generateKeyPair ( ) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance ( "RSA" );
        keyPairGenerator.initialize ( 2048 );
        return keyPairGenerator.generateKeyPair ( );
    }

    public static byte[] encryptRSA ( byte[] message , Key key ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.ENCRYPT_MODE , key );
        return cipher.doFinal ( message );
    }

    public static byte[] decryptRSA ( byte[] message , Key key ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.DECRYPT_MODE , key );
        return cipher.doFinal ( message );
    }

    /**
     * @param message   the message to be encrypted
     * @param secretKey the secret key used to encrypt the message
     *
     * @return the encrypted message as an array of bytes
     *
     * @throws Exception when the decryption fails
     */
    public static byte[] decryptMessage ( byte[] message , byte[] secretKey ) throws Exception {
        //byte[] message16 = new byte[16];
        //System.arraycopy(message,0,message16,0,Math.min(message.length,16));
        byte[] secretKeyPadded = ByteBuffer.allocate ( 32 ).put ( secretKey ).array ( );
        SecretKeySpec secreteKeySpec = new SecretKeySpec ( secretKeyPadded , "AES" );
        Cipher cipher = Cipher.getInstance ( "AES/ECB/PKCS5Padding" );
        cipher.init ( Cipher.DECRYPT_MODE , secreteKeySpec );
        return cipher.doFinal ( message );
    }

    /**
     * @param message   the message to be decrypted
     * @param secretKey the secret key used to decrypt the message
     *
     * @return the decrypted message as an array of bytes
     *
     * @throws Exception when the encryption fails
     */
    public static byte[] encryptMessage ( byte[] message , byte[] secretKey ) throws Exception {
        byte[] secretKeyPadded = ByteBuffer.allocate ( 32 ).put ( secretKey ).array ( );
        SecretKeySpec secreteKeySpec = new SecretKeySpec ( secretKeyPadded , "AES" );
        Cipher cipher = Cipher.getInstance ( "AES/ECB/PKCS5Padding" );
        cipher.init ( Cipher.ENCRYPT_MODE , secreteKeySpec );
        return cipher.doFinal ( message );
    }


}