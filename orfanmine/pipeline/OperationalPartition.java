package orfanmine.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.TreeSet;

import orfanmine.util.DBType;
import orfanmine.util.ORFanMineUtils;

public class OperationalPartition {

	private String dbFilePath;
	private String outputFilePath = null;
	private Integer taxID;
	private Integer taxExcludeID;
	private DBType dbType;

	public static void main(String[] args) {

		// Timer
		long startTime = System.nanoTime();

		long endTime = System.nanoTime();
		long timeInSeconds = (endTime - startTime) / 1000000000;
		System.out.println("Execution time: " + (timeInSeconds / 60) + " min " + (timeInSeconds % 60) + " sec");
	}

	public OperationalPartition(String dbFilePath, DBType dbType, String outputFilePath, int taxID, int taxExcludeID) {
		this(dbFilePath, dbType, outputFilePath, taxID);
		this.taxExcludeID = taxExcludeID;
	}

	public OperationalPartition(String dbFilePath, DBType dbType, String outputFilePath, int taxID) {
		this.dbFilePath = dbFilePath;
		this.dbType = dbType;
		this.outputFilePath = outputFilePath;
		this.taxID = taxID;
	}

	public long extractDBTaxID() {

		TreeSet<Integer> taxIDSet;
		if (taxExcludeID == null)
			taxIDSet = ORFanMineUtils.exportTaxIDSet(taxID);
		else
			taxIDSet = ORFanMineUtils.exportTaxIDSetExclude(taxID, taxExcludeID);

		BufferedReader br = ORFanMineUtils.openReader(dbFilePath);

		String targetPath = null;
		if (outputFilePath == null) {
			targetPath = dbFilePath + "_" + taxID;
			if (taxExcludeID != null)
				targetPath += "-" + taxExcludeID;
		} else
			targetPath = outputFilePath;
		BufferedWriter bw = ORFanMineUtils.getWriter(targetPath, false);

		long count = 0;
		long countRef = 0;
		boolean saveLine = false;
		try {
			String readLine = br.readLine();
			while (readLine != null) {
				if (readLine.startsWith(">")) {
					saveLine = false;
					count++;
//					if (dbType == DBType.NUCL) {
//						if (count % 1250000 == 0) {
//							if (count == 1250000)
//								System.out.print("[extractDBTaxID]: 10%");
//							else
//								System.out.print(" > " + count / 125000 + "%");
//						}
//					} else {
//						if (count % 635800 == 0) {
//							if (count == 635800)
//								System.out.print("[extractDBTaxID]: 10%");
//							else
//								System.out.print(" > " + count / 635700 + "%");
//						}
//					}
					// [0] ">" + AccessionNumber + "." + Version
					// [1] taxID
					// [2..] annotations
					String[] strArray = readLine.split(" ");
					Integer taxID = Integer.valueOf(strArray[1]);
					if (taxIDSet.contains(taxID)) {
						saveLine = true;
						countRef++;
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
//			System.out.println(" > 100% > DONE!");
			if (taxExcludeID == null)
				System.out.println("[extractDBTaxID] Total number of sequences found in taxon " + taxID + ": "
						+ countRef + " out of " + count);
			else
				System.out.println("[extractDBTaxID] Total number of sequences found in taxon " + taxID
						+ " excluding taxon " + taxExcludeID + ": " + countRef);
		}
		return countRef;
	}

	public long extractHeadersTaxID() {

		TreeSet<Integer> taxIDSet;
		if (taxExcludeID == null)
			taxIDSet = ORFanMineUtils.exportTaxIDSet(taxID);
		else
			taxIDSet = ORFanMineUtils.exportTaxIDSetExclude(taxID, taxExcludeID);

		BufferedReader br = ORFanMineUtils.openReader(dbFilePath);
		String targetPath = null;
		if (outputFilePath == null) {
			targetPath = dbFilePath + "_" + taxID;
			if (taxExcludeID != null)
				targetPath += "-" + taxExcludeID;
		} else
			targetPath = outputFilePath;
		BufferedWriter bw = ORFanMineUtils.getWriter(targetPath, false);

		long count = 0;
		long countRef = 0;
		try {
			String readLine = br.readLine();
			while (readLine != null) {
				count++;
				String[] strArray = readLine.split(" ");
				Integer taxID = Integer.valueOf(strArray[1]);
				if (taxIDSet.contains(taxID)) {
					bw.write(readLine + "\n");
					countRef++;
				}
				readLine = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
//			System.out.println("[siftHeadersTaxID] Total number of sequences parsed from db: " + count);
//			System.out.println("[siftHeadersTaxID] Total number of sequences found in taxon: " + countRef);
		}
		return countRef;
	}

}
