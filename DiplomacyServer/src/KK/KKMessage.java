package KK;

public class KKMessage {

	private byte messageType;
	private byte[] msgBody;

	KKMessage(byte type, byte[] body) {
		messageType = type;
		msgBody = new byte[body.length];
		for (int i = 0; i < body.length; i++) {
			msgBody[i] = body[i];
		}
	}

	String humanReadable(KKLanguageDictionary dictionary) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while(i < msgBody.length) {
			if (msgBody[i] <= 0x3F) {
				//Int
				sb.append((int) msgBody[i]);
				i++;
			}
			else if (msgBody[i] == 0x40) {
				//Misc.
				if (msgBody[i+1] == 0x00) {
					sb.append("(");
				}
				else if (msgBody[i+1] == 0x01) {
					sb.append(")");
				}
				else {
					sb.delete(0, sb.length());
					sb.append("Error: Message untranslatable at ");
                    sb.append(String.format("%02X ", msgBody[i]));
                    sb.append(String.format("%02X ", msgBody[i+1]));
                    i = msgBody.length;
				}
				i+=2;
			}
			else if (msgBody[i] == 0x4B) {
				sb.append((char) msgBody[i+1]);
				i+=2;
			}
			else {
				StringBuilder key = new StringBuilder();
				String retVal;
				key.append(String.format("%02X", msgBody[i]));
				key.append(String.format("%02X", msgBody[i+1]));
				retVal = dictionary.getValue(key.toString());
				if (retVal != null) {
					sb.append(retVal);
					i+=2;
				}
				else {
                    sb.delete(0, sb.length());
                    sb.append("Error: Message untranslatable at ");
                    sb.append(String.format("%02X ", msgBody[i]));
                    sb.append(String.format("%02X ", msgBody[i+1]));
                    sb.append(dictionary.getSize());
                    i = msgBody.length;
				}
			}
		}
		return sb.toString();
	}

}
