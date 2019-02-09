package KK;

import bandana.gameServer.Message;
import bandana.gameServer.TokenTranslator;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class KKWriter extends  Thread{
	OutputStream outputStream;
	public KKWriter(OutputStream outputStream) {
		try {
			this.outputStream = outputStream;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void start() {
		super.start();
	}

	public void run() {
		System.out.println("Writer started");
		Scanner scanner = new Scanner(System.in);
		String fromUser;
		while (true) {
			System.out.println("Please enter next message: ");
			fromUser = scanner.nextLine();
			int pos = 0;
			List<Byte> buffer = new ArrayList<>();
			while (pos < fromUser.length()) {
				if (fromUser.charAt(pos) == '(') {
					buffer = addBytesToList(buffer, openingBracket());
					pos++;
				} else if (fromUser.charAt(pos) == ')') {
					buffer = addBytesToList(buffer, closingBracket());
					pos++;
				} else if (fromUser.charAt(pos) == '*') {
					pos++;
					while (fromUser.charAt(pos) != '*') {
						buffer = addBytesToList(buffer, TokenTranslator.toAsciiHex(fromUser.charAt(pos)));
						pos++;
					}
					pos++;
				} else if (fromUser.charAt(pos) == '%') {
				pos++;
				String num = "";
				while (fromUser.charAt(pos) != '%') {
					num += fromUser.charAt(pos);
					pos++;
				}
				int translated = 0;
				if (num.charAt(0) == '-') {
					translated = Integer.parseInt(num.substring(1));
					translated = translated * -1;
				}
				else {
					translated = Integer.parseInt(num);
				}
				buffer = addBytesToList(buffer, TokenTranslator.toHexNum(translated));
				pos++;
			}
				else {
					byte[] add = TokenTranslator.toHexBytes(fromUser.substring(pos, pos + 3));
					if (add == null) {
						System.out.println("Issue with translation.");
					}
					buffer = addBytesToList(buffer, add);
					pos += 3;
				}
			}
			byte[] resp = new byte[buffer.size()];
			for (int i = 0; i < buffer.size(); i++) {
				resp[i] = buffer.get(i);
			}
			sendMessage(new Message((byte) 2, resp), outputStream);
			try {
				Thread.sleep(3000);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static private byte[] openingBracket() {
		return new byte[]{0x40, 0x00};
	}

	static private byte[] closingBracket() {
		return new byte[]{0x40, 0x01};
	}

	static private List<Byte> addBytesToList(List<Byte> byteList, byte[] bytes) {
		for (byte b: bytes) {
			byteList.add(b);
		}
		return byteList;
	}

	public static void sendMessage(Message msg, OutputStream outputStream) {
		try {
			String hexLen = Integer.toHexString(msg.messageContent.length);
			byte[] msgHead = new byte[4];
			msgHead[0] = msg.messageType;
			msgHead[1] = 0;

			if (msg.messageContent.length < 256) {
				msgHead[2] = 0x00;
				msgHead[3] = (byte) msg.messageContent.length;
			}
			else if (msg.messageContent.length == 256) {
				msgHead[2] = 0x01;
				msgHead[3] = 0x00;
			}
			else {
				int div = msg.messageContent.length / 256;
				int rem = msg.messageContent.length - (div * 256);
				msgHead[2] = (byte) div;
				msgHead[3] = (byte) rem;
			}

			System.out.print("Sending Header: ");
			for (int i = 0; i < 4; i++){
				System.out.print(Integer.toHexString(msgHead[i] & 0xff) + " ");
			}
			System.out.println();
			System.out.println("Sending: " + msg.toString());
			for (int i = 0; i < msg.messageContent.length; i++){
				System.out.print(Integer.toHexString(msg.messageContent[i] & 0xff) + " ");
			}
			outputStream.write(msgHead);
			outputStream.write(msg.messageContent);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
