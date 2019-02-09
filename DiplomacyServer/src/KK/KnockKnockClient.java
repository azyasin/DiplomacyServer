package KK;
/*
 * This client was developed from the Knock Knock tutorial by Oracle.
 * It was developed purely for the purposes of testing a Diplomacy game server.
 */
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
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

import bandana.gameServer.Message;
import bandana.gameServer.TokenTranslator;

import java.io.*;
import java.net.*;
import java.util.*;

public class KnockKnockClient {
	Socket kkSocket;
	InputStream in;
	OutputStream out;
	public KnockKnockClient() {
		try {
			String hostName = "localhost";
			int portNumber = 16713;
			Socket kkSocket = new Socket(hostName, portNumber);
			InputStream in = kkSocket.getInputStream();
			OutputStream out = kkSocket.getOutputStream();
		}
		catch (Exception e) {
		}
	}

	public static void main(String[] args) throws IOException {
		String hostName = "localhost";
		int portNumber = 16713;
		Socket kkSocket = new Socket(hostName, portNumber);
		InputStream in = kkSocket.getInputStream();
		OutputStream out = kkSocket.getOutputStream();
		byte[] b = {0,0,0,4};
		out.write(b);
		b[0] = 0;
		b[1] = 1;
		b[2] = (byte) 0xDA;
		b[3] = (byte) 0x10;
		out.write(b);
		KKListener kkListener = new KKListener(in);
		kkListener.start();
		KKWriter kkWriter = new KKWriter(out);
		kkWriter.start();
	}

}