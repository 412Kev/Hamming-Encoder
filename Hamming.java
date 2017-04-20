/* Kevin Rhea
 * Program 2
 * 4/20/17
 * This program reads in a message from data file. Then the encoder then takes the message, gets the codewords,
 * and writes it to an file called encoded.txt. Then the decoder takes this encoded file and 
 * decodes the codewords thus writing the original data to decoded.txt
 
 * The websites below were used as refrence
 	*The genMatrix[][] and h[][] matrices were used for generating and decoding the file 
 * http://michael.dipperstein.com/hamming/
 * http://www2.rad.com/networks/1994/err_con/hamming.htm
 
 * Testing String for dataFile: "abcdefghijklmnopqrstuvwxyz  1234567890 !@#$%^&*()_+-={[]}\|;'":<>,./?`~"
 */

import java.io.*;
import java.util.*;

public class Hamming  {
	private File file;
	private FileInputStream fi, fi2;
	private FileOutputStream fo, fo2;
	private Scanner sc;

	public static void main(String[]args) throws IOException {
		Hamming h = new Hamming("datafile.txt");
	}

	public Hamming(String datafile) throws IOException {
		file = new File(datafile);
		encode();
		decode();
	}
		
	public void encode() throws IOException {
		int x = 0;
		int count = 0;
		int[] data;

		// try to open input and then read the data into an array
		try {
			fi = new FileInputStream(file);
			fo = new FileOutputStream("encoded.txt");
			data = new int[fi.available()];

			//Read in the message and get codewords and outputs them
			while((x = fi.read()) != -1) {
				// Turn to binary string so it can be easily split
				String bin = Integer.toString(x, 2);
				String b1, b2;
				
				//Print out data and binary 
				System.out.println(x + "     " + bin);

				//Make sure binary string is 8 bits
				while(bin.length() < 8)
					bin = "0" + bin;
				
				//Split byte in half 
				b1 = bin.substring(0,4);
				b2 = bin.substring(4);
				System.out.println(b1 + "   " + b2);

				//Get the codewords of c1 and c2 and store in codeword arrays
				String codewords[] = {getCodeWord(b1), getCodeWord(b2)};
				System.out.println("Codewords : " + Arrays.toString(codewords));

				//Convert to Byte so it can be written by FileOutputStream
				byte cw[] = {Byte.parseByte(codewords[0] , 2), Byte.parseByte(codewords[1],2) };
				System.out.println(Arrays.toString(cw));
				fo.write(cw);
				count++;
			}
		}	 
		// if any error occurs catch it and print it
		catch(Exception ex) { ex.printStackTrace(); } 
	}

	public void decode() throws IOException {
		int x = 0;
		int count = 0;
		String[] data;

		//try to open input and then read the codewords into an array
		try {
			fi2 = new FileInputStream("encoded.txt");
			fo2 = new FileOutputStream("decoded.txt");
			
			//Read in the message
			data = new String[fi2.available()];		
			while((x = fi2.read()) != -1) {
				//Turn codeword into 8-bit string representation
				String bin = Integer.toString(x,2);
				while(bin.length() < 7){
					bin = "0" + bin;
				}
				data[count] = bin;
				count ++;
			}

			//Decode every 2 data elements, combine them, and write to decoded.txt
			String infobits[] = new String[data.length];
			for(int i=0; i< data.length; i+=2){
				System.out.println("codewords :" + data[i] + "   " + data[(i+1)]);
				
				//Convert to byte so it can be ouputted to decoded.txt
				Byte og = Byte.parseByte((getDecoded(data[i]) + getDecoded(data[(i+1)])), 2);
				fo2.write(og);
			}
		}	 
		// if any error occurs catch it and print it
		catch(Exception ex) { ex.printStackTrace(); } 
	}

	private String getCodeWord(String binary) {
		//Matrix that when multiplied by data produces codeword
		int genMatrix[][] = {{0,1,1,1,0,0,0},
							 {1,0,1,0,1,0,0},
							 {1,1,0,0,0,1,0},
							 {1,1,1,0,0,0,1}};

		//Turn string into int array so we can perform matrix multiplication
		int databits[] = new int[4];
		for(int i=0; i<4; i++)
			databits[i] = Character.getNumericValue(binary.charAt(i));
		
		//Multiply genMatrix by infobits
		String codeword = "";
		for(int i=0; i<7; i++){
			int sum = 0;
			for(int j=0; j<4; j++){
				sum += (databits[j] * genMatrix[j][i]);
			}
			codeword += (sum%2);
		}
		return codeword;
	}

	private String getDecoded(String codeword){
		System.out.println("\nDecoding " + codeword);
		//Matrix that when multiplied by codeword produces the origial data
		int h[][] = { {1,0,0,0,1,1,1},
					  {0,1,0,1,0,1,1},
					  {0,0,1,1,1,0,1} };
		
		//Turn that string into an int 
		int cw[] = new int[7];
		for(int i=0; i<7; i++)
			cw[i] = Character.getNumericValue(codeword.charAt(i));
		
		//Corrupt a bit randomly if Random number is 1:
		System.out.println("Code word int[]: " +  Arrays.toString(cw));
		
		//Decide whether or not to corrupt a bit in the codeword
		Random rg = new Random();
		int rand = rg.nextInt(2);
		if(rand == 1){
			cw = corruptBit(cw).clone();
			System.out.println("corrupted " +  Arrays.toString(cw));
		} else { System.out.println("No error "); }
		
		//Multiply cw by validating matrix to get synapse for error checking
		int synaspe[] = new int[3];
		for(int i=0; i<3; i++){
			int sum = 0;
			for(int j=0; j<7; j++){
				sum += (h[i][j]*cw[j]);
			}
			synaspe[i] = (sum%2);
		}
		System.out.println("Synaspe " + Arrays.toString(synaspe));
		
		//If the synaspe is not all 0's, then there is an error in the codeword
		int check = synaspe[0] + synaspe[1] + synaspe[2];
		int col[] = new int[3];
		if(check != 0){
			System.out.println("ERROR Detected");
			for(int i=0; i<7; i++){
				int count = 0;
				for(int j=0; j<3; j++){
					if(synaspe[j] == h[j][i]){
						col[j] = h[j][i];
						count++;
					}
					//If count gets to 3 then i is the position of the error
					if(count == 3){
						System.out.println("Error is at postion " + i );
						System.out.println("Column : " + Arrays.toString(col));
						//Flip the position of the error
						if (cw[i] == 1){  cw[i] = 0; }
						else { cw[i] = 1; }	
					}
				}
			}
		}
		System.out.println("Decoded Codeword :" + cw[3] + cw[4] + cw[5] + cw[6]);
		return ("" + cw[3] + cw[4] + cw[5] + cw[6]);
	}

	private int[] corruptBit(int[] codeword){
		Random rg = new Random();
		int n = rg.nextInt(7);
		System.out.println("original: " + Arrays.toString(codeword));
		System.out.println("----Corrupting Bit at postion----" + n);
		
		if(codeword[n] == 0) { codeword[n] = 1; } 
		else { codeword[n] = 0; }
		
		System.out.println("corrupted " + Arrays.toString(codeword));
		return codeword;
	}
}