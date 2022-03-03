package orfanmine.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.TreeSet;

import orfanmine.util.ORFanMineUtils;

public class SiftOrthologs {

	private Integer taxID;
	private String dbFilePath;
	private String orthoFilePath;
	private String outputFilePath = null;

	public static void main(String[] args) {

		if (args.length < 4) {
			System.out.println("[SiftOrthologs] Error: Wrong syntax");
			printUsage();
			return;
		}

		Integer taxID = null;
		String dbFilePath = null;
		String orthoFilePath = null;
		String outputFilePath = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-taxon":
				if ((i + 1) < args.length)
					taxID = Integer.valueOf(args[++i]);
				break;
			case "-db":
				if ((i + 1) < args.length)
					dbFilePath = args[++i];
				break;
			case "-ortho":
				if ((i + 1) < args.length)
					orthoFilePath = args[++i];
				break;
			case "-out":
				if ((i + 1) < args.length)
					outputFilePath = args[++i];
				break;
			}
		}
		if (taxID == null) {
			System.out.println("[SiftOrthologs] Missing the taxon ID...");
			printUsage();
			return;
		}
		if (orthoFilePath == null) {
			System.out.println("[SiftOrthologs] Missing the ortholog database file path...");
			printUsage();
			return;
		}
		if (dbFilePath == null) {
			dbFilePath = ORFanMineUtils.getQueryOperational();
		}
		SiftOrthologs siftOrthologs;
		if (outputFilePath == null)
			siftOrthologs = new SiftOrthologs(taxID, dbFilePath, orthoFilePath);
		else
			siftOrthologs = new SiftOrthologs(taxID, dbFilePath, orthoFilePath, outputFilePath);
		siftOrthologs.execute();

		// Homo Sapiens (9606)
		// Drosophila melanogaster (7227)
		// C. elegans (6239)
	}

	public SiftOrthologs(Integer taxID, String dbFilePath, String orthoFilePath) {
		this.taxID = taxID;
		this.dbFilePath = dbFilePath;
		this.orthoFilePath = orthoFilePath;
	}

	public SiftOrthologs(Integer taxID, String dbFilePath, String orthoFilePath, String outputFilePath) {
		this(taxID, dbFilePath, orthoFilePath);
		this.outputFilePath = outputFilePath;
	}

	public void execute() {
		try {
			File orthoFile = new File(orthoFilePath);
			if (orthoFile.length() == 0) {
				orthoFile.delete();
//				System.out.println(orthoFilePath + " is empty!");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		BufferedReader br = ORFanMineUtils.openReader(orthoFilePath);

		TreeSet<String> orthologs = new TreeSet<String>();

		try {
			String readLine = br.readLine();
			while (readLine != null) {
				// [0] AccessionNumber + "." + Version
				// [1] identity percent
				// [2] Subject AccessionNumber
				// [3] Subject taxID
				String[] strArray = readLine.split("\t");
				Double identityPercentage = Double.valueOf(strArray[1]);
				Integer subjectTaxID = Integer.valueOf(strArray[3]);
				if ((identityPercentage > ORFanMineUtils.getIdentityPercentage()) && (!subjectTaxID.equals(taxID))) {
					String accessionNumber = strArray[0].substring(0, strArray[0].indexOf("."));
					orthologs.add(accessionNumber);
				}
				readLine = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
//			System.out.println("Orthologs count: " + orthologs.size());
		}

		br = ORFanMineUtils.openReader(dbFilePath);
		String tempFilePath = null;
		if (outputFilePath != null)
			tempFilePath = outputFilePath;
		else
			tempFilePath = ORFanMineUtils.getTmpFilePath(ORFanMineUtils.getQueryOperational());
		BufferedWriter bw = ORFanMineUtils.getWriter(tempFilePath, false);

		long count = 0;
		long countTRGs = 0;
		boolean saveLine = false;
		try {
			String readLine = br.readLine();
			while (readLine != null) {
				if (readLine.startsWith(">")) {
					saveLine = false;
					count++;
					// [0] ">" + AccessionNumber + "." + Version
					// [1] taxID
					// [2..] annotations
					String[] strArray = readLine.split(" ");
					String accessionNumber = strArray[0].substring(1, strArray[0].indexOf("."));
					if (!orthologs.contains(accessionNumber)) {
						saveLine = true;
						countTRGs++;
					}
				}
				if (saveLine)
					bw.write(readLine + "\n");
				readLine = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
			if (outputFilePath == null)
				ORFanMineUtils.replaceFile(dbFilePath, tempFilePath);

//			System.out.println("Count total sequences: " + count);
			System.out.println("Found orthologs in " + orthoFilePath + ": " + orthologs.size());
			if (orthologs.size() > 0)
				System.out.println("Remaining TRGs: " + countTRGs);
			if (countTRGs == 0) {
				System.exit(1);
			}
		}
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println(
				"\tjava orfanbase.pipeline.SiftOrthologs -taxon taxonID -ortho orthologDatabaseFilePath [-db databaseFilePath] [-out outputFilePath]");
	}
}
