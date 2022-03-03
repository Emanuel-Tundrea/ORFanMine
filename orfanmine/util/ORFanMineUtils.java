package orfanmine.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class acts as a collection of utilities for the mining service.<br>
 * It is also an interface between the ORFanMine's configuration file
 * ("/home/emanuel/ORFanMine/util/properties") and the various micro-services
 * whose functionality depends on these values.<br>
 *
 * For more information about the algorithm, @see
 * <a href="https://www.orfanbase/mining">ORFanMine web page</a>
 */
public class ORFanMineUtils {

	private static String propertiesFilePath = "/home/emanuel/ORFanMine/util/properties";
//	private static String propertiesFilePath = "k:/ORFanMine/util/properties";
	private static final String dbTmpSuffix = "_tmp";

	private String baseDir, workSpaceDir, databaseDir, protDB, protDBHeaders, protDBConfiguration, nuclDB,
			nuclDBHeaders, nuclDBConfiguration, unclassifiedDB, classifiedDB, taxIDExtension, makeBlastDBScript;
	private long protMinNrSeqsPartitionDB, nuclMinNrSeqsPartitionDB;
	private String queryOperational, headersOperational, databaseOperational, resultsOperational, scriptOperational;
	private String ntAccession2TaxID;
	private String nrAccession2TaxID;
//	private TreeMap<String, Integer> ntAccession2TaxIDMap = null;
//	private TreeMap<String, Integer> nrAccession2TaxIDMap = null;
	private String taxDBNodesFilePath, taxDBNamesFilePath, taxDBLineageFilePath;
	private String Gene2RefSeq;
	private String errorLogFile;
	private String dbCurationKeyWords;
	private int identityPercentage = 60;
	private String blastCmd;
	private static ORFanMineUtils properties = null;

//	private ORFanMineUtils() {
//	}

	private ORFanMineUtils(String filePath) {
		propertiesFilePath = filePath;
	}

	public static void main(String[] args) {

//		String[] localArgs = { "/home/emanuel/ORFanBase/util/properties.txt" };
//		args = localArgs;

		if (args.length > 0)
			setPropertiesFilePath(args[0]);
		else
			loadProperties(propertiesFilePath);
	}

	private static void loadProperties(String filePath) {
		if (properties != null)
			return;
		properties = new ORFanMineUtils(filePath);
		BufferedReader source = openReader(propertiesFilePath);

		try {
			String contentLine = source.readLine();
			while (contentLine != null) {
				String[] strArray = contentLine.split("=");
				PropertyName propName = PropertyName.valueOf(strArray[0]);
				String propValue = strArray[1];
				switch (propName) {
				case BaseDir:
					properties.baseDir = propValue;
					break;
				case WorkSpaceDir:
					properties.workSpaceDir = propValue;
					break;
				case DatabaseDir:
					properties.databaseDir = propValue;
					break;
				case ProtDB:
					properties.protDB = propValue;
					break;
				case ProtDBHeaders:
					properties.protDBHeaders = propValue;
					break;
				case ProtDBConfiguration:
					properties.protDBConfiguration = propValue;
					break;
				case NuclDB:
					properties.nuclDB = propValue;
					break;
				case NuclDBHeaders:
					properties.nuclDBHeaders = propValue;
					break;
				case NuclDBConfiguration:
					properties.nuclDBConfiguration = propValue;
					break;
				case UnclassifiedDB:
					properties.unclassifiedDB = propValue;
					break;
				case ClassifiedDB:
					properties.classifiedDB = propValue;
					break;
				case TaxIDExtension:
					properties.taxIDExtension = propValue;
					break;
				case ProtMinNrSeqsPartitionDB:
					properties.protMinNrSeqsPartitionDB = Integer.valueOf(propValue);
					break;
				case NuclMinNrSeqsPartitionDB:
					properties.nuclMinNrSeqsPartitionDB = Integer.valueOf(propValue);
					break;
				case MakeBlastDBScript:
					properties.makeBlastDBScript = propValue;
					break;
				case QueryOperational:
					properties.queryOperational = propValue;
					break;
				case HeadersOperational:
					properties.headersOperational = propValue;
					break;
				case DatabaseOperational:
					properties.databaseOperational = propValue;
					break;
				case ResultsOperational:
					properties.resultsOperational = propValue;
					break;
				case ScriptOperational:
					properties.scriptOperational = propValue;
					break;
				case DBCurationKeyWords:
					properties.dbCurationKeyWords = propValue;
					break;
				case NTAccession2TaxID:
					properties.ntAccession2TaxID = propValue;
					break;
				case NRAccession2TaxID:
					properties.nrAccession2TaxID = propValue;
					break;
				case TaxDBNodes:
					properties.taxDBNodesFilePath = propValue;
					break;
				case TaxDBNames:
					properties.taxDBNamesFilePath = propValue;
					break;
				case TaxDBLineage:
					properties.taxDBLineageFilePath = propValue;
					break;
				case Gene2RefSeq:
					properties.Gene2RefSeq = propValue;
					break;
				case ErrorLogFile:
					properties.errorLogFile = propValue;
					break;
				case IdentityPercentage:
					int idPer = Integer.valueOf(propValue);
					if (idPer > 60 || idPer <= 100)
						properties.identityPercentage = idPer;
					break;
				case BLAST_CMD:
					properties.blastCmd = propValue;
					break;
				}
				contentLine = source.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		closeReader(source);
	}

	/**
	 * Getter: Returns the path to the ORFanMine configuration file.<br>
	 * By default: "/home/emanuel/ORFanMine/util/properties"<br>
	 * 
	 * @return the path to the ORFanMine configuration file
	 */
	public static String getPropertiesFilePath() {
		return propertiesFilePath;
	}

	/**
	 * Setter: Provides the path to the ORFanMine configuration file
	 * 
	 * @param filePath the path to the ORFanMine configuration file
	 */
	public static void setPropertiesFilePath(String filePath) {
		ORFanMineUtils.propertiesFilePath = filePath;
		loadProperties(filePath);
	}

	/**
	 * Getter: Returns the path to the ORFanMine base folder (the path on the
	 * current system that corresponds to the path where ORFanMine application is
	 * installed).<br>
	 * By default: "/home/emanuel/ORFanMine/"<br>
	 * 
	 * @return the path to the ORFanMine base folder
	 */
	public static String getBaseDir() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.baseDir;
	}

	/**
	 * Getter: Returns the path to the ORFanMine workspace folder (the path on the
	 * operating system that corresponds to the path where ORFanMine stores
	 * temporary files and results).<br>
	 * By default: "/home/emanuel/ORFanMine/workspace/"<br>
	 * 
	 * @return the path to the ORFanMine workspace folder
	 */
	public static String getWorkSpaceDir() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.workSpaceDir;
	}

	/**
	 * Getter: Returns the path to the ORFanMine database folder (the path on the
	 * operating system that corresponds to the path where ORFanMine stores the
	 * nucleotide/protein databases, their header files and the current
	 * configuration of the GENERA already available for BLASTing).<br>
	 * By default: "/home/emanuel/ORFanMine/db/"<br>
	 * 
	 * For more information about the algorithm, @see
	 * <a href="https://www.orfanbase/mining">ORFanMine web page</a>
	 * 
	 * @return the path to the ORFanMine database folder
	 */
	public static String getDatabaseDir() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir;
	}

	/**
	 * Getter: Returns the relative path to the ORFanMine protein database (relative
	 * to the ORFanMine base folder).<br>
	 * By default: "db/prot"<br>
	 * 
	 * @return the relative path to the ORFanMine protein database
	 */
	public static String getProtDB() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + properties.protDB;
	}

	/**
	 * Getter: Returns the relative path to the ORFanMine protein database headers
	 * (relative to the ORFanMine base folder).<br>
	 * By default: "db/prot.h"<br>
	 * 
	 * @return the relative path to the ORFanMine protein database headers
	 */
	public static String getProtDBHeaders() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + properties.protDBHeaders;
	}

	/**
	 * Getter: Returns the relative path to the ORFanMine protein database
	 * configuration file (relative to the ORFanMine base folder).<br>
	 * By default: "db/prot.config"<br>
	 * 
	 * For more information about the algorithm, @see
	 * <a href="https://www.orfanbase/mining">ORFanMine web page</a>
	 * 
	 * @return the relative path to the ORFanMine protein database configuration
	 *         file
	 */
	public static String getProtDBConfiguration() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + properties.protDBConfiguration;
	}

	/**
	 * Getter: Returns the relative path to the ORFanMine nucleotide database
	 * (relative to the ORFanMine base folder).<br>
	 * By default: "db/nucl"<br>
	 * 
	 * @return the relative path to the ORFanMine nucleotide database
	 */
	public static String getNuclDB() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + properties.nuclDB;
	}

	/**
	 * Getter: Returns the relative path to the ORFanMine nucleotide database
	 * headers (relative to the ORFanMine base folder).<br>
	 * By default: "db/nucl.h"<br>
	 * 
	 * @return the relative path to the ORFanMine nucleotide database headers
	 */
	public static String getNuclDBHeaders() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + properties.nuclDBHeaders;
	}

	/**
	 * Getter: Returns the relative path to the ORFanMine nucleotide database
	 * configuration file (relative to the ORFanMine base folder).<br>
	 * By default: "db/nucl.config"<br>
	 * 
	 * For more information about the algorithm, @see
	 * <a href="https://www.orfanbase/mining">ORFanMine web page</a>
	 * 
	 * @return the relative path to the ORFanMine nucleotide database configuration
	 *         file
	 */
	public static String getNuclDBConfiguration() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + properties.nuclDBConfiguration;
	}

	/**
	 * Getter: Returns the default file path of the database storing the collection
	 * of sequences whose lineage misses a GENUS rank.<br>
	 * By default: "/home/emanuel/ORFanMine/db/unclassified"<br>
	 * 
	 * @return the default file path of the database storing the collection of
	 *         sequences without a GENUS taxonomy ID in its lineage
	 */
	public static String getUnclassifiedDBPath(DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + getUnclassifiedDBName(type);
	}

	public static String getTaxIDUnclassifiedDBPath(DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		String path = getUnclassifiedDBPath(type) + "." + getTaxIDExtension();
		return path;
	}

	public static String getTaxIDUnclassifiedDBName(DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		String nameStr = getUnclassifiedDBName(type) + "." + getTaxIDExtension();
		return nameStr;
	}

	/**
	 * Getter: Returns the default name of the database storing the collection of
	 * sequences whose lineage misses a GENUS rank.<br>
	 * By default: "unclassified"<br>
	 * 
	 * @return the default name of the database storing the collection of sequences
	 *         without a GENUS taxonomy ID in its lineage
	 */
	public static String getUnclassifiedDBName(DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return type.getName() + "_" + properties.unclassifiedDB;
	}

	/**
	 * Getter: Returns the default file path of the database storing the collection
	 * of sequences whose lineage includes a GENUS rank taxonomy ID, but it is less
	 * represented in the NCBI non-redundant database (less than the value of the
	 * property provided by {@link PropertyName#MinNrSeqsPartitionDB}.<br>
	 * By default: "/home/emanuel/ORFanMine/db/classified"<br>
	 * 
	 * @return the default file path of the database storing the collection of
	 *         sequences whose lineage includes a GENUS rank taxonomy ID, but it is
	 *         less represented in the NCBI non-redundant database
	 */
	public static String getClassifiedDBPath(DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + getClassifiedDBName(type);
	}

	public static String getTaxIDClassifiedDBPath(DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		String path = getClassifiedDBPath(type) + "." + getTaxIDExtension();
		return path;
	}

	public static String getTaxIDExtension() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.taxIDExtension;
	}

	public static String getClassifiedDBName(DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return type.getName() + "_" + properties.classifiedDB;
	}

	public static long getProtMinNrSeqsPartitionDB() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.protMinNrSeqsPartitionDB;
	}

	public static long getNuclMinNrSeqsPartitionDB() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.nuclMinNrSeqsPartitionDB;
	}

	public static String getMakeBlastDBScript() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + properties.makeBlastDBScript;
	}

	public static String getDBPartitionFilePath(Integer rankID, DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.databaseDir + getDBPartitionFileName(rankID, type);
	}

	public static String getTaxIDDBPartitionFilePath(Integer rankID, DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		String taxIDFilePath = getDBPartitionFilePath(rankID, type) + "." + getTaxIDExtension();
		return taxIDFilePath;
	}

	public static String getTaxIDDBPartitionFileName(Integer rankID, DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		String taxIDFilePath = getDBPartitionFileName(rankID, type) + "." + getTaxIDExtension();
		return taxIDFilePath;
	}

	public static String getDBPartitionFileName(Integer rankID, DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return type.getName() + "_" + rankID;
	}

	public static String getQueryOperational() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.workSpaceDir + properties.queryOperational;
	}

	public static String getHeadersOperational() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.workSpaceDir + properties.headersOperational;
	}

	public static String getDatabaseOperational() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.workSpaceDir + properties.databaseOperational;
	}

	public static String getResultsOperational() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.workSpaceDir + properties.resultsOperational;
	}

	public static String getScriptOperational(DBType type) {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.baseDir + properties.scriptOperational + "_" + type.getName() + ".sh";
	}

	public static String getNtAccession2TaxID() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.ntAccession2TaxID;
	}

	public static String getNrAccession2TaxID() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.nrAccession2TaxID;
	}

	public static String getTaxDBNodesFilePath() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.taxDBNodesFilePath;
	}

	public static String getTaxDBNamesFilePath() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.taxDBNamesFilePath;
	}

	public static String getTaxDBName(Integer taxID) {
		BufferedReader br = ORFanMineUtils.openReader(getTaxDBNamesFilePath());
		String scientific_name = null;
		boolean nameFound = true;
		String contentLine = null;
		try {
			contentLine = br.readLine();
			while (contentLine != null) {
//				[0] - tax_id		-- the id of node associated with this name
//				[1] - name_txt		-- name itself
				String[] strArray = contentLine.split("\\t");

				Integer tax_id = Integer.valueOf(strArray[0]);
				scientific_name = strArray[1];
				if (tax_id.equals(taxID)) {
					nameFound = true;
					break;
				}
				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			System.out.println("Could not decode line: " + contentLine + "\n");
			ioe.printStackTrace();
			return null;
		} finally {
			ORFanMineUtils.closeReader(br);
		}
		if (nameFound)
			return scientific_name;
		return null;
	}

	public static String getTaxDBLineageFilePath() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.taxDBLineageFilePath;
	}

	public static String getErrorLogFile() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.errorLogFile;
	}

	public static int getIdentityPercentage() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.identityPercentage;
	}

	public static String getBlastCmd() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.blastCmd;
	}

	// tax id lineage
	// [0] = tax_id
	// [1] = separator
	// [2] = lineage with " " (space) as separator
	// [3] = separator final
//	private void loadNTAccession2TaxIDMap(int million) {
//		ntAccession2TaxIDMap = new TreeMap<String, Integer>();
//
//		BufferedReader br = openReader(ntAccession2TaxID);
//
//		long count = 0;
//		try {
//			String contentLine = br.readLine();
//			while (contentLine != null) {
//				count++;
//				if (((count / 1000000) >= million) && ((count / 1000000) < (million + 16))) {
//					String[] strArray = contentLine.split(" ");
//					String accessionNumber = strArray[0];
//					if (accessionNumber.indexOf('.') > 0)
//						accessionNumber = accessionNumber.substring(0, accessionNumber.indexOf('.'));
//					Integer id = Integer.valueOf(strArray[1]);
//					ntAccession2TaxIDMap.put(accessionNumber, id);
//				}
//				if ((count / 1000000) >= (million + 16))
//					break;
//				contentLine = br.readLine();
//			}
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		} finally {
//			closeReader(br);
//		}
//		System.out.print("Total NT Accession Nr: " + count + "...");
//	}
//
//	public static TreeMap<String, Integer> getNtAccession2TaxIDMap(int million) {
//		if (properties == null)
//			loadProperties(propertiesFilePath);
//		properties.loadNTAccession2TaxIDMap(million);
//		return properties.ntAccession2TaxIDMap;
//	}

	public static String getGene2RefSeq() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.Gene2RefSeq;
	}

	public static String[] getDbCurationKeyWords() {
		if (properties == null)
			loadProperties(propertiesFilePath);
		return properties.dbCurationKeyWords.split(",");
	}

	public static TreeSet<Integer> exportTaxIDSet(Integer taxID) {
		TreeMap<Integer, TaxNode> taxIdLineage = TaxNode.getTaxIdLineage();
		TreeSet<Integer> taxIDSet = new TreeSet<Integer>();
		taxIDSet.add(taxID);

		if (taxIdLineage.containsKey(taxID)) {
			TaxNode node = taxIdLineage.get(taxID);
			TreeSet<TaxNode> children = node.getChildren();
			for (TaxNode child : children) {
				Integer childTaxID = child.getId();
				taxIDSet.add(childTaxID);
				taxIDSet.addAll(exportTaxIDSet(childTaxID));
			}
		}
		return taxIDSet;
	}

	public static TreeSet<Integer> exportTaxIDSetExclude(Integer taxID, Integer taxExcludeID) {
		TreeMap<Integer, TaxNode> taxIdLineage = TaxNode.getTaxIdLineage();
		TreeSet<Integer> taxIDSet = new TreeSet<Integer>();
		if (taxID.equals(taxExcludeID))
			return taxIDSet;

		taxIDSet.add(taxID);

		if (taxIdLineage.containsKey(taxID)) {
			TaxNode node = taxIdLineage.get(taxID);
			TreeSet<TaxNode> children = node.getChildren();
			for (TaxNode child : children) {
				Integer childTaxID = child.getId();
				taxIDSet.addAll(exportTaxIDSetExclude(childTaxID, taxExcludeID));
			}
		}
		return taxIDSet;
	}

	public static String getTmpFilePath(String filePath) {
		return (filePath + dbTmpSuffix);
	}

	public static boolean replaceFile(String originalFilePath, String tempFilePath) {
		File deleteFile = new File(originalFilePath);
		deleteFile.delete();
		File renameFile = new File(tempFilePath);
		return renameFile.renameTo(deleteFile);
	}

	public static BufferedReader openReader(String sourceDB) {
		BufferedReader source = null;
		try {
			source = new BufferedReader(new FileReader(sourceDB));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return source;
	}

	public static boolean closeReader(BufferedReader source) {
		try {
			if (source != null)
				source.close();
		} catch (IOException ioe) {
			System.out.println("Error in closing the BufferedReader: " + ioe);
			return false;
		}
		return true;
	}

	public static BufferedWriter getWriter(String targetFileName, boolean append) {
		BufferedWriter bw = null;
		try {
			File targetFile = new File(targetFileName);
			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
			FileWriter fw = new FileWriter(targetFile, append);
			bw = new BufferedWriter(fw);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return bw;
	}

	public static boolean closeWriter(BufferedWriter bw) {
		try {
			if (bw != null)
				bw.close();
		} catch (Exception ex) {
			System.out.println("Error in closing the BufferedWriter: " + ex);
			return false;
		}
		return true;
	}
}
