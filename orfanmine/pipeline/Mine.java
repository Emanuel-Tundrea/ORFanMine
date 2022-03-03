package orfanmine.pipeline;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.TreeSet;

import orfanmine.util.DBType;
import orfanmine.util.ORFanMineUtils;
import orfanmine.util.TaxNode;
import orfanmine.util.TaxRank;

public class Mine {

	public static TreeSet<Integer> dbList = null;

	private Integer taxon;
	private DBType dbType;
	private TaxRank stopRank;
	private String uiID;
	private String email;
	private String databaseFilePath;
	private String queryFilePath;
	private String resultsFilePath;
	private String scriptFilePath;

	public static void main(String[] args) {
//		String[] argsNew = { "-type", "prot", "-taxon", "5331", "-level", "family", "-email", "e@e.com", "-uiID",
//				"1234567MNM" };// 579446, 5331
//		args = argsNew;

		if (args.length < 8) {
			System.out.println("[Mine] Error: Wrong syntax");
			printUsage();
			return;
		}

		String databaseFilePath = null;
		String queryFilePath = null;
		String resultsFilePath = null;
		DBType dbType = null;
		String scriptFilePath = null;
		Integer taxon = null;
		TaxRank stopRank = null;
		String uiID = null;
		String email = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-db":
				if ((i + 1) < args.length)
					databaseFilePath = args[++i];
				break;
			case "-query":
				if ((i + 1) < args.length)
					queryFilePath = args[++i];
				break;
			case "-type":
				if ((i + 1) < args.length)
					dbType = DBType.getDBTypeArg(args[++i]);
				break;
			case "-taxon":
				if ((i + 1) < args.length)
					taxon = Integer.valueOf(args[++i]);
				break;
			case "-level":
				if ((i + 1) < args.length)
					stopRank = TaxRank.identifyRank(args[++i]);
				break;
			case "-uiID":
				if ((i + 1) < args.length)
					uiID = args[++i];
				break;
			case "-results":
				if ((i + 1) < args.length)
					resultsFilePath = args[++i];
				break;
			case "-email":
				if ((i + 1) < args.length)
					email = args[++i];
				break;
			case "-script":
				if ((i + 1) < args.length)
					scriptFilePath = args[++i];
				break;
			}
		}
		if (databaseFilePath == null)
			databaseFilePath = ORFanMineUtils.getDatabaseOperational();
		if (queryFilePath == null)
			queryFilePath = ORFanMineUtils.getQueryOperational();
		if (resultsFilePath == null)
			resultsFilePath = ORFanMineUtils.getResultsOperational();
		if (scriptFilePath == null)
			scriptFilePath = ORFanMineUtils.getScriptOperational(dbType);
		if (taxon == null) {
			System.out.println("Error: Argument \"-taxon\". Value is missing...");
			printUsage();
			return;
		}
		if (dbType == null) {
			System.out.println("Error: Argument \"-type\". Value is missing...");
			printUsage();
			return;
		}
		if (uiID == null) {
			System.out.println("Error: Argument \"-uiID\". Value is missing...");
			printUsage();
			return;
		}
		if (email == null) {
			System.out.println("Error: Argument \"-email\". Value is missing...");
			printUsage();
			return;
		}
		if (stopRank == null)
			stopRank = TaxRank.ROOT;

		Mine mining = new Mine(taxon, dbType, stopRank, databaseFilePath, queryFilePath, resultsFilePath,
				scriptFilePath, uiID, email);

//		boolean generateScriptSuccess = mining.generateScript();
		boolean generateScriptSuccess = mining.generateScript3();

//		if (generateScriptSuccess) {
//			ProcessBuilder processBuilder = new ProcessBuilder();
//			processBuilder.command("./" + scriptFilePath);
//			try {
//
//				Process process = processBuilder.start();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}

	public Mine(Integer taxon, DBType dbType, TaxRank stopRank, String databaseFilePath, String queryFilePath,
			String resultsFilePath, String scriptFilePath, String uiID, String email) {
		this.taxon = taxon;
		this.dbType = dbType;
		this.stopRank = stopRank;
		this.databaseFilePath = databaseFilePath;
		this.queryFilePath = queryFilePath;
		this.resultsFilePath = resultsFilePath;
		this.scriptFilePath = scriptFilePath;
		this.uiID = uiID;
		this.email = email;
	}

	// 3.0
	public boolean generateScript3() {
		if (dbList == null)
			dbList = orfanmine.dbcuration.Partition.loadDBList(dbType);
		ArrayList<Integer> lineage = TaxNode.findSpeciesLineageByLevel(taxon, stopRank);
		if (lineage == null) {
			System.out.println("Could not generate the script: no known lineage ...");
			return false;
		}
		BufferedWriter bw = ORFanMineUtils.getWriter(scriptFilePath, false);
		try {
			bw.write("now=$(date)\necho $now\n");
			boolean dbListContainsTaxon = writeScriptCurrentTaxon(bw, lineage);
			Integer excludeTaxon = taxon;
			int i = 0;
			for (Integer taxID : lineage) {
				TaxNode currentNode = TaxNode.getTaxIdLineage().get(taxID);
				writeScriptCurrentLevel(bw, currentNode, excludeTaxon);
				if (!dbList.contains(taxID))
					writeScriptClassified(bw, currentNode, excludeTaxon);
				bw.write("echo Completed step " + ++i + " out of " + (lineage.size() + 1) + " total\n");
				bw.write("now=$(date)\necho $now\n");
				excludeTaxon = taxID;
			}
			writeScriptMiscellaneous(bw, dbListContainsTaxon, excludeTaxon);
			bw.write("echo Completed step " + ++i + " out of " + (lineage.size() + 1) + " total\n");
			bw.write("now=$(date)\necho $now\n");
			bw.write("java -cp code orfanmine.pipeline.Submitter -taxon " + taxon + " -type " + dbType.getName()
					+ " -uiID " + uiID + "\n");
			bw.write("mkdir storage/" + dbType.getName() + "/" + taxon + "\n");
			bw.write("mv workspace/query storage/" + dbType.getName() + "/" + taxon + "\n");
			bw.write("mv workspace/results* storage/" + dbType.getName() + "/" + taxon + "\n");
			bw.write("cp workspace/*" + uiID + ".csv storage/" + dbType.getName() + "/" + taxon + "\n");
			bw.write("cp " + scriptFilePath + " storage/" + dbType.getName() + "/" + taxon + "\n");
			bw.write("cd storage/" + dbType.getName() + "/" + "\n");
			bw.write("zip " + LocalDate.now().toString() + "_" + dbType.getName() + "_" + taxon + ".zip " + taxon
					+ "/*\n");
			bw.write("rm " + taxon + "/*\n");
			bw.write("rm -d " + taxon + "\n");
			bw.write("cd ../../\n");
			bw.write("mkdir storage/" + email + "\n");
			bw.write("mv workspace/*" + uiID + ".csv storage/" + email + "\n");
			bw.write("rm workspace/*\n");
			bw.write("now=$(date)\necho $now\n");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			ORFanMineUtils.closeWriter(bw);
		}
		return true;
	}

	private String compileBlastCmd(String database, String query, String results) {
		String blastCmd = ORFanMineUtils.getBlastCmd();
		blastCmd = blastCmd.replaceFirst("BLAST", dbType.blastCmd());
		blastCmd = blastCmd.replaceFirst("DATABASE", database);
		blastCmd = blastCmd.replaceFirst("QUERY", query);
		blastCmd = blastCmd.replaceFirst("EVALUE", "1e-3");
//		if (dbType == DBType.PROT)
//			blastCmd = blastCmd.replaceFirst("EVALUE", "1e-6");
//		else
//			blastCmd = blastCmd.replaceFirst("EVALUE", "1e-3");
		blastCmd = blastCmd.replaceFirst("RESULTS", results);
		return blastCmd;
	}

	private void writeScriptExitIfEmptyQuery(BufferedWriter bw) throws IOException {
		bw.write("if [ $? -eq 1 ]\nthen exit 0\nfi\n");
	}

	private void writeScriptClassified(BufferedWriter bw, TaxNode currentNode, Integer excludeTaxon)
			throws IOException {
		bw.write("java -cp code orfanmine.pipeline.Taxon -taxon " + currentNode.getId() + " -exclude " + excludeTaxon
				+ " -type " + dbType.getName() + " -db '" + ORFanMineUtils.getClassifiedDBPath(dbType) + "'\n");
		bw.write("cd workspace\n");
		bw.write("makeblastdb -in database -dbtype " + dbType.getName() + " -parse_seqids -taxid_map "
				+ ORFanMineUtils.getTaxIDClassifiedDBPath(dbType) + " -blastdb_version 5\n");
		String blastCmd = compileBlastCmd("database", "query", "results_" + currentNode.getId() + "_classified");
		bw.write(blastCmd + "\n");
		bw.write("cd ..\n");

		bw.write("java -cp code orfanmine.pipeline.SiftOrthologs -taxon " + taxon
				+ " -db workspace/query -ortho workspace/results_" + currentNode.getId() + "_classified\n");
		writeScriptExitIfEmptyQuery(bw);
	}

	private void writeScriptMiscellaneous(BufferedWriter bw, boolean dbListContainsTaxon, Integer stopTaxon)
			throws IOException {
		String targetDB = ORFanMineUtils.getUnclassifiedDBPath(dbType);
		boolean outOfWorkspace = true;
		if (!dbListContainsTaxon || (stopRank != TaxRank.ROOT)) {
			bw.write("java -cp code orfanmine.pipeline.Taxon -taxon " + stopTaxon + " -exclude " + taxon + " -type "
					+ dbType.getName() + " -db '" + ORFanMineUtils.getUnclassifiedDBPath(dbType) + "'\n");
			targetDB = "database";
			bw.write("cd workspace\n");
			outOfWorkspace = false;
			bw.write("makeblastdb -in database -dbtype " + dbType.getName() + " -parse_seqids -taxid_map "
					+ ORFanMineUtils.getTaxIDUnclassifiedDBPath(dbType) + " -blastdb_version 5\n");
		}
		if (outOfWorkspace)
			bw.write("cd workspace\n");
		String blastCmd = compileBlastCmd(targetDB, "query", "results_" + stopTaxon + "_unclassified");
		bw.write(blastCmd + "\n");
		bw.write("cd ..\n");
		bw.write("java -cp code orfanmine.pipeline.SiftOrthologs -taxon " + taxon
				+ " -db workspace/query -ortho workspace/results_" + stopTaxon + "_unclassified\n");
		writeScriptExitIfEmptyQuery(bw);
	}

	private boolean writeScriptCurrentTaxon(BufferedWriter bw, ArrayList<Integer> lineage) throws IOException {
		boolean dbListContainsTaxon = false;
		Integer targetDbTaxID = null;
		for (Integer level : lineage)
			if (dbList.contains(level)) {
				targetDbTaxID = level;
				dbListContainsTaxon = true;
				break;
			}
		if (dbListContainsTaxon) {
			bw.write("java -cp code orfanmine.pipeline.Taxon -taxon " + taxon + " -type " + dbType.getName() + " -db "
					+ ORFanMineUtils.getDBPartitionFilePath(targetDbTaxID, dbType) + "\n");
		} else {
			bw.write("java -cp code orfanmine.pipeline.Taxon -taxon " + taxon + " -type " + dbType.getName() + " -db '"
					+ ORFanMineUtils.getClassifiedDBPath(dbType) + "'\n");
		}
		return dbListContainsTaxon;
	}

	private void writeScriptCurrentLevel(BufferedWriter bw, TaxNode currentNode, Integer excludeTaxon)
			throws IOException {
		if (currentNode.getRank() == TaxRank.GENUS) {
			Integer taxID = currentNode.getId();
			if (dbList.contains(taxID)) {
				bw.write("java -cp code orfanmine.pipeline.Taxon -taxon " + taxID + " -exclude " + excludeTaxon
						+ " -type " + dbType.getName() + " -db " + ORFanMineUtils.getDBPartitionFilePath(taxID, dbType)
						+ "\n");
				bw.write("cd workspace\n");
				bw.write("makeblastdb -in database -dbtype " + dbType.getName() + " -parse_seqids -taxid_map "
						+ ORFanMineUtils.getTaxIDDBPartitionFilePath(taxID, dbType) + " -blastdb_version 5\n");
//				String blastCmd = compileBlastCmd(ORFanMineUtils.getDBPartitionFilePath(taxID, dbType), "query",
//						"results_" + taxID);
				String blastCmd = compileBlastCmd("database", "query", "results_" + taxID);
				bw.write(blastCmd + "\n");
				bw.write("cd ..\n");
				bw.write("java -cp code orfanmine.pipeline.SiftOrthologs -taxon " + taxon
						+ " -db workspace/query -ortho workspace/results_" + taxID + "\n");
				writeScriptExitIfEmptyQuery(bw);
			}
		} else

		{
			for (TaxNode node : currentNode.getChildren()) {
				Integer nodeTaxID = node.getId();
				if (!nodeTaxID.equals(excludeTaxon)) {
					if (dbList.contains(nodeTaxID)) {
						bw.write("cd db\n");
						String blastCmd = compileBlastCmd(ORFanMineUtils.getDBPartitionFileName(nodeTaxID, dbType),
								"../workspace/query", "../workspace/results_" + nodeTaxID);
						bw.write(blastCmd + "\n");
						bw.write("cd ..\n");
						bw.write("java -cp code orfanmine.pipeline.SiftOrthologs -taxon " + taxon
								+ " -db workspace/query -ortho workspace/results_" + nodeTaxID + "\n");
						writeScriptExitIfEmptyQuery(bw);
					} else {
						TaxRank partitionLevel = TaxNode.getTaxIdLineage().get(dbList.first()).getRank();
						if (partitionLevel != node.getRank()) {
							writeScriptCurrentLevel(bw, node, excludeTaxon);
						}
					}
				}
			} // for
		} // else
	}

	// v 2.0
//	public boolean generateScript() {
//		ArrayList<Integer> lineage = TaxNode.findSpeciesLineageByLevel(taxon, stopRank);
//		if (lineage == null) {
//			System.out.println("Could not generate the script: no known lineage ...");
//			return false;
//		}
//		BufferedWriter bw = ORFanMineUtils.getWriter(scriptFilePath, false);
//		try {
//			bw.write("java -cp code orfanmine.pipeline.Taxon -taxon " + taxon + " -type " + dbType.getName() + "\n");
//
//			Integer excludeTaxon = taxon;
//			int i = 0;
//			for (Integer taxID : lineage) {
//				bw.write("java -cp code orfanmine.pipeline.Taxon -taxon " + taxID + " -exclude " + excludeTaxon
//						+ " -type " + dbType.getName() + "\n");
//				bw.write("cd workspace\n");
//				bw.write("makeblastdb -in database -dbtype " + dbType.getName() + " -blastdb_version 5\n");
//				if (dbType == DBType.PROT)
//					bw.write(dbType.blastCmd()
//							+ " -db database -query query -outfmt \"6 qseqid pident sacc\" -max_target_seqs 5 -evalue 1e-3 -num_threads 48 -out results_"
//							+ taxID + "\n");
//				else
//					bw.write(dbType.blastCmd()
//							+ " -db database -query query -outfmt \"6 qseqid pident sacc\" -max_target_seqs 5 -evalue 1e-3 -num_threads 48 -out results_"
//							+ taxID + "\n");
//				bw.write("cd ..\n");
//				bw.write("java -cp code orfanmine.pipeline.SiftOrthologs -taxon " + taxon
//						+ " -db workspace/query -ortho workspace/results_" + taxID + "\n");
//				bw.write("echo Completed step " + ++i + " out of " + lineage.size() + " total\n");
//				excludeTaxon = taxID;
//			}
//			bw.write("java -cp code orfanmine.pipeline.Submitter -taxon " + taxon + " -type " + dbType.getName()
//					+ " -uiID " + uiID + "\n");
//			bw.write("mkdir storage/" + dbType.getName() + "/" + taxon + "\n");
//			bw.write("mv workspace/query storage/" + dbType.getName() + "/" + taxon + "\n");
//			bw.write("mv workspace/results* storage/" + dbType.getName() + "/" + taxon + "\n");
//			bw.write("cp 2*.csv storage/" + dbType.getName() + "/" + taxon + "\n");
//			bw.write("mkdir storage/" + email + "\n");
//			bw.write("cp 2*.csv storage/" + email + "\n");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		} finally {
//			ORFanMineUtils.closeWriter(bw);
//		}
//		return true;
//	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println(
				"\tjava orfanmine.pipeline.Mine -taxon taxonID -type molecule_type -uiID uniqueID -email userEmail [-level levelName] [-db databaseFilePath] [-query queryFilePath] [-results resultsFilePath] [-script scriptOutputFilePath]");
		System.out.println("\t\tmolecule_type: \"nucl\" || \"prot\"");
		System.out.println(
				"\t\tlevel: \"genus\" || \"family\" || \"order\" || \"class\" || \"phylum\" || \"kingdom\" || \"superkingdom\"");
	}
}
