package bandana.gameServer;

public class MessageFormat {
	String lvl;
	String[] composition;

	MessageFormat(String level, String comp){
		lvl = level;
		composition = comp.split(" ");
	}
}
