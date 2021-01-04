package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Configuration {
	public static String getDBAdress() {
		try {
			String adr = Paths.get("").toAbsolutePath().toString() + "/dbAdress.txt";
			File myObj = new File(adr);
			Scanner myReader = new Scanner(myObj);
			String data = myReader.nextLine();
			myReader.close();
			return data;

		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return null;
	}
}
