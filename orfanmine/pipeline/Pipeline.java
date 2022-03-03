package orfanmine.pipeline;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import orfanmine.util.DBType;
import orfanmine.util.ORFanMineUtils;
import orfanmine.util.TaxNode;

public class Pipeline {

	private Integer taxon;
	private DBType dbType;
	private String databaseFilePath;
	private String headersFilePath;
	private String queryFilePath;
	private String resultsFilePath;
	private String scriptFilePath;

	public static void main(String[] args) {
//		String[] argsNew = { "-db", "c:\\ORFanBase\\Test\\test", "-headers", "c:\\ORFanBase\\Test\\testHeaders",
//				"-query", "c:\\ORFanBase\\Test\\query", "-type", "prot", "-taxon", "9606"};
//		String[] argsNew = { "-type", "nucl", "-taxon", "6239" };
//		args = argsNew;

		if (args.length < 4) {
			System.out.println("[Taxon] Error: Wrong syntax");
			printUsage();
			return;
		}

		String databaseFilePath = null;
		String headersFilePath = null;
		String queryFilePath = null;
		String resultsFilePath = null;
		DBType dbType = null;
		String scriptFilePath = null;
		Integer taxon = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-db":
				if ((i + 1) < args.length)
					databaseFilePath = args[++i];
				break;
			case "-headers":
				if ((i + 1) < args.length)
					headersFilePath = args[++i];
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
			case "-results":
				if ((i + 1) < args.length)
					resultsFilePath = args[++i];
				break;
			case "-script":
				if ((i + 1) < args.length)
					scriptFilePath = args[++i];
				break;
			}
		}
		if (databaseFilePath == null)
			databaseFilePath = ORFanMineUtils.getDatabaseOperational();
		if (headersFilePath == null)
			headersFilePath = ORFanMineUtils.getHeadersOperational();
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

		Pipeline pipeline = new Pipeline(taxon, dbType, databaseFilePath, headersFilePath, queryFilePath,
				resultsFilePath, scriptFilePath);

		boolean generateScriptSuccess = pipeline.generateScript();

		if (generateScriptSuccess) {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("./" + scriptFilePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Pipeline(Integer taxon, DBType dbType, String databaseFilePath, String headersFilePath, String queryFilePath,
			String resultsFilePath, String scriptFilePath) {
		this.taxon = taxon;
		this.dbType = dbType;
		this.databaseFilePath = databaseFilePath;
		this.headersFilePath = headersFilePath;
		this.queryFilePath = queryFilePath;
		this.resultsFilePath = resultsFilePath;
		this.scriptFilePath = scriptFilePath;
	}

	// v 2.0
	public boolean generateScript() {
		ArrayList<Integer> lineage = TaxNode.findSpeciesLineageByLevel(taxon);
		if (lineage == null) {
			System.out.println("Could not generate the script: no known lineage ...");
			return false;
		}
		BufferedWriter bw = ORFanMineUtils.getWriter(scriptFilePath, false);
		try {
//			bw.write("@echo off\n");
			bw.write("java -cp code orfanbase.pipeline.Taxon -taxon " + taxon + " -type " + dbType.getName() + "\n");
//			bw.write("copy \"" + queryFilePath + "\" \"" + databaseFilePath + "\"\n");
//			bw.write("cd \"" + ORFanMineUtils.getWorkSpaceDir() + "\"\n");
//			bw.write("makeblastdb -in database -blastdb_version 5 -dbtype " + dbType.getName() + "\n");
//			bw.write("cd..\n");
//			if (dbType == DBType.PROT)
//				bw.write(dbType.blastCmd() + " -db \"" + databaseFilePath + "\" -query \"" + queryFilePath
//						+ "\" -outfmt \"6 qseqid pident sacc staxids\" -max_target_seqs 5 -evalue 1e-6 -out \""
//						+ resultsFilePath + "_" + taxon + "\" -num_threads 4\n");
//			else
//				bw.write(dbType.blastCmd() + " -db \"" + databaseFilePath + "\" -query \"" + queryFilePath
//						+ "\" -outfmt \"6 qseqid pident sacc\" -max_target_seqs 5 -evalue 1e-3 -out \""
//						+ resultsFilePath + "_" + taxon + "\" -num_threads 4\n");
//			bw.write("java -cp code orfanbase.dbcuration.QueryCuration " + resultsFilePath + "_" + taxon + "\n");

			Integer excludeTaxon = taxon;
			int i = 0;
			for (Integer taxID : lineage) {
				bw.write("java -cp code orfanbase.pipeline.Taxon -taxon " + taxID + " -exclude " + excludeTaxon
						+ " -type " + dbType.getName() + "\n");
//				bw.write("IF %ERRORLEVEL% == 0 (\n");
				bw.write("cd \"" + ORFanMineUtils.getWorkSpaceDir() + "\"\n");
//				bw.write("makeblastdb -in database -blastdb_version 5 -dbtype " + dbType.getName() + "\n");
				bw.write("makeblastdb -in database -dbtype " + dbType.getName() + "\n");
				bw.write("cd ..\n");
				if (dbType == DBType.PROT)
					bw.write(dbType.blastCmd()
							+ " -db database -query query -outfmt \"6 qseqid pident sacc\" -max_target_seqs 5 -evalue 1e-6 -out results_"
							+ taxID + "\n");
				else
					bw.write(dbType.blastCmd()
							+ " -db database -query query -outfmt \"6 qseqid pident sacc\" -max_target_seqs 5 -evalue 1e-3 -out results_"
							+ taxID + "\n");
				bw.write("java -cp code orfanbase.pipeline.SiftOrthologs -taxon " + taxon + " -db query -ortho results_"
						+ taxID + "\n");
//				bw.write(")\n");
				bw.write("echo Completed step " + ++i + " out of " + lineage.size() + " total\n");
				excludeTaxon = taxID;
			}
			bw.write(
					"java -cp code orfanbase.pipeline.Submitter -taxon " + taxon + " -type " + dbType.getName() + "\n");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			ORFanMineUtils.closeWriter(bw);
		}
		return true;
	}

	// v 1.0
//	public void generateScript() {
//		ArrayList<Integer> lineage = TaxNode.findSpeciesLineage(taxon);
//		if (lineage == null) {
//			System.out.println("Could not generate the script: no known lineage ...");
//			return;
//		}
//		BufferedWriter bw = ORFanMineUtils.getWriter(scriptFilePath, false);
//		try {
////			bw.write("@echo off\n");
//			bw.write("java -cp code orfanbase.pipeline.Taxon -taxon " + taxon + " -type " + dbType.getName() + "\n");
//			Integer excludeTaxon = taxon;
//			int i = 0;
//			for (Integer taxID : lineage) {
//				bw.write("java -cp code orfanbase.pipeline.Taxon -taxon " + taxID + " -exclude " + excludeTaxon
//						+ " -type " + dbType.getName() + "\n");
//				bw.write("IF %ERRORLEVEL% == 0 (\n");
//				bw.write("\tcd \"" + ORFanMineUtils.getWorkSpaceDir() + "\"\n");
//				bw.write("\tmakeblastdb -in database -parse_seqids -blastdb_version 5 -taxid_map headers -dbtype "
//						+ dbType.getName() + "\n");
////				bw.write("\tmakeblastdb -in \"" + databaseFilePath + "\" -parse_seqids -blastdb_version 5 -taxid_map \""
////						+ headersFilePath + "\" -dbtype " + dbType.getName() + "\n");
//				bw.write("\tcd..\n");
//				if (dbType == DBType.PROT)
//					bw.write("\t" + dbType.blastCmd() + " -db \"" + databaseFilePath + "\" -query \"" + queryFilePath
//							+ "\" -outfmt \"6 qseqid pident sacc staxids\" -max_target_seqs 1000 -evalue 1e-6 -out \""
//							+ resultsFilePath + "_" + taxID + "\" -num_threads 4\n");
//				else
//					bw.write("\t" + dbType.blastCmd() + " -db \"" + databaseFilePath + "\" -query \"" + queryFilePath
//							+ "\" -outfmt \"6 qseqid pident sacc staxids\" -max_target_seqs 1000 -evalue 1e-3 -out \""
//							+ resultsFilePath + "_" + taxID + "\" -num_threads 4\n");
//				bw.write("\tjava -cp code orfanbase.pipeline.SiftOrthologs -db \"" + queryFilePath + "\" -ortho \""
//						+ resultsFilePath + "_" + taxID + "\"\n");
//				bw.write(")\n");
//				bw.write("echo Completed step " + ++i + " out of " + lineage.size() + " total\n");
//				excludeTaxon = taxID;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			ORFanMineUtils.closeWriter(bw);
//		}
//	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println(
				"\tjava orfanbase.pipeline.Pipeline -taxon taxonID -type molecule_type [-db databaseFilePath] [-headers headersFilePath] [-query queryFilePath] [-results resultsFilePath] [-script scriptOutputFilePath]");
		System.out.println("\t\tmolecule_type: \"nucl\" || \"prot\"");
	}
}
