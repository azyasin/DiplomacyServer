package bandana.gameServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DaideSyntax {


	//HashMaps for the respective language definition types in daide_syntax.pdf
	private HashMap<String, ArrayList<MessageFormat>> client_server_message = new HashMap<>();
	private HashMap<String, ArrayList<MessageFormat>> server_client_message = new HashMap<>();

	DaideSyntax(){

		int i;
		for (i = 0; i < Constants.client_server_message.length; i++) {
			String[] entry = Constants.client_server_message[i].split(" ");
			setCompositions(entry, "client_server_message");
		}

		for (i = 0; i < Constants.server_client_message.length; i++) {
			String[] entry = Constants.server_client_message[i].split(" ");
			setCompositions(entry, "server_client_message");
		}
	}


	//set for single format returns a string, null is success
	public String setCompositions(String[] entry, String htName) {
		HashMap<String, ArrayList<MessageFormat>> HashMap = getHashMap(htName);
		ArrayList<MessageFormat> formats;
		if (HashMap == null) {
			formats = new ArrayList<>();
		} else {
			formats = HashMap.get(entry[1]);
		}
		if (formats == null) {
			formats = new ArrayList<>();
		}
		int len = entry.length - 1;
		String sub = "";
		for (int i = 0; i < len; i++) {
			if (i != 0) {
				sub = sub + " ";
			}
			sub = sub + entry[i + 1];
		}
		MessageFormat current = new MessageFormat(entry[0], sub);
		formats.add(current);
		return setHashMap(htName, entry[1], formats);
	}


	//getformats func returns a list of message formats for the passed command
	public ArrayList<MessageFormat> getCompositions(String entry, String htName) {
		ArrayList<MessageFormat> formats = getHashMap(htName).get(entry);
		return formats;
	}

	//getformats func returns a list of message formats for the passed command
	public ArrayList<MessageFormat> getCompositions(String htName) {
		ArrayList<MessageFormat> formats = getOtherTables(htName);
		return formats;
	}

	private ArrayList<MessageFormat> getOtherTables(String htName) {
		ArrayList<MessageFormat> formats = new ArrayList<>();
		String[] list;
		switch (htName) {
			case "acknowledgable_command":
				list = Constants.acknowedgable_command;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "compound_order_result":
				list = Constants.compound_order_result;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "client_command":
				list = Constants.client_command;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "client_request":
				list = Constants.client_request;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "explanation":
				list = Constants.explanation;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "future_offer":
				list = Constants.future_offer;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "logical_operator":
				list = Constants.logical_operator;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "mdf_adjacencies":
				list = Constants.mdf_adjacencies;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "mdf_centre_list":
				list = Constants.mdf_centre_list;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "mdf_coast":
				list = Constants.mdf_coast;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "mdf_coast_adjacencies":
				list = Constants.mdf_coast_adjacencies;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "mdf_province":
				list = Constants.mdf_province;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "mdf_province_adjacencies":
				list = Constants.mdf_province_adjacencies;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "mdf_province_list":
				list = Constants.mdf_province_list;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "mdf_provinces":
				list = Constants.mdf_provinces;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "mdf_supply_centres":
				list = Constants.mdf_supply_centres;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "negatable_command":
				list = Constants.negatable_command;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "negatable_query":
				list = Constants.negatable_query;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "negated_client_request":
				list = Constants.negated_client_request;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "negated_server":
				list = Constants.negated_server_message;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "arrangement":
				list = Constants.arrangement;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "order":
				list = Constants.order;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "order_note":
				list = Constants.order_note;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "order_result":
				list = Constants.order_result;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "period":
				list = Constants.period;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "power_list":
				list = Constants.power_list;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "power_summary":
				list = Constants.power_summary;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "press_message":
				list = Constants.press_message;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "province_list":
				list = Constants.province_list;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "province_and_coast":
				list = Constants.province_and_coast;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "query":
				list = Constants.query;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "reply":
				list = Constants.reply;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "rejectable_client_command":
				list = Constants.rejectable_client_command;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "sc_ownership_list":
				list = Constants.sc_ownership_list;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "sco_aList":
				list = Constants.sco_entry;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "send_message":
				list = Constants.send_message;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "think_and_fact":
				list = Constants.think_and_fact;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "try_parameters":
				list = Constants.try_parameters;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "try_token_client_to_server":
				list = Constants.try_token_client_to_server;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "try_token_server_to_client":
				list = Constants.try_token_server_to_client;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "turn":
				list = Constants.turn;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "unit_with_location":
				list = Constants.unit_with_location;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "occ_unit_with_location":
				list = Constants.occ_unit_with_location;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "unit_with_location_and_mrt":
				list = Constants.unit_with_location_and_mrt;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "variant":
				list = Constants.variant;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "variant_option":
				list = Constants.variant_option;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			case "why_sequence":
				list = Constants.why_sequence;
				for (String aList : list) {
					formats.add(stringToMessageFormat(aList));
				}
				break;
			default:
				formats = null;
					break;
		}
		return formats;
	}

	//prints the formats
	public void printCompositions(String htName) {
		HashMap<String, ArrayList<MessageFormat>> HashMap = getHashMap(htName);
		for (Map.Entry<String, ArrayList<MessageFormat>> entry : HashMap.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue().get(0).composition[0]);
		}
	}

	private HashMap<String, ArrayList<MessageFormat>> getHashMap(String name) {
		HashMap<String, ArrayList<MessageFormat>> HashMap;
		switch (name) {
			case "client_server_message":
				HashMap = client_server_message;
				break;
			case "server_client_message":
				HashMap = server_client_message;
				break;
			default:
				HashMap = null;
				break;
		}
		return HashMap;
	}

	private String setHashMap(String name, String key, ArrayList<MessageFormat> value) {
		String returnValue = "";
		switch (name) {
			case "client_server_message":
				if (client_server_message.put(key, value) == null) {
					returnValue = null;
				} else {
					returnValue = "Overwritten " + key;
				}
				break;
			case "server_client_message":
				if (server_client_message.put(key, value) == null) {
					returnValue = null;
				} else {
					returnValue = "Overwritten " + key;
				}
				break;
			default:
				returnValue = "Overwritten " + key;
				break;
		}
		return returnValue;
	}

	private MessageFormat stringToMessageFormat(String entry) {
		String[] list = entry.split(" ");
		String sub = "";
		for (int j = 0; j < list.length; j++ ){
			if(j != 0) {
				sub = sub + list[j];
				if (j != list.length - 1) {
					sub = sub + " ";
				}
			}
		}
		return new MessageFormat(list[0], sub);
	}

	//Test Driver for debugging
	public static void main(String[] args) {

		DaideSyntax test = new DaideSyntax();
		//test.printCompositions("client_server_message");
		//test.printCompositions("server_client_message");
		System.out.println("OBS for client_server_message:");
		ArrayList<MessageFormat> frmt = test.getCompositions("NME", "client_server_message");
		for (MessageFormat i : frmt) {
			for (String j : i.composition) {
				System.out.print(j + " ");
			}
			System.out.println();
		}
	}
}
	