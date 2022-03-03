package orfanmine.dbcuration;

import orfanmine.util.DBType;
import orfanmine.util.NcbiDBType;
import orfanmine.util.OrthoDBType;
import orfanmine.util.OrthologDBType;

public class OrthoDBExport {

	public final static String filePathArg = "-db";
	public final static String dbTypeArg = "-type";
	public final static String orthoTypeArg = "-ortho";

	public static void main(String[] args) {
		long startTime = System.nanoTime();

//		String[] argsLocal = { "-db", "c:\\eclipse\\eclipse\\workspace\\ORFanBaseSpring\\bin\\prot", "-type", "prot",
//				"-ortho", "e:\\NCBI\\Orthologs\\Gene\\gene_orthologs" }; // "e:\\OrthoDB\\odb10v1_gene_xrefs.tab"
//		args = argsLocal;

		if (args.length < 6) {
			System.out.println("[OrthoDBExport] Error: Wrong syntax");
			printUsage();
			return;
		}

		DBType dbTypeStrArg = null;
		String dbFilePath = null;
		String dbOrthoPath = null;

		for (int i = 0; i < args.length - 1; i++) {
			if (filePathArg.equals(args[i])) {
				dbFilePath = args[i + 1];
				i++;
				continue;
			}
			if (dbTypeArg.equals(args[i])) {
				dbTypeStrArg = DBType.getDBTypeArg(args[i + 1]);
				i++;
				continue;
			}
			if (orthoTypeArg.equals(args[i])) {
				dbOrthoPath = args[i + 1];
				i++;
			}
		}

		boolean inputError = false;
		if (dbFilePath == null) {
			System.out.println("[OrthoDBExport] Error: Missing argument \"-db\"");
			inputError = true;
		}
		if (dbTypeStrArg == null) {
			System.out.println(
					"[OrthoDBExport] Error: Missing argument \"-type\" or its mandatory value: String, 'nucl', 'prot'");
			inputError = true;
		}
		if (dbOrthoPath == null) {
			System.out.println("[OrthoDBExport] Error: Missing argument \"-ortho\"");
			inputError = true;
		}
		if (!OrthoDBType.getCmdParam().equals(dbOrthoPath) && !NcbiDBType.getCmdParam().equals(dbOrthoPath)) {
			System.out.println("[OrthoDBExport] Error: '" + dbOrthoPath
					+ "' is an unsupported orthologs' database type. \nSupported types: String, 'ortho', 'ncbi'");
			inputError = true;
		}
		if (inputError) {
			printUsage();
			return;
		}

//		String targetDBPath = "c:\\eclipse\\eclipse\\workspace\\ORFanBaseSpring\\bin\\nr_accessions";
//		DBType dbTypeArg = DBType.NR;
//		String orthoDBArgPath = "e:\\NCBI\\Orthologs\\Gene\\gene_orthologs"; // "e:\\OrthoDB\\odb10v1_gene_xrefs.tab"

		OrthologDBType dbExport = null;
		if (OrthoDBType.getCmdParam().equals(dbOrthoPath))
			dbExport = new OrthoDBType(dbOrthoPath, dbFilePath, dbTypeStrArg);
		if (NcbiDBType.getCmdParam().equals(dbOrthoPath))
			dbExport = new NcbiDBType(dbOrthoPath, dbFilePath, dbTypeStrArg);

		// OrthoDB10v1: "e:\\OrthoDB\\odb10v1_gene_xrefs.tab"
		// "e:\\NCBI\\Orthologs\\Gene\\gene_orthologs"
		dbExport.removeOrthologsFromHeaders();

		long endTime = System.nanoTime();
		long timeInSeconds = (endTime - startTime) / 1000000000;
		System.out.println("Execution time: " + (timeInSeconds / 60) + " min " + timeInSeconds % 60 + " sec");
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println(
				"\tjava OrthoDBExport -db headers_database_file_path -type molecule_type -ortho orthologs_database");
		System.out.println("\t\tmolecule_type: \"nucl\" || \"prot\"");
		System.out.println("\t\torthologs_database: \"ortho\" || \"ncbi\"");
	}
}
