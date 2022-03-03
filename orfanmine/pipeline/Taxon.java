package orfanmine.pipeline;

import orfanmine.util.DBType;
import orfanmine.util.ORFanMineUtils;

public class Taxon {

	public static void main(String[] args) {

//		String[] argsNew = { "-taxon", "579446", "-type", "prot", "-db", "k:/ORFanMine/db/prot_classified" };
//		String[] argsNew = { "-taxon", "9605", "-exclude", "9606", "-type", "prot", "-db",
//				"k:/ORFanMine/db/prot_9605" };
//		args = argsNew;

		if (args.length < 4) {
			System.out.println("[Taxon] Error: Wrong syntax");
			printUsage();
			return;
		}

		String dbFilePath = null;
		String outputFilePath = null;
		Integer taxID = null;
		Integer taxExcludeID = null;
		DBType dbTypeArg = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-taxon":
				if ((i + 1) < args.length)
					taxID = Integer.valueOf(args[++i]);
				break;
			case "-type":
				if ((i + 1) < args.length)
					dbTypeArg = DBType.getDBTypeArg(args[++i]);
				break;
			case "-exclude":
				if ((i + 1) < args.length)
					taxExcludeID = Integer.valueOf(args[++i]);
				break;
			case "-db":
				if ((i + 1) < args.length)
					dbFilePath = args[++i];
				break;
			case "-out":
				if ((i + 1) < args.length)
					outputFilePath = args[++i];
				break;
			}
		}
		if (dbTypeArg == null) {
			System.out.println(
					"[Taxon] Error: Argument \"-type\". Mandatory value is missing:  `String, `nucl', `prot''");
			printUsage();
			return;
		}
		if (taxID == null) {
			System.out.println("[Taxon] Missing the taxon ID...");
			printUsage();
			return;
		}
		if (dbFilePath == null) {
			if (dbTypeArg == DBType.NUCL) {
				dbFilePath = ORFanMineUtils.getNuclDB();
			} else {
				dbFilePath = ORFanMineUtils.getProtDB();
			}
		}
		OperationalPartition partition;
		if (taxExcludeID == null) {
			if (outputFilePath == null)
				outputFilePath = ORFanMineUtils.getQueryOperational();
			partition = new OperationalPartition(dbFilePath, dbTypeArg, outputFilePath, taxID);
			partition.extractDBTaxID();
		} else {
			String targetFilePath = outputFilePath;
			if (outputFilePath == null)
				targetFilePath = ORFanMineUtils.getDatabaseOperational();
			partition = new OperationalPartition(dbFilePath, dbTypeArg, targetFilePath, taxID, taxExcludeID);
			if (partition.extractDBTaxID() == 0)
				System.exit(1);
		}
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println(
				"\tjava orfanmine.pipeline.Taxon -type molecule_type -taxon taxonID [-exclude taxonID] [-db databaseFilePath] [-out outputFilePath]");
		System.out.println("\t\tmolecule_type: \"nucl\" || \"prot\"");
	}

}
