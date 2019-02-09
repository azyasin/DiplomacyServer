package bandana.gameServer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class TokenTranslator {

	/**
	 * Each 'token' sent by either the client or the server consists of two bytes.
	 * This method converts each token into its human-readable string representation
	 *
	 * @param b1
	 * @param b2
	 * @return
	 */
	static public String toString(byte b1, byte b2) {

		if (b1 == 0x40) { //Miscellaneous category
			if (b2 == 0x00) {
				return "(";
			} else if (b2 == 0x01) {
				return ")";
			} 
		else {		//??

			}
		} else if (b1 == 0x4B) { //Text category
			return "" + (char) b2;
		}
		else if (b1 <= 0x3F) {
			String binary = Integer.toBinaryString((int) (b2 & 0xff));
			while (binary.length() < 8) {
				binary = "0" + binary;
			}
			binary = Integer.toBinaryString((int) (b1 & 0xff)) + binary;

			int result= 0;
			while (binary.length() < 16) {
				binary = "0" + binary;
			}

			if(binary.charAt(2)=='0') {
				result = Integer.parseInt(binary.substring(3), 2);
				return Integer.toString(result);
			} else {
				char[] inverted = binary.substring(3).toCharArray();
				for (int i = inverted.length-1; i>-1; i--) {
					if(inverted[i]=='1'){
						inverted[i]='0';
						break;
					} else {
						inverted[i]='1';
					}
				}

				for (int i=0; i<inverted.length; i++) {
					if(inverted[i]=='0'){
						inverted[i]='1';
					} else {
						inverted[i]='0';
					}
				}
				binary = "";
				for (char c: inverted) {
					binary += c;
				}
				result = Integer.parseInt(binary, 2) * -1;
				//System.out.println(result + " " + binary);
				return (Integer.toString(result));
			}


		}

		//Other categories interpreted by LanguageDictionary
		String retVal;
		retVal = Constants.dictionary.getEntry(b1, b2);
		if (retVal == null) {
			//For unknown tokens: return the hexadecimal representation of the two bytes.
			String b = Integer.toHexString(b1);
			if (b.length() == 1) {
				b = "0" + b;
			}
			retVal+=b;
			b = Integer.toHexString(b2);
			if (b.length() == 1) {
				b = "0" + b;
			}
			retVal+=b;
			retVal = retVal.toUpperCase();
			return retVal;
		}
		return retVal;
	}

	static public String toBinaryString(byte b1, byte b2) {
		return "" + Integer.toBinaryString((int)b1) + Integer.toBinaryString((int)b2);
	}

	static public byte[] toHexBytes(String name) {
		return Constants.dictionary.getHexCode(name);
	}

	static public byte[] toHexNum(int num) {
		byte[] returnValue = new byte[2];
		boolean neg = false;
		String binary = "";
		if (num < 0) {
			binary = Integer.toBinaryString(num * -1);
		}
		else {
			binary = Integer.toBinaryString(num);
		}

		while(binary.length() < 13) {
			binary = "0" + binary;
		}
		if(num>-1) {
			binary = "00" + "0" + binary; //add prefix and sign
		}
		else {
			String inverted = "";
			for (int i = 0; i < binary.length(); i++){
				if (binary.charAt(i) == '0') {
					inverted += "1";
				}
				else {
					inverted += "0";
				}
			}

			String carryOver = "1";
			char[] inv = inverted.toCharArray();
			for (int i = inv.length - 1; i > -1; i--) {
				if (inv[i] == '0'){
					inv[i] = '1';
					break;
				}
				else {
					inv[i] = '0';
				}
			}
			binary = "00" + "1"; //add prefix and sign
			for (char c: inv) {
				binary += c;
			}


		}

		int twosCompPartA = Integer.parseInt(binary.substring(0,8), 2);
		int twosCompPartB = Integer.parseInt(binary.substring(8), 2);
		returnValue[0] = (byte) twosCompPartA;
		returnValue[1] = (byte) twosCompPartB;
		return returnValue;
	}

	static public byte[] toAsciiHex(char character) {
		byte[] hexVal = new byte[2];
		hexVal[0] = 0x4B;

		int ascii = (int) character;
		String hex = Integer.toHexString(ascii);

		hexVal[1] = (byte)Integer.parseInt(hex, 16);

		return  hexVal;
	}

	public static void main(String args[]) {
		FileWriter out = null;
		try {
			out = new FileWriter("out.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = -1000; i < 2000; i++) {
			//System.out.print(i + " ");
			byte[] hex = toHexNum(i);
			String hexa = Integer.toHexString((int)hex[0] & 0xff);
			String hexb = Integer.toHexString((int)hex[1] & 0xff);
			String backToInt = toString(hex[0], hex[1]);
			try {
				out.append(hexa + " " + hexb + " " + backToInt + "\n");
			} catch (Exception e) {
				e.printStackTrace();
				try {
					out.close();
				} catch (Exception b) {
					b.printStackTrace();
				}
			}
		}
		try {
			out.close();
		} catch (Exception b) {
			b.printStackTrace();
		}
	}
}
