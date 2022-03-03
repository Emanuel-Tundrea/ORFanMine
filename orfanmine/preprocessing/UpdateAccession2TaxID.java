package orfanmine.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import orfanmine.util.DBType;
import orfanmine.util.ORFanMineUtils;

public class UpdateAccession2TaxID {

	private String accession2TaxIDTablePath = null;
	private DBType dbType = null;

	// -file -> accession2taxID file path
	// -type -> -dbtype molecule_type: "nucl" || "prot"
	public static void main(String[] args) {

		// Timer
		long startTime = System.nanoTime();

//		String[] argsLocal = { "-file", "prot.txt", "-type", "prot" };
//		args = argsLocal;
		if (args.length < 4) {
			System.out.println("[UpdateAccession2TaxID] Error: Wrong syntax");
			printUsage();
			return;
		}

		DBType dbTypeArg = null;
		String accession2TaxIDTablePath = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-file":
				if ((i + 1) < args.length)
					accession2TaxIDTablePath = args[++i];
				break;
			case "-type":
				if ((i + 1) < args.length)
					dbTypeArg = DBType.getDBTypeArg(args[++i]);
				break;
			}
		}
		if (accession2TaxIDTablePath == null) {
			System.out.println("[UpdateAccession2TaxID] Error: Missing argument \"-file\" or its mandatory value");
			printUsage();
			return;
		}
		if (dbTypeArg == null) {
			System.out.println(
					"[UpdateAccession2TaxID] Error: Argument \"-type\". Mandatory value is missing:  `String, `nucl', `prot''");
			printUsage();
			return;
		}

		UpdateAccession2TaxID serviceObj = new UpdateAccession2TaxID(accession2TaxIDTablePath, dbTypeArg);

		serviceObj.updateAccession2TaxIDTable();

		long endTime = System.nanoTime();
		long timeInSeconds = (endTime - startTime) / 1000000000;
		System.out.println("Execution time: " + (timeInSeconds / 60) + " min " + (timeInSeconds % 60) + " sec");
	}

	public UpdateAccession2TaxID(String accession2TaxIDTablePath, DBType dbType) {
		this.accession2TaxIDTablePath = accession2TaxIDTablePath;
		this.dbType = dbType;
	}

	private void updateAccession2TaxIDTable() {
		BufferedReader br = ORFanMineUtils.openReader(accession2TaxIDTablePath);
		String target = null;
		if (dbType == DBType.NUCL)
			target = ORFanMineUtils.getNtAccession2TaxID();
		else // DBType.PROT
			target = ORFanMineUtils.getNrAccession2TaxID();
		BufferedWriter bw = ORFanMineUtils.getWriter(target, false);

		long count = 0;// total NT = 291,119,930 NR = 969,754,287

		String contentLine = null;
		try {
			contentLine = br.readLine();// skip the header
			contentLine = br.readLine();
			while (contentLine != null) {
				count++;
//				if (count % 1000000 == 0)
//					System.out.println("MILLION: " + count / 1000000);

//				1. Accession
//				   Accession of the sequence record, without a version. e.g. BA000005
//				2. Accession.version
//				   Accession of the sequence record together with the version number. 
//				   e.g. BA000005.3
//				   Some dead sequence records do not have any version number in which case the
//				   value in this column will be the accession followed by a dot. e.g. X53318.
//				3. TaxId
//				   Taxonomy identifier of the source organism for the sequence record. e.g. 9606
//				   If for some reason the source organism cannot be mapped to the taxonomy 
//				   database, the column will contain 0.
//				4. GI

				String[] strArray = contentLine.split("\\t");

				String accessionNumber = strArray[0];
				Integer taxID = Integer.valueOf(strArray[2]);

				bw.write(accessionNumber + " " + taxID + "\n");

				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			System.out.println("Could not decode line: " + contentLine + "\n");
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		System.out.println("[updateAccession2TaxIDTable] Total count: " + count + "...");
	}

	public String getAccession2TaxIDTablePath() {
		return accession2TaxIDTablePath;
	}

	public DBType getDbType() {
		return dbType;
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println(
				"\tjava orfanbase.preprocessing.util.UpdateAccession2TaxID -file accession2taxIDFilePath -type molecule_type");
		System.out.println("\t\tmolecule_type: \"nucl\" || \"prot\"");
	}
}
