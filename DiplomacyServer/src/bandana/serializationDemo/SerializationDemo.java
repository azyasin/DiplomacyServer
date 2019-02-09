package bandana.serializationDemo;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;

import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;

public class SerializationDemo {

	//STATIC FIELDS

	//STATIC METHODS
	
	/**
	 * Demonstrates how to serialize a Game object to file, and how to write a text file.
	 * Note that you can replace the 'Game' class with any other class that implements the Serializable interface.
	 * Also, note that you must use the dip-1.6b library, because the Game class in the dip-1.6 library does not implement Serializable.
	 * @param game
	 * @throws FileNotFoundException 
	 */
	public static void saveDemo(Game game) throws FileNotFoundException{
		
		String folderPath = "C:\\Users\\Dave\\some folder\\"; 	//The folder where you want to store the serialization file. If the folder does not exist, it will be created.
		String serializationFile = "serializationFile.ser";		//The name you want to give to the serialization file. It is custom to give it the file extension .ser
		String textFile = "textFile.txt";					//The name you want to give to the text file.
		
		//Save the game object to a file:
		//FileIO.object2file(folderPath, serializationFile, game);
		FileIO.object2file(folderPath, serializationFile);
		
		//create some text to write to a text file:
		ArrayList<String> content = new ArrayList<>();
		content.add("Hello");
		content.add("World!");
		
		boolean append = true; //If this is set to 'false' and the text file already exists, then all the existing content of the file will be deleted and replaced with the new content.
		//Otherwise, if set to 'true', the new content will simply be appended to the file.
		
		FileIO.strings2File(folderPath, textFile, content,  append);
		
		

		
		
	}
	
	public static void loadDemo() throws FileNotFoundException{
		
		String folderPath = "C:\\Users\\Dave\\some folder\\"; 	//The path to the folder where you want to store the serialization file. If the folder does not exist, it will be created.
		String serializationFile = "serializationFile.ser";		//The name you want to give to the serialization file. It is custom to give it the file extension .ser
		String textFile = "textFile.txt";
		
		
		//Load a game object from file:
		Serializable loadedObject = FileIO.file2object(folderPath, serializationFile);
		Game game = (Game)loadedObject; //Cast the object to the Game class.
		//(this only works because we already know that the file contains an object of class Game).
		
		//Print out the owned supply centers and units for each power.
		for(Power power : game.getPowers()){
			System.out.println(power.getName());
			System.out.println("   Supply centers owned:");
			for(Province sc : power.getOwnedSCs()){
				System.out.println("   " + sc.getName());
			}
			System.out.println("   Units:");
			for(Region unit : power.getControlledRegions()){
				System.out.println("   " + unit.getName());
			}
		}
		
		
		
		
		
		
		
		//Load the text file:
		ArrayList<String> content = FileIO.file2Strings(folderPath, textFile);
		
		//and print its contents:
		for (String string : content) {
			System.out.println(string);
		}
		
	}
	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
