package orfanmine.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import orfanmine.util.ORFanMineUtils;

public class UpdateTaxNames {

	private String namesDumpFilePath = null;

	// args[0] - names dump file path
	public static void main(String[] args) {

		// Timer
		long startTime = System.nanoTime();

		if (args.length < 1) {
			System.out.println("[UpdateTaxNames] Error: Please specify the names dump file path!");
			printUsage();
			return;
		}

		UpdateTaxNames serviceObj = new UpdateTaxNames(args[0]);// "e:\\NCBI\\names.dmp"

		serviceObj.updateTaxNamesTable();

		long endTime = System.nanoTime();
		long timeInSeconds = (endTime - startTime) / 1000000000;
		System.out.println("Execution time: " + (timeInSeconds / 60) + " min " + (timeInSeconds % 60) + " sec");
	}

	public UpdateTaxNames(String namesDumpFilePath) {
		this.namesDumpFilePath = namesDumpFilePath;
	}

	private boolean updateTaxNamesTable() {
		if (namesDumpFilePath == null) {
			System.out.println("[UpdateTaxNodes] Error: Names dump file path NULL!");
			printUsage();
			return false;
		}

		BufferedReader br = ORFanMineUtils.openReader(namesDumpFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(ORFanMineUtils.getTaxDBNamesFilePath(), false);

		long count = 0;// total = 3,328,821

		String contentLine = null;
		try {
			contentLine = br.readLine();
			while (contentLine != null) {
				count++;
//				[0] - tax_id		-- the id of node associated with this name
//				[1] - separator
//				[2] - name_txt		-- name itself
//				[3] - separator
//				[4] - unique name	-- the unique variant of this name if name not unique
//				[5] - separator
//				[6] - name class	-- (synonym, common name, ...)

				String[] strArray = contentLine.split("\\t");

				Integer tax_id = Integer.valueOf(strArray[0]);
				String name = strArray[2];
				String scientific_name = strArray[6];
				if (scientific_name.equals("scientific name"))
					bw.write(tax_id + "\t" + name + "\n");

				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			System.out.println("Could not decode line: " + contentLine + "\n");
			ioe.printStackTrace();
			return false;
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		System.out.println("[updateTaxNamesTable] Line total: " + count + "...");
		return true;
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println("\tjava orfanbase.preprocessing.util.UpdateTaxNames namesDumpFilePath");
	}
}
