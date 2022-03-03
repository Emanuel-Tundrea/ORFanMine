package orfanmine.dbcuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;

import orfanmine.util.DBType;
import orfanmine.util.ORFanMineUtils;

public class DBCuration {

	private String dbFilePath = null;
	private DBType dbType = null;

	public static void main(String[] args) {

		// Timer
		long startTime = System.nanoTime();

//		String[] argsLocal = { "-file", "e:\\NT\\nt", "-type", "nucl" };
//		String[] argsLocal = { "-file", "e:\\ORFanBase\\db\\nucl", "-type", "nucl" };
//		args = argsLocal;

		if (args.length < 4) {
			System.out.println("[DBCuration] Error: Wrong syntax");
			printUsage();
			return;
		}

		DBType dbTypeArg = null;
		String dbFilePathArg = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-file":
				if ((i + 1) < args.length)
					dbFilePathArg = args[++i];
				break;
			case "-type":
				if ((i + 1) < args.length)
					dbTypeArg = DBType.getDBTypeArg(args[++i]);
				break;
			}
		}
		if (dbFilePathArg == null) {
			System.out.println("[DBCuration] Error: Missing argument \"-file\" or its mandatory value");
			printUsage();
			return;
		}
		if (dbTypeArg == null) {
			System.out.println(
					"[DBCuration] Error: Argument \"-type\". Mandatory value is missing:  `String, `nucl', `prot''");
			printUsage();
			return;
		}

		DBCuration dbCuration = new DBCuration(dbFilePathArg, dbTypeArg);

//		Nr of sequences: 458431797
//		Execution time: 11 min 8 sec
//		System.out.println("Step 1 out of 7: Generating full-headers table ..."); // Total count: 458,431,797
//		dbCuration.generateFullHeadersDB(); // [step 01]
		long endTime = System.nanoTime();
//		System.out.println("Execution time: " + ((endTime - startTime) / 1000000000 / 60) + " min "
//				+ ((endTime - startTime) / 1000000000 % 60) + " sec");

//		TOTAL lines count: 458431797
//		Removable clone = 18954
//		Removable exon = 741261
//		Removable synthetic = 269392
//		Removable predicted = 4680536
//		Removable partial = 28299820
//		Removable hypothetical = 108862056
//		Removable putative = 3774319
//		Removable environmental = 22592
//		TOTAL CountRemovables: 139281764
//		Remaining Sequences: 319150033
//		Execution time: 5 min 54 sec
//		Execution time: 5 min 54 sec
		System.out.println("Step 2 out of 7: Removing non-existing sequences from headers ...");
		dbCuration.removeNonExistentSequencesFromHeadersDB(); // [step 02]
		endTime = System.nanoTime();
		System.out.println("Execution time: " + ((endTime - startTime) / 1000000000 / 60) + " min "
				+ ((endTime - startTime) / 1000000000 % 60) + " sec");

//		 TOTAL Removable Count: 125034
//		 Remaining Sequences: 319024999
		System.out.println("Step 3 out of 7: Removing sequences missing accession-number version from headers ...");
		dbCuration.removeSequencesWithoutAccessionNumberVersionFromHeadersDB(); // [step 03]
		endTime = System.nanoTime();
		System.out.println("Execution time: " + ((endTime - startTime) / 1000000000 / 60) + " min "
				+ ((endTime - startTime) / 1000000000 % 60) + " sec");
//
//		Nr of sequences: 319024999
//		Execution time: 1 min 15 sec
		System.out.println("Step 4 out of 7: Generating accession-number list from headers ...");
		dbCuration.generateAccessionNumberListFromHeadersDB(); // [step 04]
		endTime = System.nanoTime();
		System.out.println("Execution time: " + ((endTime - startTime) / 1000000000 / 60) + " min "
				+ ((endTime - startTime) / 1000000000 % 60) + " sec");
//
//		// !!! Intermediate STEP: remove orthologs provided by OrthodDB and NCBI
//

//		Total annotations: 318997802
//		Execution time: 107 min 27 sec
		System.out.println("Step 5 out of 7: Annotating headers with TaxID ...");
		dbCuration.annotateHeadersWithTaxID(); // [step 05]
		endTime = System.nanoTime();
		System.out.println("Execution time: " + ((endTime - startTime) / 1000000000 / 60) + " min "
				+ ((endTime - startTime) / 1000000000 % 60) + " sec");
//
//		Count with    TaxID (classified):   318997802
//		Count without TaxID (unclassified): 27197
//		Execution time: 1 min 12 sec
		System.out.println("Step 6 out of 7: Removing headers without TaxID ...");
		dbCuration.removeHeadersWithoutTaxID(); // [step 06]
		endTime = System.nanoTime();
		System.out.println("Execution time: " + ((endTime - startTime) / 1000000000 / 60) + " min "
				+ ((endTime - startTime) / 1000000000 % 60) + " sec");
//

//		count: 458431796
//		Execution time: 22 min 49 sec
		System.out.println("Step 7 out of 7: Annotating database headers with TaxID ...");
		dbCuration.annotateDBHeadersWithTaxID(); // [step 07]

//		dbCuration.removeOverSizedSequences("e:\\ORFanBase\\db\\nucl_sizes", 30000);
//		dbCuration.removeUnderSizedSequences("e:\\ORFanBase\\db\\nucl_sizes", 30000);

		endTime = System.nanoTime();
		System.out.println("Execution time: " + ((endTime - startTime) / 1000000000 / 60) + " min "
				+ ((endTime - startTime) / 1000000000 % 60) + " sec");
	}

	public DBCuration(String dbFilePath, DBType dbType) {
		this.dbFilePath = dbFilePath;
		this.dbType = dbType;
	}

	public long removeUnderSizedSequences(String sizesFilePath, long limit) {
		TreeSet<String> seqList = new TreeSet<String>();
		BufferedReader br = ORFanMineUtils.openReader(sizesFilePath);
		long count = 0;
		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
				if (count % 1000000 == 0)
					System.out.println("MILLION: " + count / 1000000);
				String[] strArray = contentLine.split(" ");
				Integer size = Integer.valueOf(strArray[2]);// size
				if (size <= limit) {
					String accessionNumber = strArray[0];
					seqList.add(accessionNumber);
				}
				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
		}
		System.out.println("TOTAL over size sequence count: " + seqList.size());
		removeSequenceList(seqList);
		return seqList.size();
	}

	public long removeOverSizedSequences(String sizesFilePath, long limit) {
		TreeSet<String> seqList = new TreeSet<String>();
		BufferedReader br = ORFanMineUtils.openReader(sizesFilePath);
		long count = 0;
		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
				if (count % 1000000 == 0)
					System.out.println("MILLION: " + count / 1000000);
				String[] strArray = contentLine.split(" ");
				Integer size = Integer.valueOf(strArray[2]);// size
				if (size > limit) {
					String accessionNumber = strArray[0];
					seqList.add(accessionNumber);
				}
				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
		}
		System.out.println("TOTAL over size sequence count: " + seqList.size());
		removeSequenceList(seqList);
		return seqList.size();
	}

	public long removeSequenceList(TreeSet<String> seqList) {
		long count = 0;
		long countRemoved = 0;

		BufferedReader br = ORFanMineUtils.openReader(dbFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(dbFilePath + ".database", false);
//		String headersFilePath = null;
//		if (dbType == DBType.NUCL)
//			headersFilePath = ORFanMineUtils.getNuclDBHeaders();
//		else
//			headersFilePath = ORFanMineUtils.getProtDBHeaders();
		BufferedWriter bwH = ORFanMineUtils.getWriter(dbFilePath + ".headers", false);

		try {
			boolean validSeq = false;
			String contentLine = br.readLine();
			while (contentLine != null) {
				if (contentLine.startsWith(">")) {
					count++;
					if (count % 1000000 == 0)
						System.out.println("MILLION: " + count / 1000000);

					String[] strArray = contentLine.split(" ");

					int indexDot = strArray[0].indexOf("."); // accession number
					String accessionNumber = contentLine.substring(1, indexDot);
//				String accessionNumber = strArray[0];
					if (seqList.contains(accessionNumber)) {
						validSeq = false;
						countRemoved++;
					} else {
						validSeq = true;
						bwH.write(accessionNumber + " " + strArray[1] + "\n");
					}
				}
				if (validSeq)
					bw.write(contentLine + "\n");
				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
			ORFanMineUtils.closeWriter(bwH);
		}
		System.out.println("TOTAL sequence count: " + count);
		System.out.println("TOTAL sequence removed: " + countRemoved);
		System.out.println("TOTAL sequence remaining: " + (count - countRemoved));

		return countRemoved;
	}

	private long annotateDBHeadersWithTaxID() {
		BufferedReader brDB = ORFanMineUtils.openReader(dbFilePath);
		String headersFilePath = null;
		if (dbType == DBType.NUCL)
			headersFilePath = ORFanMineUtils.getNuclDBHeaders();
		else
			headersFilePath = ORFanMineUtils.getProtDBHeaders();
		BufferedReader brHeaders = ORFanMineUtils.openReader(headersFilePath);
		String databaseFilePath = null;
		if (dbType == DBType.NUCL)
			databaseFilePath = ORFanMineUtils.getNuclDB();
		else
			databaseFilePath = ORFanMineUtils.getProtDB();
		BufferedWriter bwDB = ORFanMineUtils.getWriter(databaseFilePath, false);

		long count = 0;

		boolean validLine = true;
		String readLineDB = null;
		String readLineHeaders = null;
		try {
			readLineDB = brDB.readLine();
			readLineHeaders = brHeaders.readLine();
			while ((readLineDB != null) && (readLineHeaders != null)) {
				if (readLineDB.startsWith(">")) {
					count++;
					if (count % 1000000 == 0)
						System.out.println("MILLION [annotateDBHeadersWithTaxID]: " + count / 1000000);

					String[] strArray = readLineHeaders.split(" ");
					String accessionNumber = strArray[0];
					if (readLineDB.indexOf(accessionNumber) > 0) {
						String accessionNumberWithVersion = readLineDB.substring(1, readLineDB.indexOf(" "));
						readLineHeaders = readLineHeaders.replaceFirst(accessionNumber, accessionNumberWithVersion);
						readLineDB = readLineDB.replaceFirst(accessionNumberWithVersion, readLineHeaders);
						validLine = true;
						readLineHeaders = brHeaders.readLine();
					} else
						validLine = false;
				}
				if (validLine)
					bwDB.write(readLineDB + "\n");
				readLineDB = brDB.readLine();
			}
			// write the Fasta code for the last sequence
			while (readLineDB != null) {
				if (readLineDB.startsWith(">"))
					break;// no more new sequences because headers table is completely parsed
				bwDB.write(readLineDB + "\n");
				readLineDB = brDB.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(brDB);
			ORFanMineUtils.closeReader(brHeaders);
			ORFanMineUtils.closeWriter(bwDB);
		}
		System.out.println("[annotateDBHeadersWithTaxID] count: " + count);

		return count;
	}

	private long removeHeadersWithoutTaxID() {
		String headersFilePath = null;
		if (dbType == DBType.NUCL)
			headersFilePath = ORFanMineUtils.getNuclDBHeaders();
		else
			headersFilePath = ORFanMineUtils.getProtDBHeaders();
		BufferedReader br = ORFanMineUtils.openReader(headersFilePath);
		BufferedWriter bwWithTaxID = ORFanMineUtils.getWriter(ORFanMineUtils.getTmpFilePath(headersFilePath), false);

		long countWithTaxID = 0;
		long countNoTaxID = 0;

		String readLine = null;
		try {
			readLine = br.readLine();
			while (readLine != null) {
				String[] strArray = readLine.split(" ");
				if (strArray.length > 1) {
					bwWithTaxID.write(readLine + "\n");
					countWithTaxID++;
				} else {
					countNoTaxID++;
				}
				readLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bwWithTaxID);
		}
		System.out.println("Count with    TaxID (classified):   " + countWithTaxID);
		System.out.println("Count without TaxID (unclassified): " + countNoTaxID);

		ORFanMineUtils.replaceFile(headersFilePath, ORFanMineUtils.getTmpFilePath(headersFilePath));

		return countWithTaxID;
	}

	private void annotateHeadersWithTaxID() {
		BufferedReader br;
		if (dbType == DBType.NUCL)
			br = ORFanMineUtils.openReader(ORFanMineUtils.getNtAccession2TaxID());
		else
			br = ORFanMineUtils.openReader(ORFanMineUtils.getNrAccession2TaxID());

		long count = 0;
		long annotations = 0;

		TreeMap<String, Integer> accNrTaxIDList = new TreeMap<String, Integer>();

		String accNrTaxIDLine = null;
		try {
			accNrTaxIDLine = br.readLine();
			while (accNrTaxIDLine != null) {
				count++;
				if (count % 10000000 == 0)
					System.out.println("[annotateHeadersWithTaxID] 10xM: " + count / 10000000);

				String[] strArray = accNrTaxIDLine.split(" ");

				String accessionNumber = strArray[0];
				Integer taxID = Integer.valueOf(strArray[1]);
				accNrTaxIDList.put(accessionNumber, taxID);

				if (count % 200000000 == 0) {
					int size = 294;// ntaccession2taxid =
					if (dbType == DBType.PROT)
						size = 415; // nraccession2taxid = 4,151,303,757
					System.out.println("Loaded Accession2TaxID: " + count / 10000000 + " out of " + size);
					annotations += annotateHeadersWithTaxID(accNrTaxIDList);
					accNrTaxIDList.clear();
				}

				accNrTaxIDLine = br.readLine();
			}
//			System.out.println("Loaded Accession2TaxID: " + count / 1000000 + "out of 969");
			annotations += annotateHeadersWithTaxID(accNrTaxIDList);
			accNrTaxIDList.clear();
		} catch (IOException ioe) {
			System.out.println("Could not decode line: " + accNrTaxIDLine + "\n");
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
		}
		System.out.println("[annotateHeadersWithTaxID] Total annotations: " + annotations);
	}

	// one iteration
	private long annotateHeadersWithTaxID(TreeMap<String, Integer> accNrTaxIDList) {
		String headersFilePath = null;
		if (dbType == DBType.NUCL)
			headersFilePath = ORFanMineUtils.getNuclDBHeaders();
		else
			headersFilePath = ORFanMineUtils.getProtDBHeaders();
		BufferedReader br = ORFanMineUtils.openReader(headersFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(ORFanMineUtils.getTmpFilePath(headersFilePath), false);

		long count = 0;
		long annotations = 0;

		long dbSize = 319024999; // prot @ 13-Feb-2022
		if (dbType == DBType.NUCL)
			dbSize = 12513039;// nucl
		String accessionNumber = null;
		try {
			accessionNumber = br.readLine();
			while (accessionNumber != null) {
				count++;
				if (count % 10000000 == 0) {
					long percentage = ((count * 100) / dbSize);
					System.out.print(">" + percentage + "%");
				}
				if (accNrTaxIDList.containsKey(accessionNumber)) {
					accessionNumber += " " + accNrTaxIDList.get(accessionNumber);
					annotations++;
				}
				bw.write(accessionNumber + "\n");

				accessionNumber = br.readLine();
			}
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);

		} catch (IOException ioe) {
			System.out.println("Could not decode line: " + accessionNumber + "\n");
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		System.out.println(">DONE!");
		System.out.println("Annotations: " + annotations);

		ORFanMineUtils.replaceFile(headersFilePath, ORFanMineUtils.getTmpFilePath(headersFilePath));

		return annotations;
	}

	protected void generateAccessionNumberListFromHeadersDB() {
		String headersFilePath = null;
		if (dbType == DBType.NUCL)
			headersFilePath = ORFanMineUtils.getNuclDBHeaders();
		else
			headersFilePath = ORFanMineUtils.getProtDBHeaders();
		BufferedReader br = ORFanMineUtils.openReader(headersFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(ORFanMineUtils.getTmpFilePath(headersFilePath), false);
		long count = 0;
		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
//				if (count % 1000000 == 0)
//					System.out.println("MILLION [generateAccessionNumberListFromHeadersDB]: " + count / 1000000);
				int beginIndex = 1; // skip the '>' sign
				int endIndex = contentLine.indexOf('.');
				String accessionNumber = contentLine.substring(beginIndex, endIndex);
				bw.write(accessionNumber + "\n");

				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		ORFanMineUtils.replaceFile(headersFilePath, ORFanMineUtils.getTmpFilePath(headersFilePath));

		System.out.println("[generateAccessionNumberList] Nr of sequences: " + count);
	}

	protected long removeSequencesWithoutAccessionNumberVersionFromHeadersDB() {

		// Total number of entries found in the DB
		long count = 0;

		// Total number of entries removed
		long countRemoved = 0;

		String headersFilePath = null;
		if (dbType == DBType.NUCL)
			headersFilePath = ORFanMineUtils.getNuclDBHeaders();
		else
			headersFilePath = ORFanMineUtils.getProtDBHeaders();
		BufferedReader br = ORFanMineUtils.openReader(headersFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(ORFanMineUtils.getTmpFilePath(headersFilePath), false);
		BufferedWriter bwRemoved = ORFanMineUtils.getWriter("removed", false);

		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
				if (count % 10000000 == 0)
					System.out.println(
							"[removeSequencesWithoutAccessionNumberVersionFromHeadersDB] 10xM: " + count / 10000000);
				String accessionNumber = contentLine.substring(1, contentLine.indexOf(" "));
				if (accessionNumber.indexOf(".") < 0) {
					countRemoved++;
					bwRemoved.write(contentLine);
				} else
					bw.write(contentLine + "\n");
				contentLine = br.readLine();
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
			ORFanMineUtils.closeWriter(bwRemoved);
		}

		ORFanMineUtils.replaceFile(headersFilePath, ORFanMineUtils.getTmpFilePath(headersFilePath));

		System.out.println("[removeSequencesWithoutAccessionNumberVersion] TOTAL Removable Count: " + countRemoved);
		System.out.println(
				"[removeSequencesWithoutAccessionNumberVersion] Remaining Sequences: " + (count - countRemoved));

		return countRemoved;
	}

	protected boolean removeNonExistentSequencesFromHeadersDB() {

		boolean returnValue = true;

		// Total number of entries found in the DB
		long count = 0;

		String[] keyWords = ORFanMineUtils.getDbCurationKeyWords();
		// Total number of entries removed
		long countRemoved = 0;
		// Total number of entries found in the target DB which are part of this tier
		// curation
		long[] countRemovables = new long[keyWords.length];

		for (int i = 0; i < countRemovables.length; i++)
			countRemovables[i] = 0;

		String headersFilePath = null;
		if (dbType == DBType.NUCL)
			headersFilePath = ORFanMineUtils.getNuclDBHeaders();
		else
			headersFilePath = ORFanMineUtils.getProtDBHeaders();
		BufferedReader br = ORFanMineUtils.openReader(headersFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(ORFanMineUtils.getTmpFilePath(headersFilePath), false);

		try {
			boolean keyWordFound = false;
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
				if (count % 10000000 == 0)
					System.out.println("[removeNonExistentSequencesFromHeadersDB] 10xM: " + count / 10000000);
				int transcriptIndex = contentLine.indexOf((char) 1);// contains char: '' -> ascii = 1
				if (transcriptIndex > 0)
					contentLine = contentLine.substring(0, transcriptIndex);
				keyWordFound = false;
				String contentLineLower = contentLine.toLowerCase();
				for (int i = 0; i < keyWords.length; i++) {
					if (contentLineLower.contains(keyWords[i])) {
						if (!keyWordFound)
							countRemoved++;
						keyWordFound = true;
						countRemovables[i]++;
					}
				}
				if (!keyWordFound) {
					bw.write(contentLine + "\n");
				}
				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			returnValue = false;
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		ORFanMineUtils.replaceFile(headersFilePath, ORFanMineUtils.getTmpFilePath(headersFilePath));

		System.out.println("TOTAL lines count: " + count);
		for (int i = 0; i < keyWords.length; i++)
			System.out.println("Removable " + keyWords[i] + " = " + countRemovables[i]);
		System.out.println("TOTAL CountRemovables: " + countRemoved);
		System.out.println("Remaining Sequences: " + (count - countRemoved));

		return returnValue;
	}

	protected long generateFullHeadersDB() {
		BufferedReader br = ORFanMineUtils.openReader(dbFilePath);
		String headersFilePath = null;
		if (dbType == DBType.NUCL)
			headersFilePath = ORFanMineUtils.getNuclDBHeaders();
		else
			headersFilePath = ORFanMineUtils.getProtDBHeaders();
		BufferedWriter bw = ORFanMineUtils.getWriter(headersFilePath, false);

		long count = 0;
		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				if (contentLine.startsWith(">")) {
					count++;
					if (count % 10000000 == 0)
						System.out.println("[generateFullHeadersDB] 10xM: " + count / 10000000);
					bw.write(contentLine + "\n");
				}
				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}

		System.out.println("[generateFullHeadersDB] Nr of sequences: " + count);
		return count;
	}

	public String getDbFilePath() {
		return dbFilePath;
	}

	public DBType getDbType() {
		return dbType;
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println("\tjava orfanbase.dbcuration.DBCuration -file databaseFilePath -type molecule_type");
		System.out.println("\t\tmolecule_type: \"nucl\" || \"prot\"");
	}

}
