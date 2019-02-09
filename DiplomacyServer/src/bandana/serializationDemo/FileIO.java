package bandana.serializationDemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class FileIO {
	
	public static void main(String[] args) {
		//File file = new File("C:\\Users\\30044279\\Dropbox\\Testing1\\testing.txt");
		File file = new File("bogus");
		System.out.println(getPathExistenceReport(file));
		
		System.out.println("done!");
	}
	

	//////////////////////////////////////////////////
	//Functions to write to files	
	//////////////////////////////////////////////////
	
	public static File string2File(String filePath, String content, boolean append){
		return strings2File(filePath, toList(content), append);
	}
	
	public static File strings2File(String filePath, List<String> content, boolean append){
		File file = new File(filePath);
		strings2File(file, content, append);
		return file;

	}
	
	public static File string2File(String folderPath, String fileName, String content, boolean append){
		return strings2File(folderPath, fileName, toList(content), append);
	}
	
	public static File strings2File(String folderPath, String fileName, List<String> content, boolean append){
		File file = new File(folderPath, fileName);
		strings2File(file, content, append);
		return file;

	}
	
	public static File string2File(File folder, String fileName, String content, boolean append){
		return strings2File(folder, fileName, toList(content), append);
	}
	
	public static File strings2File(File folder, String fileName, List<String> content, boolean append){
		File file = new File(folder, fileName);
		strings2File(file, content, append);
		return file;
	}
	
	
	public static void string2File(File file, String content, boolean append){
		strings2File(file, toList(content), append);
	}
	
	public static void strings2File(File file, List<String> content, boolean append){
		
		//Make sure that the folder exists.
		file.getParentFile().mkdirs(); //if the folder already exists, nothing happens.
		
		//It is not necessary to make sure the file exists, because the file will be created by the FileWriter.
		
		try ( 
			PrintWriter out = new PrintWriter(new FileWriter(file, append));
		){
			for(String line : content){
				out.println(line);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static List<String> toList(String string){
		ArrayList<String> contentAsList = new ArrayList<>(1);
		contentAsList.add(string);
		return contentAsList;
	}
	
	
	public static void object2file(String filePath, Serializable object){
		File file = new File(filePath);
		object2file(file, object);
	}
	
	public static void object2file(String folderPath, String fileName, Serializable object){
		File file = new File(folderPath, fileName);
		object2file(file, object);
	}
	
	
	public static void object2file(File folder, String fileName, Serializable object){
		File file = new File(folder, fileName);
		object2file(file, object);
	}
	
	
	public static void object2file(File file, Serializable object){
		
		//Make sure that the folder exists.
		file.getParentFile().mkdirs(); //if the folder already exists, nothing happens.
		
		
		try (
			FileOutputStream fileOut = new FileOutputStream(file);
	    	ObjectOutputStream out = new ObjectOutputStream(fileOut);  
	    ){
			out.writeObject(object);
	      
	    }catch(IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	
	

	
	public static File createFolder(String parentFolderPath, String newFolderName){
		File folder = new File(parentFolderPath, newFolderName);
		folder.mkdirs();
		return folder;				
	}
	
	public static File createFolder(File parentFolder, String newFolderName){
		File folder = new File(parentFolder, newFolderName);
		folder.mkdirs();
		return folder;			
	}
	
	
	public static File createFolder(String FolderPath){
		File folder = new File(FolderPath);
		folder.mkdirs();
		return folder;		
	}	
	

	
	
	/**
	 * Creates a new file, unless it already exists.
	 * @param parentFolder
	 * @param fileName
	 * @return
	 */
	public static File createFile(File parentFolder, String fileName){
		
		parentFolder.mkdirs();
		
		File file = new File(parentFolder, fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return file;		
	}
	
	 public static String getDateString(){
			//Get the current time to put in the filename of the log file
			Calendar now = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss");
			return sdf.format(now.getTime());
	 }
	 
	
	 //////////////////////////////////////////////////
	 //Functions to read files	
	 //////////////////////////////////////////////////
	 public static ArrayList<String> file2Strings(String pathToFile) throws FileNotFoundException{
		 File inputFile = new File(pathToFile);
		 return file2Strings(inputFile);	
	 }
	 
	 public static ArrayList<String> file2Strings(String pathToFolder, String fileName) throws FileNotFoundException{
		 File inputFile = new File(pathToFolder, fileName);
		 return file2Strings(inputFile);	
	 }
	 
	 public static ArrayList<String> file2Strings(File folder, String fileName) throws FileNotFoundException{
		 File inputFile = new File(folder, fileName);
		 return file2Strings(inputFile);	
	 }
		
	 public static ArrayList<String> file2Strings(File inputFile) throws FileNotFoundException{
		 
		 if(!inputFile.exists()){
			 throw new FileNotFoundException(getPathExistenceReport(inputFile));
		 }
		 
			
		 ArrayList<String> lines = null;
			
		 try (
			BufferedReader br = new BufferedReader(new FileReader(inputFile))
		 ){
				
			 String line;
			 lines = new ArrayList<>();
			 while ((line = br.readLine()) != null) {
				 lines.add(line);
			 }
				
		} catch (IOException e) {
				e.printStackTrace();
		}
			
		return lines;
			
	 }
	
	
	 
	
	 //Deserialize an object
	 public static Serializable file2object(String serFilePath) throws FileNotFoundException{
		 return file2object(new File(serFilePath));
	 }
	 
	 public static Serializable file2object(String folderPath, String fileName) throws FileNotFoundException{
		 return file2object(new File(folderPath, fileName));
	 }
	 
	 public static Serializable file2object(File folder, String fileName) throws FileNotFoundException{
		 return file2object(new File(folder, fileName));
	 }
	 
	 public static Serializable file2object(File serFile) throws FileNotFoundException{
			
			
		 if(!serFile.exists()){
			 throw new FileNotFoundException(getPathExistenceReport(serFile));
		 }
			
		Serializable object = null;

		try (
			FileInputStream fileIn = new FileInputStream(serFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
	    ){
			object = (Serializable)in.readObject();
	       
	    }catch(IOException i) {
	    	i.printStackTrace();
	    }catch(ClassNotFoundException c) {
	        c.printStackTrace();
	    }
		
		return object;
	}
	 
	/**
	 * If the given file or folder does not exist, this method will return a message stating which part of its path does exist and which does not.
	 * @param file
	 * @return
	 */
	public static String getPathExistenceReport(File file){
		
		if(file.exists()){
			return "The given file or folder exists";
		}
		
		File nonExisting = file;
		File loopFile = file;
		while(loopFile != null && !loopFile.exists()){
			nonExisting = loopFile;
			loopFile = loopFile.getParentFile();
		}
		
		
		//case 1: no part of the given path exits:
		if(loopFile == null){
			
			return "Root folder " + nonExisting + " does not exist.";
			
		}else{ //case 2: tell which part does exist and which part does not.
			
			return "No file or folder with name " + nonExisting.getName() + " found at " + loopFile.getAbsolutePath();
			
		}
		
	}
	 

}
