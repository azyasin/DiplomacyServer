package KK;

import bandana.gameServer.Message;
import bandana.gameServer.TokenTranslator;

import java.io.InputStream;

public class KKListener extends Thread{
	InputStream inputStream;
	public KKListener(InputStream inputStream) {
		try {
			this.inputStream = inputStream;
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
		System.out.println("Listener started");
		while (true) {
			try {
				byte[] header = new byte[4];
				inputStream.read(header);
				int remainingBytes = 256 * (header[2] & 0xff) + (header[3] & 0xff);
				if (remainingBytes == 0) {
					continue;
				}
				byte[] msgBody = new byte[remainingBytes];
				inputStream.read(msgBody);
				System.out.println(new Message((byte)2, msgBody).toString());
				Thread.sleep(500);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
