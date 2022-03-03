package orfanmine.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import orfanmine.util.DBType;
import orfanmine.util.ORFanMineUtils;
import orfanmine.util.TaxRank;

//1 title	Species_Name(TaxID)
//2	description	Species_Name(TaxID)
//3	isPublic	TRUE
//4	status	DONE
//5	sourceType	ORFanBasePipeline
//6	parameters.cvTermAccession	
//7	parameters.cvTermDescription
//8	parameters.cvTermValue
//9	parameters.cvTermLabel
//10	submitter.title		Bot
//11	submitter.firstName	ORFan
//12	submitter.lastName	Base
//13	submitter.email		
//14	submitter.country
//15	submitter.isAcceptedTerms	TRUE
//16	submitter.termsAcceptedDate	DATE
//17	genes.accession				AccessionNumber
//18	genes.name					
//19	genes.description			NCBI Annotation
//20	genes.alias	
//21	genes.sequence				Link to NCBI
//22	genes.chromosome
//23	genes.startLocation
//24	genes.endLocation
//25	genes.geneProperties.length
//26	genes.geneProperties.gcContent
//27	genes.geneProperties.cpgIslands
//28	genes.geneProperties.isProteinCode
//29	genes.organism.scientificName
//30	genes.organism.ncbiTaxonomyId	TaxID
//31	genes.type	nucl || prot

public class Submitter {

	private static String submitterHeader = "title,description,isPublic,status,sourceType,parameters.cvTermAccession,parameters.cvTermDescription,parameters.cvTermValue,parameters.cvTermLabel,submitter.title,submitter.firstName,submitter.lastName,submitter.email,submitter.country,submitter.isAcceptedTerms,submitter.termsAcceptedDate,genes.accession,genes.name,genes.description,genes.alias,genes.sequence,genes.chromosome,genes.startLocation,genes.endLocation,genes.geneProperties.length,genes.geneProperties.gcContent,genes.geneProperties.cpgIslands,genes.geneProperties.isProteinCode,genes.organism.scientificName,genes.organism.ncbiTaxonomyId,genes.type";
	private String queryFilePath;
	private String csvFilePath;
	private DBType dbType;
	private String speciesName;
	private Integer taxID;
	private String uiID;
	private String stopRank;

	public static void main(String[] args) {
//		String[] argsNew = { "-taxon", "7227", "-type", "nucl", "-fasta",
//				"c:\\ORFanBase\\Results\\NUCL\\Drosophila melanogaster (7227)\\query" };
//		args = argsNew;

		if (args.length < 6) {
			System.out.println("[Submitter] Error: Wrong syntax");
			printUsage();
			return;
		}

		String fastaFilePath = null;
		String csvFilePath = null;
		DBType dbType = null;
		Integer taxID = null;
		String uiID = null;
		String stopRank = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-taxon":
				if ((i + 1) < args.length)
					taxID = Integer.valueOf(args[++i]);
				break;
			case "-type":
				if ((i + 1) < args.length)
					dbType = DBType.getDBTypeArg(args[++i]);
				break;
			case "-uiID":
				if ((i + 1) < args.length)
					uiID = args[++i];
				break;
			case "-level":
				if ((i + 1) < args.length)
					stopRank = args[++i];
				break;
			case "-fasta":
				if ((i + 1) < args.length)
					fastaFilePath = args[++i];
				break;
			case "-csv":
				if ((i + 1) < args.length)
					csvFilePath = args[++i];
				break;
			}
		}
		if (dbType == null) {
			System.out.println(
					"[Submitter] Error: Argument \"-type\". Mandatory value is missing:  `String, `nucl', `prot''");
			printUsage();
			return;
		}
		if (uiID == null) {
			System.out.println("[Submitter] Error: Argument \"-uiID\". Value is missing");
			printUsage();
			return;
		}
		if (taxID == null) {
			System.out.println("[Submitter] Missing the taxon ID...");
			printUsage();
			return;
		}
		if (stopRank == null)
			stopRank = TaxRank.ROOT.getName();
		if (fastaFilePath == null)
			fastaFilePath = ORFanMineUtils.getQueryOperational();
//		if (csvFilePath == null) {
//			csvFilePath = "";
//			int folderSeparatorIndex = fastaFilePath.lastIndexOf("\\");
//			if (folderSeparatorIndex > 0)
//				csvFilePath = fastaFilePath.substring(0, folderSeparatorIndex + 1);
//			csvFilePath += dbType.getName() + "_" + taxID + ".csv";
//		}

		Submitter submitter = new Submitter(fastaFilePath, csvFilePath, dbType, taxID, uiID, stopRank);
		submitter.generateCSV();
	}

	public Submitter(String queryFilePath, String csvFilePath, DBType dbType, Integer taxID, String uiID,
			String stopRank) {
		this.queryFilePath = queryFilePath;
		this.dbType = dbType;
		this.taxID = taxID;
		this.uiID = uiID;
		this.stopRank = stopRank;
		this.speciesName = ORFanMineUtils.getTaxDBName(taxID);
		if (csvFilePath == null)
			this.csvFilePath = generateCSVFileName(taxID, dbType, uiID, stopRank);
		else
			this.csvFilePath = csvFilePath;
	}

	public static String generateCSVFileName(Integer taxID, DBType dbType, String uiID, String stopRank) {
		String fileName = ORFanMineUtils.getWorkSpaceDir();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd-HH-mm");
		LocalDateTime now = LocalDateTime.now();
		fileName += dtf.format(now);
		fileName += "-" + ORFanMineUtils.getTaxDBName(taxID) + "-" + taxID + "-" + dbType + "-" + stopRank;
		fileName += "-" + uiID + ".csv";
		return fileName;
	}

	private void generateCSV() {
		BufferedReader br = ORFanMineUtils.openReader(queryFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(csvFilePath, false);

		long count = 0;
		try {
			bw.write(submitterHeader + "\n");
			String contentLine = br.readLine();
			while (contentLine != null) {
				if (contentLine.startsWith(">")) {
					count++;
					String[] strArray = contentLine.split(" ");
					String accessionNumber = strArray[0].substring(1);
					String speciesNameCurrent = speciesName;
					Integer internalTaxID = Integer.valueOf(strArray[1]);
					if (!internalTaxID.equals(taxID)) {
						System.out.println("Warning: internal taxonomic ID (" + internalTaxID
								+ ") does not match the parameter taxonomic ID (" + taxID + ") for accession number: "
								+ accessionNumber);
						speciesNameCurrent = ORFanMineUtils.getTaxDBName(internalTaxID);
					}
					String annotation = contentLine.substring(strArray[0].length() + strArray[1].length() + 2);
					String submitterString = assemblyLine(speciesNameCurrent, accessionNumber, internalTaxID,
							annotation);
					bw.write(submitterString + "\n");
				}
				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}

		System.out.println("[generateCSV] Nr of lines: " + count);
	}

	private String assemblyLine(String speciesNameCurrent, String accessionNumber, Integer internalTaxID,
			String annotation) {
		String[] submitterLine = new String[31];
		submitterLine[0] = speciesNameCurrent + " (" + internalTaxID + ")";
		submitterLine[1] = speciesNameCurrent + " (" + internalTaxID + ")";
		submitterLine[2] = "TRUE";
		submitterLine[3] = "DONE";
		submitterLine[9] = "Bot";
		submitterLine[10] = "ORFan";
		submitterLine[11] = "Base";
		submitterLine[12] = "bot@orfanbase.com";
		submitterLine[14] = "TRUE";
		submitterLine[15] = LocalDate.now().toString();
		submitterLine[29] = internalTaxID.toString();
		submitterLine[30] = dbType.getName();

		submitterLine[16] = accessionNumber;
		submitterLine[18] = "\"" + annotation + "\"";
		if (dbType == DBType.NUCL)
			submitterLine[20] = "https://www.ncbi.nlm.nih.gov/nuccore/" + accessionNumber;
		else
			submitterLine[20] = "https://www.ncbi.nlm.nih.gov/protein/" + accessionNumber;
		String submitterString = "";
		boolean isFirst = true;
		for (String str : submitterLine) {
			if (!isFirst)
				submitterString += ",";
			else
				isFirst = false;
			if (str != null)
				submitterString += str;
		}
		return submitterString;
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println(
				"\tjava orfanbase.pipeline.Submitter -type molecule_type -taxon taxonID -uiID uniqueID [-level levelName] [-fasta fastaResultFilePath] [-csv csvOutputFilePath]");
		System.out.println("\t\tmolecule_type: \"nucl\" || \"prot\"");
	}

}
