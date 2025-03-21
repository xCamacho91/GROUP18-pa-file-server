import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class DiffieHellman {

    private static final BigInteger G = BigInteger.valueOf ( 3 );
    private static final BigInteger N = BigInteger.valueOf ( 1289971646 );
    private static final int NUM_BITS = 256;

    public static BigInteger generatePrivateKey ( ) throws NoSuchAlgorithmException {
        Random randomGenerator = SecureRandom.getInstance ( "SHA1PRNG" );
        return new BigInteger ( NUM_BITS , randomGenerator );
    }

    public static BigInteger generatePublicKey ( BigInteger privateKey ) {
        return G.modPow ( privateKey , N );
    }

    public static BigInteger computePrivateKey ( BigInteger publicKey , BigInteger privateKey ) {
        return publicKey.modPow ( privateKey , N );
    }

}