package bandana.gameServer;

import java.lang.String;
public class Constants {
	/**
	 * The default port used by Parlance. The BANDANA server should use the same port for compatibility reasons.
	 */
	public static final int DEFAULT_PORT = 16713; //don't change this value.

	/**
	 * Array of Standard Tokens
	 */

	public static final String[][] tokens = new java.lang.String[][]{{"AUS", "41", "00"},
		{"ENG", "41", "01"}, {"FRA", "41", "02"}, {"GER", "41", "03"}, {"ITA", "41", "04"}, {"RUS", "41", "05"},
		{"TUR", "41", "06"}, {"AMY", "42", "00"}, {"FLT", "42", "01"}, {"CTO", "43", "20"}, {"CVY", "43", "21"},
		{"HLD", "43", "22"}, {"MTO", "43", "23"}, {"SUP", "43", "24"}, {"VIA", "43", "25"}, {"DSB", "43", "40"},
		{"RTO", "43", "41"}, {"BLD", "43", "80"}, {"REM", "43", "81"}, {"WVE", "43", "82"}, {"MBV", "44", "00"},
		{"BPR", "44", "01"}, {"CST", "44", "02"}, {"ESC", "44", "03"}, {"FAR", "44", "04"}, {"HSC", "44", "05"},
		{"NAS", "44", "06"}, {"NMB", "44", "07"}, {"NMR", "44", "08"}, {"NRN", "44", "09"}, {"NRS", "44", "0A"},
		{"NSA", "44", "0B"}, {"NSC", "44", "0C"}, {"NSF", "44", "0D"}, {"NSP", "44", "0E"}, {"NSU", "44", "10"},
		{"NVR", "44", "11"}, {"NYU", "44", "12"}, {"YSC", "44", "13"}, {"SUC", "45", "00"}, {"BNC", "45", "01"},
		{"CUT", "45", "02"}, {"DSR", "45", "03"}, {"FLD", "45", "04"}, {"NSO", "45", "05"}, {"RET", "45", "06"},
		{"NCS", "46", "00"}, {"NEC", "46", "02"}, {"ECS", "46", "04"}, {"SEC", "46", "06"}, {"SCS", "46", "08"},
		{"SWC", "46", "0A"}, {"WCS", "46", "0C"}, {"NWC", "46", "0E"}, {"SPR", "47", "00"}, {"SUM", "47", "01"},
		{"FAL", "47", "02"}, {"AUT", "47", "03"}, {"WIN", "47", "04"}, {"CCD", "48", "00"}, {"DRW", "48", "01"},
		{"FRM", "48", "02"}, {"GOF", "48", "03"}, {"HLO", "48", "04"}, {"HST", "48", "05"}, {"HUH", "48", "06"},
		{"IAM", "48", "07"}, {"LOD", "48", "08"}, {"MAP", "48", "09"}, {"MDF", "48", "0A"}, {"MIS", "48", "0B"},
		{"NME", "48", "0C"}, {"NOT", "48", "0D"}, {"NOW", "48", "0E"}, {"OBS", "48", "0F"}, {"OFF", "48", "10"},
		{"ORD", "48", "11"}, {"OUT", "48", "12"}, {"PRN", "48", "13"}, {"REJ", "48", "14"}, {"SCO", "48", "15"},
		{"SLO", "48", "16"}, {"SND", "48", "17"}, {"SUB", "48", "18"}, {"SVE", "48", "19"}, {"THX", "48", "1A"},
		{"TME", "48", "1B"}, {"YES", "48", "1C"}, {"ADM", "48", "1D"}, {"SMR", "48", "1E"}, {"AOA", "49", "00"},
		{"BTL", "49", "01"}, {"ERR", "49", "02"}, {"LVL", "49", "03"}, {"MRT", "49", "04"}, {"MTL", "49", "05"},
		{"NPB", "49", "06"}, {"NPR", "49", "07"}, {"PDA", "49", "08"}, {"PTL", "49", "09"}, {"RTL", "49", "0A"},
		{"UNO", "49", "0B"}, {"DSD", "49", "0D"}, {"ALY", "4A", "00"}, {"AND", "4A", "01"}, {"BWX", "4A", "02"},
		{"DMZ", "4A", "03"}, {"ELS", "4A", "04"}, {"EXP", "4A", "05"}, {"FWD", "4A", "06"}, {"FCT", "4A", "07"},
		{"FOR", "4A", "08"}, {"HOW", "4A", "09"}, {"IDK", "4A", "0A"}, {"IFF", "4A", "0B"}, {"INS", "4A", "0C"},
		{"OCC", "4A", "0E"}, {"ORR", "4A", "0F"}, {"PCE", "4A", "10"}, {"POB", "4A", "11"}, {"PRP", "4A", "13"},
		{"QRY", "4A", "14"}, {"SCD", "4A", "15"}, {"SRY", "4A", "16"}, {"SUG", "4A", "17"}, {"THK", "4A", "18"},
		{"THN", "4A", "19"}, {"TRY", "4A", "1A"}, {"VSS", "4A", "1C"}, {"WHT", "4A", "1D"}, {"WHY", "4A", "1E"},
		{"XDO", "4A", "1F"}, {"XOY", "4A", "20"}, {"YDO", "4A", "21"}, {"CHO", "4A", "22"}, {"BCC", "4A", "23"},
		{"UNT", "4A", "24"}, {"BOH", "50", "00"}, {"BUR", "50", "01"}, {"GAL", "50", "02"}, {"RUH", "50", "03"},
		{"SIL", "50", "04"}, {"TYR", "50", "05"}, {"UKR", "50", "06"}, {"BUD", "51", "07"}, {"MOS", "51", "08"},
		{"MUN", "51", "09"}, {"PAR", "51", "0A"}, {"SER", "51", "0B"}, {"VIE", "51", "0C"}, {"WAR", "51", "0D"},
		{"ADR", "52", "0E"}, {"AEG", "52", "0F"}, {"BAL", "52", "10"}, {"BAR", "52", "11"}, {"BLA", "52", "12"},
		{"EAS", "52", "13"}, {"ECH", "52", "14"}, {"GOB", "52", "15"}, {"GOL", "52", "16"}, {"HEL", "52", "17"},
		{"ION", "52", "18"}, {"IRI", "52", "19"}, {"MAO", "52", "1A"}, {"NAO", "52", "1B"}, {"NTH", "52", "1C"},
		{"NWG", "52", "1D"}, {"SKA", "52", "1E"}, {"TYS", "52", "1F"}, {"WES", "52", "20"}, {"ALB", "54", "21"},
		{"APU", "54", "22"}, {"ARM", "54", "23"}, {"CLY", "54", "24"}, {"FIN", "54", "25"}, {"GAS", "54", "26"},
		{"LVN", "54", "27"}, {"NAF", "54", "28"}, {"PIC", "54", "29"}, {"PIE", "54", "2A"}, {"PRU", "54", "2B"},
		{"SYR", "54", "2C"}, {"TUS", "54", "2D"}, {"WAL", "54", "2E"}, {"YOR", "54", "2F"}, {"ANK", "55", "30"},
		{"BEL", "55", "31"}, {"BER", "55", "32"}, {"BRE", "55", "33"}, {"CON", "55", "34"}, {"DEN", "55", "35"},
		{"EDI", "55", "36"}, {"GRE", "55", "37"}, {"HOL", "55", "38"}, {"KIE", "55", "39"}, {"LON", "55", "3A"},
		{"LVP", "55", "3B"}, {"MAR", "55", "3C"}, {"NAP", "55", "3D"}, {"NWY", "55", "3E"}, {"POR", "55", "3F"},
		{"ROM", "55", "40"}, {"RUM", "55", "41"}, {"SEV", "55", "42"}, {"SMY", "55", "43"}, {"SWE", "55", "44"},
		{"TRI", "55", "45"}, {"TUN", "55", "46"}, {"VEN", "55", "47"}, {"BUL", "57", "48"}, {"SPA", "57", "49"},
		{"STP", "57", "4A"}};

	public static final String[] client_server_message = {"OBS NME ( string ) ( string )",
			"OBS OBS", "OBS IAM ( power ) ( number )", "OBS MAP", "OBS MDF", "OBS YES ( acknowledgable_command )",
			"OBS REJ ( acknowledgable_command )", "OBS NOW", "OBS SCO", "OBS HST ( turn )", "OBS TME ( number )",
			"OBS TME", "OBS ADM ( string ) ( string )", "OBS PRN ( bad_bracketed_sequence )",
			"OBS HUH ( any_token_sequence )", "0 HLO", "0 SUB ( order ) ( order ) ...",
			"0 SUB ( turn ) ( order ) ( order ) ...", "0 NOT ( negatable_command )", "0 MIS", "0 GOF", "0 ORD",
			"0 DRW", "10 DRW ( power power_list )", "10 send_message"};

	public static final String[] server_client_message = {"OBS YES ( client_command )",
			"OBS YES ( client_request )", "OBS REJ ( client_request )", "OBS REJ ( rejectable_client_command )",
			"OBS MAP ( string )", "OBS MDF ( power_list ) ( mdf_provinces ) ( mdf_adjacency_list )",
			"OBS HLO ( power ) ( number ) ( variant )",
			"OBS NOW ( turn ) ( unit_with_location_and_mrt ) ( unit_with_location_and_mrt ) ...",
			"OBS SCO ( sco_entry ) ( sco_entry ) ...",
			"0 THX ( order ) ( order_note )",
			"0 MIS ( unit_with_location_and_mrt ) ( unit_with_location_and_mrt ) ...",
			"0 MIS ( number )", "OBS ORD ( turn ) ( order ) ( compound_order_result )",
			"0 SVE ( string )", "0 LOD ( string )", "OBS OFF", "OBS TME ( number )",
			"OBS PRN ( bad_bracketed_sequence )",
			"OBS HUH ( any_token_sequence )", "OBS CCD ( power )",
			"OBS NOT ( negated_server_message )", "OBS ADM ( string ) ( string )",
			"OBS SLO ( power )", "OBS DRW", "OBS SMR ( turn ) ( power_summary ) ( power_summary ) ...",
			"10 DRW ( power power_list )", "10 OUT ( power )",
			"10 FRM ( power number ) ( power_list ) ( press_message )",
			"10 FRM ( power number ) ( power_list ) ( reply )"};

	public static final String[] acknowedgable_command = {"OBS MAP ( string )", "OBS SVE ( string )"};

	public static final String[] compound_order_result = {"OBS order_note", "OBS order_result",
			"OBS order_note RET", "OBS order_result RET"};

	public static final String[] client_command = {"OBS OBS", "OBS NOT ( TME )"};

	public static final String[] client_request = {"OBS NME ( string ) ( string )", "OBS IAM ( power ) ( number )",
			"0 NOT ( GOF )", "0 GOF", "OBS TME ( number )", "OBS DRW", "OBS NOT ( negated_client_request )",
			"10 DRW ( power power_list )", "10 send_message"};

	public static final String[] explanation = {"80 EXP ( turn ) ( reply )"};

	public static final String[] future_offer = {"10 PCE ( power power_list )",
			"10 ALY ( power_list ) VSS ( power_list )", "10 DRW", "10 SLO ( power )", "10 NOT ( future_offer )",
			"20 XDO ( order )", "20 DMZ ( power_list ) ( province_list )",
			"40 SCD ( sc_ownership_list ) ( sc_ownership_list ) ...",
			"40 OCC ( occ_unit_with_location ) ( occ_unit_with_location ) ...",
			"50 AND ( future_offer ) ( future_offer ) ...",
			"50 ORR ( future_offer ) ( future_offer ) ...",
			"50 CHO ( number number ) ( future_offer ) ( future_offer ) ...", "110 XOY ( power ) ( power )",
			"110 YDO ( power ) ( unit_with_location ) ( unit_with_location ) ...",
			"120 SND ( power ) ( power_list ) ( press_message )",
			"120 SND ( power ) ( power_list ) ( reply )", "120 FWD ( power_list ) ( power ) ( power )",
			"120 BCC ( power ) ( power_list ) ( power)"};

	public static final String[] logical_operator = {"30 AND ( arrangement ) ( arrangement ) ...",
			"30 ORR ( arrangement ) ( arrangement ) ..."};

	public static final String[] mdf_adjacencies = {"OBS ( mdf_province_adjacencies ) ( mdf_province_adjacencies ) ..."};

	public static final String[] mdf_coast = {"OBS FLT coast"};

	public static final String[] mdf_centre_list = {"OBS power province_list", "OBS UNO province_list",
			"OBS ( power_list ) province_list"};

	public static final String[] mdf_coast_adjacencies = {"OBS unit_type mdf_province mdf_province ...",
			"OBS ( mdf_coast ) mdf_province mdf_province ..."};

	public static final String[] mdf_province = {"OBS province", "OBS ( province_and_coast )"};

	public static final String[] mdf_province_adjacencies = {"province ( mdf_coast_adjacencies ) ( mdf_coast_adjacencies ) ..."};

	public static final String[] mdf_province_list = {"OBS mdf_province mdf_province ..."};

	public static final String[] mdf_provinces = {"OBS ( mdf_supply_centres ) ( province_list )"};

	public static final String[] mdf_supply_centres = {"OBS ( mdf_centre_list ) ( mdf_centre_list ) ..."};

	public static final String[] negatable_command = {"OBS SUB ( order )", "OBS SUB", "OBS GOF", "OBS DRW",
			"OBS DRW ( power power_list )", "OBS TME", "OBS TME ( number )"};

	public static final String[] negatable_query = {"60 QRY ( arrangement )", "60 NOT ( query )"};

	public static final String[] negated_client_request = {"OBS TME ( number )", "OBS DRW", "10 DRW ( power power_list )"};

	public static final String[] negated_server_message = {"OBS CCD ( power )", "OBS TME ( number )"};

	public static final String[] arrangement = {"10 PCE ( power power_list )",
			"10 ALY ( power_list ) VSS ( power_list )", "10 DRW", "10 SLO ( power )", "10 NOT ( arrangement )",
			"10 NAR ( arrangement )", "20 XDO ( order )", "20 DMZ ( power_list ) ( province_list )",
			"40 SCD ( sc_ownership_list ) ( sc_ownership_list ) ...",
			"40 OCC ( occ_unit_with_location ) ( occ_unit_with_location ) ...",
			"50 AND ( arrangement ) ( arrangement ) ...", "50 ORR ( arrangement ) ( arrangement ) ...",
			"50 CHO ( number number ) ( arrangement ) ( arrangement ) ...", "90 FOR ( turn ) ( future_offer )",
			"90 FOR ( period ) ( future_offer )", "110 XOY ( power ) ( power )",
			"110 YDO ( power ) ( unit_with_location ) ( unit_with_location ) ...",
			"120 SND ( power ) ( power_list ) ( press_message )", "120 SND ( power ) ( power_list ) ( reply )",
			"120 FWD ( power_list ) ( power ) ( power )", "120 BCC ( power ) ( power_list ) ( power )"};

	public static final String[] order = {"OBS ( unit_with_location ) HLD",
			"OBS ( unit_with_location ) MTO mdf_province",
			"OBS ( unit_with_location ) SUP ( unit_with_location )",
			"OBS ( unit_with_location ) SUP ( unit_with_location ) MTO province",
			"OBS ( unit_with_location ) CVY ( unit_with_location ) CTO province",
			"OBS ( unit_with_location ) CTO province VIA ( province_list )",
			"OBS ( unit_with_location ) RTO mdf_province", "OBS ( unit_with_location ) DSB",
			"OBS ( unit_with_location ) BLD", "OBS ( unit_with_location ) REM", "OBS power WVE"};

	public static final String[] order_note = {"OBS MBV", "OBS FAR", "OBS NSP", "OBS NSU", "OBS NAS", "OBS NSF",
			"OBS NSA", "OBS NYU", "OBS NRN", "OBS NVR", "OBS YSC", "OBS ESC", "OBS HSC", "OBS NSC", "OBS CST",
			"OBS NMB", "OBS NMR", "OBS NRS"};

	public static final String[] order_result = {"OBS SUC", "OBS BNC", "OBS CUT", "OBS DSR", "OBS NSO"};

	public static final String[] period = {"90 ( turn ) ( turn"};

	public static final String[] power_list = {"OBS power power ..."};

	public static final String[] power_summary = {"OBS power ( string ) ( string ) number",
			"OBS power ( string ) ( string ) 0 number"};

	public static final String[] press_message = {"10 PRP ( arrangement )", "10 TRY ( try_parameters )",
			"10 CCL ( press_message )", "30 PRP ( logical_operator )", "60 INS ( arrangement )",
			"60 QRY ( arrangement )", "60 SUG ( arrangement )", "60 THK ( arrangement )", "610 FCT ( arrangement )",
			"70 WHT ( unit_with_location )", "70 HOW ( province )", "70 HOW ( power )", "80 EXP ( turn ) ( reply )",
			"100 IFF ( arrangement ) THN ( press_message )",
			"100 IFF ( arrangement ) THN ( press_message ) ELS ( press_message )",
			"120 FRM ( power number ) ( power_list ) ( press_message )", "120 FRM ( power number ) ( power_list ) ( reply )",
			"8000 string"};

	public static final String[] province_list = {"OBS province province ..."};

	public static final String[] province_and_coast = {"OBS province coast"};

	public static final String[] query = {"60 QRY ( arrangement )"};

	public static final String[] reply = {"10 YES ( press_message )", "10 REJ ( press_message )",
			"10 BWX ( press_message )", "10 HUH ( any_token_sequence )", "60 THK ( negatable_query )",
			"60 FCT ( negatable_query )", "60 IDK ( query )", "80 YES ( explanation )", "80 REJ ( explanation )",
			"80 IDK ( explanation )", "80 SRY ( explanation )", "130 WHY ( think_and_fact )",
			"130 POB (why_sequence )", "130 WHY ( SUG ( arrangement ) )", "130 WHY ( PRP ( arrangement ) )",
			"130 WHY ( INS ( arrangement ) )", "130 IDK ( PRP ( arrangement ) )", "130 IDK ( INS ( arrangement ) )",
			"130 IDK ( SUG ( arrangement ) )", "10 press_message"};

	public static final String[] rejectable_client_command = {"OBS HLO", "OBS NOW", "OBS SCO", "OBS HST ( turn )",
			"0 SUB ( order ) ( order ) ...", "OBS ORD", "OBS TME", "OBS ADM ( string ) ( string )"};

	public static final String[] sc_ownership_list = {"OBS power province_list", "OBS UNO province_list"};

	public static final String[] sco_entry = {"60 QRY ( arrangement )"};

	public static final String[] send_message = {"10 SND ( power_list ) ( press_message )",
			"10 SND ( power_list ) ( reply )", "10 SND ( turn ) ( power_list ) ( press_message )",
			"10 SND ( turn ) ( power_list ) ( reply )"};

	public static final String[] think_and_fact = {"130 THK ( arrangement )", "130 FCT ( arrangement )"};

	public static final String[] try_parameters = {"10 empty", "10 try_token try_token ..."};

	public static final String[] try_token_client_to_server = {"10 PRP", "10 PCE", "10 ALY", "10 VSS", "10 DRW",
			"10 SLO", "10 NOT", "10 NAR", "10 YES", "10 REJ", "10 BWX", "10 CCL", "10 XDO", "10 DMZ", "10 AND",
			"10 ORR", "10 SCD", "10 OCC", "10 INS", "10 QRY", "10 THK", "10 FCT", "10 IDK", "10 SUG", "10 WHT",
			"10 HOW", "10 EXP", "10 SRY", "10 FOR", "10 IFF", "10 THN", "10 ELS", "10 XOY", "10 YDO", "10 FRM",
			"10 FWD", "10 SND", "10 WHY", "10 POB"};

	public static final String[] try_token_server_to_client = {"10 PRP", "10 PCE", "10 ALY", "10 VSS", "10 DRW",
			"10 SLO", "10 NOT", "10 NAR", "10 YES", "10 REJ", "10 BWX", "10 CCL", "20 XDO", "20 DMZ", "30 AND",
			"30 ORR", "40 SCD", "40 OCC", "60 INS", "60 QRY", "60 THK", "610 FCT", "60 IDK", "60 SUG", "70 WHT",
			"70 HOW", "80 EXP", "80 SRY", "90 FOR", "100 IFF", "100 THN", "100 ELS", "110 XOY", "110 YDO",
			"120 FRM", "120 FWD", "120 SND", "130 WHY", "130 POB"};

	public static final String[] turn = {"OBS season number"};

	public static final String[] unit_with_location = {"OBS power unit_type province",
			"OBS power unit_type ( province_and_coast )"};

	public static final String[] occ_unit_with_location = {"50 power unit_type province",
			"50 power unit_type ( province_and_coast )", "50 power UNT province"};

	public static final String[] unit_with_location_and_mrt = {"OBS power unit_type province",
			"OBS power unit_type ( province_and_coast )",
			"OBS power unit_type province MRT ( mdf_province_list )",
			"OBS power unit_type ( province_and_coast ) MRT ( mdf_province_list )"};

	public static final String[] variant = {"OBS ( variant_option ) ( variant_option ) ..."};

	public static final String[] variant_option = {"OBS LVL number", "OBS MTL number", "OBS RTL number",
			"OBS BTL number", "OBS AOA", "OBS DSD", "10 PDA", "10 PTL number", "10 NPR", "10 NPB"};

	public static final String[] why_sequence = {"130 WHY ( think_and_fact )"};

	/**
	 * Language Dictionary used to convert from tokens to human readable
	 */
	
	public static final LanguageDictionary dictionary = new LanguageDictionary();
	
	public static final MessageValidator validator = new MessageValidator();

	public static final DaideSyntax daideSyntax = new DaideSyntax();
}
