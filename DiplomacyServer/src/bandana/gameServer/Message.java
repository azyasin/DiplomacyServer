package bandana.gameServer;

import java.util.Arrays;

/**
 * Represents an incoming message. The connection handler creates objects of this class from the bytes it receives, and passes these objects to the GameManager.
 *
 * @author Dave de Jonge, Western Sydney University
 */
public class Message  implements java.io.Serializable {

	public byte messageType;
	public byte[] messageContent;

	public Message(byte Type, byte[] Content) {
		this.messageType = Type;
		this.messageContent = Arrays.copyOf(Content, Content.length);
	}

	public String toString() {

		String s = "";
		for (int i = 0; i < messageContent.length; i += 2) {
			s += TokenTranslator.toString(messageContent[i], messageContent[i + 1]);
		}

		return s;
	}

	public String toByteString() {
		String s = "";
		for (byte b: messageContent) {
			s += Integer.toHexString((int)b) + " ";
		}
		return s;
	}
}
