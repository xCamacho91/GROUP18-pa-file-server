import java.io.Serializable;

/**
 * This class represents a message object that is sent to the server by the client.
 */
public class Message implements Serializable {

    private final byte[] message;
    private final byte[] signature;
    private int messageNumber; // message number
    private int totalMessages; // total of messages of each file
    private boolean last;   //verify if it is the last package from the split of the files


    /**
     * Constructs a Message object by specifying the message bytes that will be sent to the server.
     *
     * @param message the message that is sent to the server
     * @param signature hash to verify message integrity
     */
    public Message(byte[] message, byte[] signature, int messageNumber, int totalMessages, boolean last) {
        this.message = message;
        this.signature = signature;
        this.messageNumber = messageNumber;
        this.totalMessages = totalMessages;
        this.last=last;
    }

    /**
     * Gets the message string.
     *
     * @return the message string
     */
    public byte[] getMessage ( ) {
        return message;
    }
    public byte[] getSignature ( ) {
        return signature;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public int getTotalMessages() {
        return totalMessages;
    }
    public boolean isLast() {
        return last;
    }

}