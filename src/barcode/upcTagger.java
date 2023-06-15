package barcode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class upcTagger {
	private int gtin8, gtin12F, gtin12T, gtin13F, gtin13T, gtin14F, gtin14T, other;
	private String[][] labelToCompany;
	public upcTagger() {
		labelToCompany = new String[][] {
			{"great value", "Walmart"},
			{"equate", "Walmart"},
			{"sams choice", "Walmart"},
			{"parents choice","Walmart"},
			{"marketside", "Walmart"},
			
			//Kro
			{"Kroger","Kroger"},
			{"simple truth","Kroger"},
			{"private selection","Kroger"},
			{"Comforts", "Kroger"},
			
			{"cvs","CVS"},
			{"Gold emblem","CVS"},
			
			{"Nice", "Walgreens"},
			{"walgreen", "Walgreens"},
			{"Finest Nutrition", "Walgreens"},
			
			//GE
			{"Giant Eagle", "Giant Eagle"},
			{"Market District", "Giant Eagle"},
			{"Natures Basket", "Giant Eagle"},
			{"TopCare","Giant Eagle"},
			{"Top Care","Giant Eagle"},
			
			//RA
			{"Rite Aid", "Rite Aid"},
			
			//Food 4 less?
			
			{"Ralphs","Ralphs"},
			
			{"Safeway","Safeway"},
			
			{"O ORGANICS","Albertsons"},
			{"Signature","Albertsons"},
			{"Lucerne","Albertsons"},
			{"Open Nature","Albertsons"},
			
			{"Culinaria","Schnucks"},
			{"Schnucks","Schnucks"}
			
			};
	}
	
	/**
	 * Tags a list of barcodes and outputs into a new text file
	 * @param in Path of the file with barcodes. Assumes .txt, barcode is at the start of each line and ends with a comma, one barcode per line
	 * @param out Path of file that will be written to
	 * 			[Barcode] | [Length] | [Tag] | [Check Digit]
	 */
	public void tagBatch(String in, String out) {
		BufferedReader reader;
		BufferedWriter writer;
		
	    try {
			reader = new BufferedReader(new FileReader(in));
			writer = new BufferedWriter(new FileWriter(out));
			String line,code,name;
			line = reader.readLine();
			writer.write("ProductCode,ProductCodeType,Description,SubCategoryCode,ProgramCode,CodeLength,GTINClass,CheckDigit,Company\n");
			while ((line = reader.readLine()) != null) {
				code = line.substring(0,line.indexOf(','));
				int index = line.indexOf(',',code.length()+1)+1;
				name = line.substring(index, line.indexOf(',',index));
				String text = line + "," + tagUPC(code) + tagCompany(name) + getGTIN14(code) + "\n";
				writer.write(text);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
	
	/**
	 * Identifies a string as a GTIN classification if possible
	 * @param sCode GTIN string to process
	 * @return GTIN-[NUNBER]-[T (checkdigit)/ F (no checkdigit)] | checkDigit
	 * 			else UNKNOWN
	 */
	public String tagUPC(String sCode) {
		String gtinType = "";
		long code = 0;
		try {
			code = Long.parseLong(sCode);
		} catch(Exception e) {
			return "ERROR,-1";
		}
		int length = (code+"").length();
		
		switch(length) {
		case 8: gtinType = "GTIN-8,-1"; 
			gtin8++;
			break;
		case 11: gtinType = "GTIN-12-F," + checkDigit(code); 
			gtin12F++;
			break;
		case 12: if(checkDigit(code/10) == code%10) {
					gtinType = "GTIN-12-T," + code%10; 
					gtin12T++;
					break;
				} else {
					gtinType = "GTIN-13-F," + checkDigit(code); 
					gtin13F++;
					break;
				}
		case 13: if(checkDigit(code/10) == code%10) {
					gtinType = "GTIN-13-T," + code%10;
					gtin13T++;
					break;
				} else {
					gtinType = "GTIN-14-F," + checkDigit(code);
					gtin14F++;
					break;
				}
		case 14: gtinType = "GTIN-14-T," + code%10; 
			gtin14T++;
			break;
		default: gtinType = "UNKNOWN,-1";
			other++;
			break;
		}
		
		return sCode.length() + "," + gtinType;
	}
	
	public String tagCompany(String product) {
		String company = "N/A";
		for(String[] label:labelToCompany) {
			if(product.toLowerCase().replaceAll("[^a-z ]", "").indexOf(label[0].toLowerCase()) == 0) {
				company = label[1];
				break;
			}
		}
		return ","+company;
	}
	
	public String getGTIN14(String sCode) {
		long code = 0;
		try {
			code = Long.parseLong(sCode);
		} catch(Exception e) {
			return "ERROR,-1";
		}
		int length = (code+"").length();
		
		if(length == 11) 
			code = code*10 + checkDigit(code);
		else if(length == 12 || length == 14) 
			if(!(code%10 == checkDigit(code/10))) code = code*10 + checkDigit(code);
		
		String standard = "00000000000000" + code;
		
		return ','+standard.substring(standard.length()-14);
	}
	
	/**
	 * Calculates the check digit of a given barcode
	 * @param code Barcode
	 * @return Check digit
	 */
	public int checkDigit(long code) {
		int sum = 0;
		boolean count = false;
		
		while(code > 0) {
			sum += (count?1:3) * code%10;
			code /= 10;
			count = !count;
		}
		
		return (10-sum%10)%10;
	}
	
	//run
	public static void main(String[] args) {
		new upcTagger().tagBatch("NB_APL_05.23.23.txt", "output.txt");
	}
}


