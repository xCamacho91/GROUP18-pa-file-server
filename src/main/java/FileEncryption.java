import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class FileEncryption {

    public static byte[] decryptMessage ( byte[] message , byte[] secretKey ) throws Exception {
        SecretKeySpec secreteKeySpec = new SecretKeySpec ( secretKey , "AES" );
        Cipher cipher = Cipher.getInstance ( "AES/ECB/PKCS5Padding" );
        cipher.init ( Cipher.DECRYPT_MODE , secreteKeySpec );
        return cipher.doFinal ( message );
    }

    public static byte[] encryptMessage ( byte[] message , byte[] secretKey ) throws Exception {
        SecretKeySpec secreteKeySpec = new SecretKeySpec ( secretKey , "AES" );
        Cipher cipher = Cipher.getInstance ( "AES/ECB/PKCS5Padding" );
        cipher.init ( Cipher.ENCRYPT_MODE , secreteKeySpec  );
        return cipher.doFinal ( message );
    }
    
}
