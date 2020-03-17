import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
 * Author:		Tyler Blanchard
 * Date:		10/31/2018
 * 
 * Description:		 takes a Hack Assembly Language Program (.asm) as input, 
 * 					 translate it into Hack Machine Program and save it as 
 * 					 the same name with different extension (.hack)
 */
public class Assembler {

	// Implement a static arrayList for the rom
	static ArrayList<String> rom = new ArrayList<String>();

	// Implement a static HashMap for the symbolTable
	static HashMap<String,Integer> symbolTable = new HashMap<String,Integer>();

	public static void main(String[] args) throws IOException {
		
		// the .asm file that the assembler is converting from
		String file = "Pong.asm";
		
		// makes a .hack file with the same name as the .asm file
		String outFile = file.substring(0, file.indexOf(".")) + ".hack";

		// Initialize symbolTable
		intializeSymbolTable(symbolTable);

		// does a firstPass in the .asm code
		firstPass(file);

		// does a secondPass in the .asm code
		secondPass(outFile, symbolTable);

	}

	/**
	 * Reads Symbols.txt into the symbolTable HashMap
	 *
	 * @param symbolTable The HashMap that Symbols.txt is being read into
	 */
	private static void intializeSymbolTable(HashMap<String,Integer> symbolTable) throws FileNotFoundException {
		// put Symbols.txt on to the file
		File symbol = new File("Symbols.txt");

		// scan objects from Symbols.txt
		Scanner sc = new Scanner(symbol);

		// reads Symbols.txt into symbolTable
		while (sc.hasNextLine()) {

			symbolTable.put(sc.next(),sc.nextInt());
		}

		// closes the scanner
		sc.close();
	}

	/**
	 * Reads the .asm file into the rom ArrayList. Only reading in the labels
	 * and code. Ignoring the comments and empty lines on the .asm file
	 *
	 * @param file Where the .asm file is saved on so it can be added to the ArrayList
	 */
	private static void firstPass(String file) throws FileNotFoundException {
		File fi = new File(file);
		
		// Initialize variables
		String label = "";
		String line = "";

		// scan objects from a text file
		Scanner sc = new Scanner(fi);
		// read from the file
		while(sc.hasNext()) {
			line = sc.nextLine();
			// remove comments
			if (line.indexOf("//") >= 0) {
				line = line.substring(0, line.indexOf("//"));
			}
			if (line.indexOf("(") >= 0) {
				//extract label (substring between "(" and ")"))
				label = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
				//add the label and value into symbol table
				symbolTable.put(label, rom.size());

				line = "";
			}
			// makes it so that it only assigns non empty lines to the arrayList
			if (!line.trim().isEmpty()) {
				// put the asm code into the array
				rom.add(line);
			}

		}
		// closes scanner
		sc.close();

	}

	/**
	 * Converts the asm code into hack code. Changes it depending on if it's
	 * A-instructions, non-symbol A-instructions, and C-instructions type. Uses the
	 * a printWriter to create a .hack file that holds the converted code
	 *
	 * @param outFile The name of the .hack file that the printWriter will make
	 * @param symbolTable Checks to see if the A-instructions matches a symbol within it
	 * 					  and has it equal the hack value that is saved on it when it matches a symbol.
	 * 					  Symbol is add to symbolTable if it is new.
	 */
	private static void secondPass(String outFile, HashMap<String,Integer> symbolTable) throws FileNotFoundException {
		// Initialize HashMaps for dest, comp, and jump
		Map<String,String> destMap= new HashMap<String,String>();
		Map<String,String> compMap= new HashMap<String,String>();
		Map<String,String> jumpMap= new HashMap<String,String>();
		
		// Initialize PrintWriter
		PrintWriter pw = new PrintWriter(new File(outFile));

		// call method to put dest.txt in destMap
		makeDest(destMap);
		
		// call method to put comp.txt in compMap
		makeComp(compMap);
		
		// call method to put jump.txt in jumpMap
		makeJump(jumpMap);
		
		// Initialize variables
		String str = "";
		String symbol = "";
		int value = 0;
		int j = 0;

		// loops threw all values stored in rom
		for (int i = 0; i < rom.size(); i ++) {
			// put a line from rom into str
			str =  rom.get(i).trim();
			// Determines if the value is A-instruction
			if (str.contains("@")) {
				// ignores the @ in the code
				symbol = str.substring(1);
				try {
					value = Integer.parseInt(symbol);
				}
				catch(NumberFormatException e) {
					// if a symbol on the symbolTable
					if(symbolTable.containsKey(symbol)) {
						// Retrieve the value assigned to symbol
						value = symbolTable.get(symbol);
					}
					// if it isn't as symbol on the symbolTable
					else {

						// gives the first non-symbol A-Instructions the value of 16
						value = 16 + j;
						
						// increase value by one for the next non-symbol A-Instructions
						j++;
						
						// assigns the simple to the symbolTable
						symbolTable.put(symbol, value);

					}
				}
				// convert value to binary
				str = DecimalToBinary(value);

			}
			// if it is C-instructions
			else {
				// value for the front of the C-instructions
				String hack = "111";
				
				// Initialize the dest, comp, and jump
				String dest = "";
				String comp = "";
				String jump = "";

				// find the index for the '=' symbol, ';' symbol,
				// and end of text
				int destEnd = str.indexOf("=");
				int jmpIdx = str.indexOf(";");
				int compIdx = 0;
				int compEnd = str.length();

				// Initialize the dest
				if (destEnd > -1) {
					dest = str.substring(0, destEnd);
					
					// Initialize the start of the comp
					compIdx = destEnd + 1;
				}
				// Initialize the jump
				if (jmpIdx > -1) {
					jump = str.substring(jmpIdx + 1, compEnd);
					
					// Initialize the end or he comp
					compEnd = jmpIdx;
				}
				// if jump is non-existent make it equal to null
				if (jump == "") {
					jump = "null";
				}
				// if dest is non-existent make it equal to null
				if (dest == "") {
					dest = "null";
				}
				
				// Initialize the comp
				comp = str.substring(compIdx, compEnd);

				// translate the comp into hack format
				for (Map.Entry<String, String> entry : compMap.entrySet()) {
					// if the comp equals the key in the compMap set it equal to the value that is paired with the key
					if(entry.getKey().equals(comp)) {
						comp = entry.getValue();
					}

				}
				// translate the jump into hack format
				for (Map.Entry<String, String> entry : jumpMap.entrySet()) {
					// if the jump equals the key in the jumpMap set it equal to the value that is paired with the key
					if(entry.getKey().equals(jump)) {
						jump = entry.getValue();
					}

				}
				// translate the dest into hack format
				for (Map.Entry<String, String> entry : destMap.entrySet()) {
					// if the dest equals the key in the destMap set it equal to the value that is paired with the key
					if(entry.getKey().equals(dest)) {
						dest = entry.getValue();
					}

				}

				// Combines the 111, comp, dest, and jump to show the 16 bit hack code
				str = hack + comp + dest + jump;
			}
			// writes a line in the .hack file
			pw.append(str + "\n");
		}

		// creates the .hack file
		pw.flush();
		
		// closes the print writer
		pw.close();

	}
	
	/**
	 * takes a value converts it to binary in String type
	 *
	 * @param value The value that will be converted to binary
	 * @return The binary of the value which will be added to the .hack file
	 */
	private static String DecimalToBinary(int value) {
		// converts the value into binary
		String binary = Integer.toBinaryString(value);
		
		// adds 0 in front of binary until it is to the 16 bit binary
		while (binary.length() < 16) {
			binary = "0" + binary;
		}
		// return binary
		return binary;

	}

	/**
	 * Reads jump.txt into the jumpMap HashMap
	 *
	 * @param jumpMap The hashMap that the jump.txt file is being read onto
	 */
	private static void makeJump(Map<String, String> jumpMap) throws FileNotFoundException {
		// put jump.txt on to the file
		File f = new File("jump.txt");

		// scan objects from jump.txt
		Scanner sc = new Scanner(f);

		// reads jump.txt into jumpMap
		while (sc.hasNextLine()) {

			jumpMap.put(sc.next(),sc.next());
		}

		// close scanner
		sc.close();

	}

	/**
	 * Reads comp.txt into the compMap HashMap
	 *
	 * @param compMap The hashMap that the comp.txt file is being read onto
	 */
	private static void makeComp(Map<String, String> compMap) throws FileNotFoundException {
		// put comp.txt on to the file
		File f = new File("comp.txt");

		// scan objects from comp.txt
		Scanner sc = new Scanner(f);

		// reads comp.txt into compMap
		while (sc.hasNextLine()) {

			compMap.put(sc.next(),sc.next());
		}
		// close scanner
		sc.close();

	}

	/**
	 * Reads dest.txt into the destMap HashMap
	 *
	 * @param destMap The hashMap that the dest.txt file is being read onto
	 */
	private static void makeDest(Map<String, String> destMap) throws FileNotFoundException {
		// put dest.txt on to the file
		File f = new File("dest.txt");

		// scan objects from dest.txt
		Scanner sc = new Scanner(f);

		// reads dest.txt into destMap
		while (sc.hasNextLine()) {

			destMap.put(sc.next(),sc.next());
		}
		// close scanner
		sc.close();
	}

}
