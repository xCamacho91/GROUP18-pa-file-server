import java.security.MessageDigest;
import java.util.Arrays;

public class Integrity {
    private static final String DIGEST_ALGORITHM = "SHA-512";

    /**
     * Computes the message digest of the given message.
     *
     * @param message The message to be digested.
     *
     * @return the message digest
     *
     * @throws Exception if the message digest algorithm is not available
     */
    public static byte[] generateDigest ( byte[] message ) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance ( DIGEST_ALGORITHM );
        return messageDigest.digest ( message );
    }

    /**
     * Verifies the message digest of the given message.
     *
     * @param digest         the message digest to be verified
     * @param computedDigest the computed message digest
     *
     * @return true if the message digest is valid, false otherwise
     */
    public static boolean verifyDigest ( byte[] digest , byte[] computedDigest ) {
        return Arrays.equals ( digest , computedDigest );
    }

}
