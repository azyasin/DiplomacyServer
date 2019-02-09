package bandana.gameServer;

import com.sun.org.apache.regexp.internal.RE;
import ddejonge.bandana.gameBuilder.DiplomacyGameBuilder;
import dip.daide.comm.DisconnectedException;
import dip.daide.comm.Token;
import dip.daide.comm.UnknownTokenException;
import es.csic.iiia.fabregues.dip.board.*;
import es.csic.iiia.fabregues.dip.comm.GameBuilder;
import es.csic.iiia.fabregues.dip.comm.daide.Conn;
import es.csic.iiia.fabregues.dip.orders.HLDOrder;
import es.csic.iiia.fabregues.dip.orders.MTOOrder;
import es.csic.iiia.fabregues.dip.orders.SUPMTOOrder;
import es.csic.iiia.fabregues.dip.orders.SUPOrder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TestingClass {
	public static void main(String[] args) {
		GameInfo gameInfo = new GameInfo("TestGame");
		ServerLogger serverLogger = new ServerLogger();
		GameManager gameManager = new GameManager(gameInfo, serverLogger);

	}

	static String[] convert(byte[] bits) {
		StringBuffer str = null;
		boolean inString = false;
		ArrayList list = new ArrayList(bits.length / 2);

		for(int i = 0; i < bits.length; i += 2) {
			if (bits[i] == 75) {
				if (!inString) {
					str = new StringBuffer();
					str.append('\'');
					inString = true;
				}

				str.append((char)bits[i + 1]);
			} else {
				if (inString) {
					str.append('\'');
					list.add(str.toString());
					str = null;
					inString = false;
				}

				try {
					list.add(Token.convert(new byte[]{bits[i], bits[i + 1]}));
				} catch (UnknownTokenException var17) {
					if (list.size() > 0 && ((String)list.get(0)).equals("HUH")) {
						list.add(Integer.toHexString(bits[i]) + "," + Integer.toHexString(bits[i + 1]));
					} else {
						System.err.println("Unknown token encountered after these tokens:");
						Iterator iter = list.iterator();

						while(iter.hasNext()) {
							System.err.print(iter.next() + " ");
						}

						System.err.println();

						try {
							byte[] errMessage = new byte[bits.length + 8];
							byte[] HUH = Token.convert("HUH");
							errMessage[0] = HUH[0];
							errMessage[1] = HUH[1];
							byte[] PAR = Token.convert("(");
							errMessage[2] = PAR[0];
							errMessage[3] = PAR[1];
							System.arraycopy(bits, 0, errMessage, 4, i);
							byte[] ERR = Token.convert("ERR");
							errMessage[i + 4] = ERR[0];
							errMessage[i + 5] = ERR[1];
							System.arraycopy(bits, i, errMessage, i + 6, bits.length - i);
							PAR = Token.convert(")");
							errMessage[bits.length + 6] = PAR[0];
							errMessage[bits.length + 7] = PAR[1];
							/*byte[] dm = this.createDiplomaticMessage(errMessage);

							try {
								this.send(290, dm, 34);
							} catch (DisconnectedException var15) {
								var15.printStackTrace();
							}*/
						} catch (UnknownTokenException var16) {
							//;
						}

						list.add("UNKNOWN");
					}
				}
			}
		}

		return (String[])list.toArray(new String[0]);
	}
}
