public class ByteUtils {

    public static byte [] generatePad(int value, int blocksize) {
        byte[] output = new byte[ blocksize ];
        for ( int i = 0 ; i < blocksize ; i++ ) {
            output[ i ] = ( byte ) value;
        }
        return output;
    }

    public static byte[] computeXOR(byte[] op1, byte[] op2) {
        byte[] output = new byte[ op1.length ];
        for ( int i = 0 ; i < op1.length ; i++ ) {
            output[ i ] = ( byte ) ( op1[ i ] ^ op2[ i ] );
        }
        return output;
    }

    public static byte[] concatByteArrays(byte[] op1, byte[] op2) {
        byte[] newOutput = new byte[ op1.length + op2.length ];
        System.arraycopy ( op1 , 0 , newOutput , 0 , op1.length );
        System.arraycopy ( op2 , 0 , newOutput , op1.length , op2.length );
        return newOutput;
    }

}
