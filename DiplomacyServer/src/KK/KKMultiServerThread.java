package KK;/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *		notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *		notice, this list of conditions and the following disclaimer in the
 *		documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *		contributors may be used to endorse or promote products derived
 *		from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.net.*;
import java.io.*;

//KKMultiServer Thread code
public class KKMultiServerThread extends Thread {
	private Socket socket = null;
	public KKLanguageDictionary dictionary;

	//uses the socket provided by 
	public KKMultiServerThread(Socket socket) {
		super("KKMultiServerThread");
		this.socket = socket;
		dictionary = new KKLanguageDictionary();
	}

	// Run function
	public void run() {
		int packets = 0;
		try (
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
		) {
			StringBuilder sb = new StringBuilder();
			byte[] message = new byte[4];

			while ((in.read(message)) != -1) { // read length of incoming message readInt()

				//printing message head
				System.out.println("Message Header:");
				for (byte i : message) {
					sb.append(String.format("%02X ", i)); //converts the bytes into a string builder in hex format
				}
				System.out.println(sb);
				sb.delete(0, sb.length());

				//Received final message, client is closing so shutting down connection
				if (message[0] == 3) {
					System.out.println("Final message recieved, closing connection!");
					break;
				}

				//getting message body
				packets = 256 * message[2] + message[3];
				byte[] body = new byte[packets];
				in.read(body);

				//printing message body
				System.out.println("Message Body:");
				for (byte i : body) {
					sb.append(String.format("%02X ", i)); //converts the bytes into a string builder in hex format
				}
				System.out.println(sb);
				sb.delete(0, sb.length());
				//TODO:Translate, check and handle message
				if (message[0] != 0x00) {
					KKMessage msg = new KKMessage(message[0], body);
					System.out.println(msg.humanReadable(dictionary));
				}

				if (message[0] == 0) {
					byte[] rm = {1, 0, 0, 0};
					out.write(rm);
					for (byte i : rm) {
						sb.append(String.format("%02X ", i)); //converts the bytes into a string builder in hex format
					}
					System.out.println("Replying with: " + sb);
					sb.delete(0, sb.length());
				}
			}
			socket.close();
			System.out.println("Socket CLOSED!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}