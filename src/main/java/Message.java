import java.io.Serializable;

/**
 * This class represents a message object that is sent to the server by the client.
 */
public class Message implements Serializable {

    /**
     * The message that is sent to the server
     */
    private final byte[] message;
    /**
     * The signature of the message
     */
    private final byte[] signature;
    /**
     * The message number
     */
    private final int messageNumber; // message number
    /**
     * The total messages
     */
    private final int totalMessages; // total of messages of each file
    /**
     * is last message
     */
    private final boolean last;   //verify if it is the last package from the split of the files


    /**
     * Constructs a Message object by specifying the message bytes that will be sent to the server.
     *
     * @param message the message that is sent to the server
     * @param signature hash to verify message integrity
     * @param messageNumber the message number
     * @param totalMessages the total messages
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

    /**
     * Gets the signature string.
     *
     * @return the signature string
     */
    public byte[] getSignature ( ) {
        return signature;
    }

    /**
     * Gets the message number.
     *
     * @return the message number
     */
    public int getMessageNumber() {
        return messageNumber;
    }

    /**
     * Gets the total messages.
     *
     * @return the total messages
     */
    public int getTotalMessages() {
        return totalMessages;
    }

    /**
     * Gets the last message.
     *
     * @return the last message
     */
    public boolean getLast() {
        return last;
    }
}