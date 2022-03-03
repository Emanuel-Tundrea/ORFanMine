package orfanmine.dbcuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.TreeSet;

import orfanmine.util.ORFanMineUtils;

public class QueryCuration {

	private String resultsFilePath;

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("[QueryCuration] Error: Wrong syntax");
			printUsage();
			return;
		}

		QueryCuration queryCuration = new QueryCuration(args[0]);
		queryCuration.execute();
	}

	public QueryCuration(String resultsFilePath) {
		this.resultsFilePath = resultsFilePath;
	}

	public long execute() {

		BufferedReader br = ORFanMineUtils.openReader(resultsFilePath);

		TreeSet<String> duplicates = new TreeSet<String>();

		try {
			String readLine = br.readLine();
			while (readLine != null) {
				// [0] AccessionNumber + "." + Version
				// [1] identity percent
				// [2] Subject AccessionNumber
				String[] strArray = readLine.split("\t");
				String queryAccNr = strArray[0];
				Integer identityPercentage = Integer.valueOf(strArray[1]);
				String subjectAccNr = strArray[2];
				if (!queryAccNr.equals(subjectAccNr) && (identityPercentage > 90)) {
					if (queryAccNr.compareTo(subjectAccNr) < 0)
						duplicates.add(queryAccNr);
					else
						duplicates.add(subjectAccNr);

				}
				readLine = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
		}

		String queryFilePath = ORFanMineUtils.getQueryOperational();
		br = ORFanMineUtils.openReader(queryFilePath);
		String tempFilePath = ORFanMineUtils.getTmpFilePath(queryFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(tempFilePath, false);

		long countSaveSequences = 0;
		try {
			boolean saveSequence = true;
			String readLine = br.readLine();
			while (readLine != null) {
				if (readLine.startsWith(">")) {
					String[] strArray = readLine.split(" ");
					String accessionNumber = strArray[0].substring(1);
					if (duplicates.contains(accessionNumber))
						saveSequence = false;
					else {
						saveSequence = true;
						countSaveSequences++;
					}
				}
				if (saveSequence)
					bw.write(readLine + "\n");
				readLine = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
			ORFanMineUtils.replaceFile(queryFilePath, tempFilePath);
			System.out.println("Removed sequences: " + duplicates.size());
			System.out.println("Remaining sequences: " + countSaveSequences);
		}
		return countSaveSequences;
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println("\tjava orfanbase.pipeline.QueryCuration BLAST_Results_File_Path");
	}
}
