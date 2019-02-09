package KK;

import java.util.*;
import java.io.*;
import java.util.regex.Pattern;

public class KKLanguageDictionary {

    private Hashtable<String, String> language;

    KKLanguageDictionary(){
        language = new Hashtable<>();
        try (
                BufferedReader in = new BufferedReader(new FileReader("LanguageRepresentation.txt"))
        ) {
            String inLine;
            while ((inLine = in.readLine()) != null) {
                if (!inLine.startsWith("#")) {
                    if (Pattern.matches("[A-Z]+ 0x[0-9A-Z]{4}",inLine)) {
                        String[] code = inLine.split(" ");
                        String[] key = code[1].split("x");
                        String check;
                        if ((check = setValue(key[1], code[0])) != null) {
                            System.out.println("Overwritten entry for key: " + key[1] + " with " + code[0] + ". Was " + check + "previously.");
                        }
                    }
                    else {
                        System.out.println("Issue with entry: " + inLine);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getValue(String key) {
        return language.get(key);
    }

    public String setValue(String key, String value) {
        return language.put(key, value);
    }

    public int getSize() {
        return language.size();
    }

    public void printList(){
        Enumeration keys = language.keys();
        System.out.printf("Key\tTranslation%n");
        while (keys.hasMoreElements()) {
            String el = (String) keys.nextElement();
            System.out.printf("%s\t%s%n", el, getValue(el));
        }
    }

    /*public static void main(String args[]) {
        KKLanguageDictionary at = new KKLanguageDictionary();
        System.out.println("Size: " + at.getSize());
        at.printList();
    }*/
}
