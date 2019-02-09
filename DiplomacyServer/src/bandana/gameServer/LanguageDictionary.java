package bandana.gameServer;

import ddejonge.bandana.gameBuilder.DiplomacyGameBuilder;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Province;

import java.util.*;
import java.util.HashMap;

public class LanguageDictionary {

    private HashMap<String, HashMap<String, String>> language = null;
	private HashMap<String, byte[]> hexCode;


	LanguageDictionary() {
    	language = new HashMap<>();
		hexCode= new HashMap<>();

		String cat = "";
		HashMap<String, String> catList = new HashMap<>();
		catList.clear();
		for (int i = 0; i < Constants.tokens.length; i++) {
			String b1 = Constants.tokens[i][1];
			String b2 = Constants.tokens[i][2];
			String entry = Constants.tokens[i][0];
			setEntry(b1, b2, entry);
			setHexCode(entry, b1 + b2);
		}
	}

	public HashMap<String, String> getCategory(byte b1) {
    	String b = byteToString(b1);
    	b = fixCase(fixLength(b));
    	return language.get(b);
	}

	public HashMap<String, String> getCategory(String b1) {
		return language.get(fixCase(fixLength(b1)));
	}

	public String getEntry(byte b1, byte b2) {
		String bc = fixCase(fixLength(byteToString(b1)));
		String be = fixCase(fixLength(byteToString(b2)));
		HashMap<String, String> category = getCategory(bc);
    	return category.get(be);
	}

	public String getEntry(String b1, String b2) {
		HashMap<String, String> category = getCategory(fixCase(fixLength(b1)));
		return category.get(fixCase(fixLength(b2)));
	}

	public HashMap<String, String> setCategory(String cat, HashMap<String, String> entries) {
		return language.put(fixCase(fixLength(cat)), entries);
	}

	public String setEntry(String b1, String b2, String entry) {
		HashMap <String, String> category;
		String ret = "";
		category = getCategory(fixCase(fixLength(b1)));
		if (category == null) {
			category = new HashMap<>();

		}
		ret = category.put(fixCase(fixLength(b2)), entry);
		setCategory(fixCase(fixLength(b1)), category);
		return ret;
	}

	public void printDictionary() {
		language.forEach((k,v)->{
			System.out.println("Item : " + k + " Value : " + v);
		});
	}

	public void printCoder() {
		hexCode.forEach((k,v)->{
			//if (k.equals("BLD") || k.equals("WVE") || k.equals("REM")) {
				System.out.print("Item : " + k + " Value : " + fixCase(fixLength(byteToString(v[0]))));
				int b = v[1] & 0xff;
				System.out.println(fixCase(fixLength(Integer.toHexString(b))));
			//}
		});
	}


	//function to set the hex entry and returns null if no clash of the value that was replaced
	public byte[] setHexCode(String name, String hex) {
		byte [] code = new byte[2];
		if (hex.length() == 4) {
			code[0] = (byte)Integer.parseInt(hex.substring(0,2), 16);
			code[1] = (byte)Integer.parseInt(hex.substring(2), 16);
		}
		else if (hex.length() == 4) {
			code[0] = (byte)Integer.parseInt(hex.substring(0,1), 16);
			code[1] = (byte)Integer.parseInt(hex.substring(1), 16);
		}
		else {
			code[0] = 0x00;
			code[1] = (byte)Integer.parseInt(hex, 16);
		}
		return hexCode.put(name, code);
	}


	//this function gets the hexcode based on the 3 letters passed
	public byte[] getHexCode(String name) {
		return hexCode.get(name);
	}

	public String byteToString(byte b) {
		return Integer.toHexString((int)b & 0xff);
	}

	public String fixLength(String b) {

		if (b.length() == 1) {
    		return "0" + b;
		}
		return b;
	}



	public String fixCase(String b) {
    	return b.toUpperCase();
	}

	public static void main(String args[]) {
        LanguageDictionary at = new LanguageDictionary();
		Game game = new DiplomacyGameBuilder().createDefaultGame();
		for (Province province: game.getProvinces()) {
			System.out.println(province.getName() + ": " + TokenTranslator.toHexBytes(province.getName()));
		}
    }
}
