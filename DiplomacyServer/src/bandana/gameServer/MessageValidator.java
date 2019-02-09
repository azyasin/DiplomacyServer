package bandana.gameServer;

import java.util.ArrayList;
import java.util.List;

/* *
 * Validates the message against the DAIDE syntax protocol
 * */

public class MessageValidator {
	private int startLocation;

	//this function runs basic validity check based on DAIDE syntax.
	static public ValidationValue isValid(Message message, int level) {
		if (!checkBrackets(message)) {
			return new ValidationValue(false, -2);
		}
		ArrayList<MessageFormat> formats;
		String entry = Constants.dictionary.getEntry(Integer.toHexString(message.messageContent[0]), Integer.toHexString(message.messageContent[1]));
		if (entry == "SND") {
			formats = Constants.daideSyntax.getCompositions("send_message");
		}
		else {
			formats = Constants.daideSyntax.getCompositions(entry, "client_server_message");
		}
		ValidationValue validationValue = checkFormatsList(formats, message.messageContent, 0, level);
		return validationValue;
	}

	static private ValidationValue checkFormatsList(ArrayList<MessageFormat> formats, byte[] message, int startLocation, int level) {
		ValidationValue errorLocation = new ValidationValue(true, startLocation);
		if (formats == null) {
			return new ValidationValue(false, 0);
		}
		for (MessageFormat format : formats) {
			ValidationValue loc = null;
			if (!format.lvl.equals("OBS")) {
				if (Integer.parseInt(format.lvl) > level) {
					return new ValidationValue(false, -1);
				}
			}
			loc = checkFormat(format.composition, message, startLocation, level);
			if (loc.isValid()) {
				return loc;
			}
			else {
				errorLocation = loc;
			}
		}
		return errorLocation;
	}

	static private ValidationValue checkFormat(String[] format, byte[] message, int startLocation, int level) {
		boolean oneOrMore = false;
		String[] oOMType = new String[1];
		int startOfOOM = 0;
		int dotdotdot = 0;
		byte half_token = 0;
		for (int i = 0; i < format.length; i++) {
			if (format[i].equals("...")) {
				oneOrMore = true;
				oOMType = getDouble(format, i);
				if (oOMType == null) {
					return new ValidationValue(false, startLocation);
				}
				startOfOOM = i - (oOMType.length * 2);
				dotdotdot = i;
			}
		}
		for (int i = 0; i < format.length; i++) {
			if (oneOrMore && startLocation < message.length) {
				if (i == startOfOOM) {
					half_token = message[startLocation];
				}
				if (format[i].equals("...")) {
					if (message[startLocation] == half_token) {
						i = startOfOOM;
					}
				}
			}
			if (format[i].length() <= 3) {
				if (startLocation + 1 < message.length) {
					String token = TokenTranslator.toString(message[startLocation], message[startLocation + 1]);
					if (!token.equals(format[i])) {
						if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
							return new ValidationValue(false, startLocation);
						}
						else {
							i = dotdotdot;
						}
					}
				}
				else {
					return new ValidationValue(false, startLocation);
				}
			} else {
				if (startLocation + 1 >= message.length) {
					return new ValidationValue(false, startLocation - 1);
				}
				switch (format[i]) {
					case "any_token_sequence":
						if (!token_sequence(format, message, startLocation, i)) {
							if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
								return new ValidationValue(false, startLocation);
							}
							else {
								i = dotdotdot;
							}
						}
						break;
					case "bad_bracketed_sequence":
						if (token_sequence(format, message, startLocation, i)) {
							if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
								return new ValidationValue(false, startLocation);
							}
							else {
								i = dotdotdot;
							}
						}
						break;
					case "coast":
						if (!checkCategory(message[startLocation], message[startLocation + 1], "coast")) {
							if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
								return new ValidationValue(false, startLocation);
							}
							else {
								i = dotdotdot;
							}
						}
						break;
					case "number":
						if (message[startLocation] > 0x3F) {
							if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
								return new ValidationValue(false, startLocation);
							}
							else {
								i = dotdotdot;
							}
						}
						break;
					case "power":
						if (!checkCategory(message[startLocation], message[startLocation + 1], "power")) {
							if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
								return new ValidationValue(false, startLocation);
							}
							else {
								i = dotdotdot;
							}
						}
						break;
					case "province":
						if (!checkProvince(message[startLocation], message[startLocation + 1])) {
							if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
								return new ValidationValue(false, startLocation);
							}
							else {
								i = dotdotdot;
							}
						}
						break;
					case "season":
						if (!checkCategory(message[startLocation], message[startLocation + 1], "season")) {
							if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
								return new ValidationValue(false, startLocation);
							}
							else {
								i = dotdotdot;
							}
						}
						break;
					case "string":
						if (message[startLocation] != 0x4B) {
							if (!checkCategory(message[startLocation], message[startLocation + 1], "season")) {
								if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
									return new ValidationValue(false, startLocation);
								}
								else {
									i = dotdotdot;
								}
							}
						}
						while (message[startLocation] == 0x4B) {
							startLocation += 2;
						}
						startLocation -= 2;
						break;
					case "unit_type":
						if (!checkCategory(message[startLocation], message[startLocation + 1], "unit_type")) {
							if (!checkCategory(message[startLocation], message[startLocation + 1], "season")) {
								if (!(oneOrMore && i > startOfOOM && i < dotdotdot)) {
									return new ValidationValue(false, startLocation);
								}
								else {
									i = dotdotdot;
								}
							}
						}
						break;
					default:
						ArrayList<MessageFormat> newFormats = Constants.daideSyntax.getCompositions(format[i]);

						if (newFormats != null) {
							ValidationValue check = checkFormatsList(newFormats, message, startLocation, level);
							if (check.isValid()) {
								return check;
							}
						} else {
							return new ValidationValue(false, startLocation);
						}
				}
			}
			startLocation += 2;
		}
		return new ValidationValue(true, startLocation);
	}

	static private boolean token_sequence(String[] format, byte[] message, int msgLocation, int formatLocation) {
		int len = format.length - 1;
		int open = 0;
		int close = 0;
		String current_token;
		/**
		 * check if any_token_sequence is only followed by a single closing bracket
		 * any_token_sequence should always be at the end, bar a single closing bracket
		 * as per daide_syntax.pdf
		 */
		if (formatLocation != format.length - 1) {
			return false;
		}
		/**
		 * Count opening and closing brackets in the any token sequence
		 */
		while (msgLocation < len) {
			current_token = TokenTranslator.toString(message[msgLocation], message[msgLocation + 1]);
			if (current_token.compareTo("(") == 0) {
				open++;
			}
			if (current_token.compareTo(")") == 0) {
				close++;
			}
			msgLocation += 2;
		}
		/**
		 * check brackets in token sequence are balanced
		 */
		if (open != close) {
			return false;
		}
		return true;
	}

	static private boolean checkCategory(byte cat, byte entry, String category) {
		int hex;
		switch (category) {
			case "coast":
				hex = 0x46;
				break;
			case "power":
				hex = 0x41;
				break;
			case "season":
				hex = 0x47;
				break;
			case "unit_type":
				hex = 0x42;
				break;
			default:
				return false;
		}

		if (cat != hex) {
			return false;
		}

		String translatedToken = TokenTranslator.toString(cat, entry);
		if (translatedToken.length() != 3) {
			return false;
		}

		return true;
	}

	static private boolean checkProvince(byte cat, byte entry) {
		if (cat < 0x50 || cat > 0x57) {
			//System.out.println("ret false on category " + cat);
			return false;
		}
		String province = TokenTranslator.toString(cat, entry);
		if (province.length() != 3) {
			//System.out.println("ret false on province " + province);
			return false;
		}
		return true;
	}

	static private boolean checkBrackets(Message message) {
		int open = 0;
		int close = 0;
		for (int i = 0; i < message.messageContent.length; i+=2) {
			if (message.messageContent[i] == (byte)0x40) {
				if (message.messageContent[i+1] == (byte)0x00) {
					open++;
				}
				else if (message.messageContent[i+1] == (byte)0x01) {
					close++;
				}
			}
		}
		if (open == close) {
			return true;
		}
		return false;
	}

	public static String[] getDouble(String[] format, int pos) {
		List<String> first = new ArrayList<>();
		List<String> second = new ArrayList<>();
		for (int i = pos - 1; i >= 0; i--) {
			if (first.size() > 0) {
				if (first.contains(format[i])) {
					second.add(format[i]);
				}
				else if (second.equals(first)) {
					int k = 0;
					String[] ret = new String[first.size()];
					for(int j = first.size() - 1; j >= 0; j--) {
						ret[k] = first.get(j);
						k++;
					}
					return ret;
				}
				else {
					first.add(format[i]);
				}
			}
			else {
				first.add(format[i]);
			}
		}
		return null;
	}

	public static void main(String args[]) {
		byte[] sub = {(byte) 0x48, (byte) 0x17, (byte) 0x40, (byte) 0x0, (byte) 0x41, (byte) 0x3, (byte) 0x40,
				(byte) 0x1, (byte) 0x40, (byte) 0x0, (byte) 0x4a, (byte) 0x13, (byte) 0x40, (byte) 0x1};
		Message test = new Message((byte)0x02, sub);
		String[] msg = new String[]{"de", "a", "b", "c", "a", "b", "c", "..."};
		String[] ret = getDouble(msg, 7);
		for (int i = 0; i < ret.length; i++) {
			System.out.println(ret[i]);
		}
	}
}