import java.security.MessageDigest;

public class HMAC {

    public static byte[] computeHMAC (byte [] text_message, byte [] key , int blocksize, MessageDigest messageDigest ){
        byte [] blockedSizedKey = computeBlockSizedKey (key , blocksize, messageDigest);
        byte [] KeyWithOpad = ByteUtils.computeXOR ( blockedSizedKey, ByteUtils.generatePad (0x5c, blocksize));
        byte [] KeyWithIpad = ByteUtils.computeXOR ( blockedSizedKey, ByteUtils.generatePad (0x36, blocksize));
        byte [] KeyWithOpadMessageDigested = messageDigest.digest(ByteUtils.concatByteArrays (KeyWithIpad,text_message));
        byte [] argDigest = ByteUtils.concatByteArrays(KeyWithOpad, KeyWithOpadMessageDigested);
        return messageDigest.digest(argDigest);
        
    }

    private static byte[] computeBlockSizedKey(byte[] key, int blocksize, MessageDigest messageDigest) {
        if (key.length>blocksize){
            key=messageDigest.digest(key);
        }
        if (key.length<blocksize){
            byte [] blockedSizedKey = new byte [blocksize];
            System.arraycopy(key, 0, blockedSizedKey, 0, key.length);
            for ( int i = key.length;i<blocksize;i++){
                blockedSizedKey [i] = 0x00;
            }
            return blockedSizedKey;
        }
        else {
            return key;
        }
    }
    
}
