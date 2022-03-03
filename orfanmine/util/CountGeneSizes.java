package orfanmine.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CountGeneSizes {

	public static void main(String[] args) {

		// Timer
		long startTime = System.nanoTime();

		String sourceDBPath = "e:\\ORFanBase\\db\\nucl";
		String targetDBPath = "e:\\ORFanBase\\\\db\\nucl_sizes";

//		ArrayList<Integer> geneSizes = countGeneSizes(sourceDBPath);
//		saveGeneSizes(targetDBPath, geneSizes);

//		countAnnotatedGeneSizes(sourceDBPath, targetDBPath);// nucl: 75,232,634

//		System.out.println(ORFanMineUtils.getTaxDBName(9606));
//		System.out.println(ORFanMineUtils.getTaxDBName(7227));

		long endTime = System.nanoTime();
		System.out.println("Execution time in seconds: " + (endTime - startTime) / 1000000000);
	}

	public static long countAnnotatedGeneSizes(String sourceDBPath, String targetDBPath) {

		// Total number of entries found in the DB
		long count = 0;

		BufferedReader br = ORFanMineUtils.openReader(sourceDBPath);
		BufferedWriter bw = ORFanMineUtils.getWriter(targetDBPath, false);

		try {
			boolean fastaCode = true;
			int geneSize = 0;
			String saveAccNr = null;
			String saveTaxID = null;
			String contentLine = br.readLine();
			while (contentLine != null) {
				if (contentLine.startsWith(">")) {
					count++;
					if (count % 1000000 == 0)
						System.out.println("MILLION: " + count / 1000000);

					String[] strArray = contentLine.split(" ");

					fastaCode = false;
					if (geneSize > 0)
						bw.write(saveAccNr + " " + saveTaxID + " " + geneSize + "\n");
					geneSize = 0;
					int indexDot = strArray[0].indexOf("."); // accession number
					saveAccNr = contentLine.substring(1, indexDot);
					saveTaxID = strArray[1];
				} else
					fastaCode = true;
				if (fastaCode)
					geneSize += contentLine.length();

				contentLine = br.readLine();
			}

			// last gene indexed
			if (geneSize > 0)
				bw.write(saveAccNr + " " + geneSize + "\n");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		System.out.println("TOTAL sequence count: " + count);

		return count;
	}

	public static long countGeneSizes(String sourceDBPath, String targetDBPath) {

		// Total number of entries found in the DB
		long count = 0;

		BufferedReader br = ORFanMineUtils.openReader(sourceDBPath);
		BufferedWriter bw = ORFanMineUtils.getWriter(targetDBPath, false);

		try {
			boolean fastaCode = true;
			int geneSize = 0;
			String saveAccNr = null;
			String contentLine = br.readLine();
			while (contentLine != null) {
				if (contentLine.startsWith(">")) {
					count++;
					if (count % 1000000 == 0)
						System.out.println("MILLION: " + count / 1000000);

					fastaCode = false;
					if (geneSize > 0)
						bw.write(saveAccNr + " " + geneSize + "\n");
					geneSize = 0;
					int indexDot = contentLine.indexOf(".");
					if (indexDot > 0)
						saveAccNr = contentLine.substring(1, indexDot);
					else {
						int indexSpace = contentLine.indexOf(" ");
						saveAccNr = contentLine.substring(1, indexSpace);
					}

				} else
					fastaCode = true;
				if (fastaCode)
					geneSize += contentLine.length();

				contentLine = br.readLine();
			}

			// last gene indexed
			if (geneSize > 0)
				bw.write(saveAccNr + " " + geneSize + "\n");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		System.out.println("TOTAL sequence count: " + count);

		return count;
	}

	public static void saveGeneSizes(String targetDBPath, ArrayList<Integer> geneSizes) {

		BufferedWriter bw = null;
		try {
			File targetFile = new File(targetDBPath);
			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
			FileWriter fw = new FileWriter(targetFile);
			bw = new BufferedWriter(fw);

			for (int geneSize : geneSizes)
				bw.write(geneSize + "\n");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {

			try {
				if (bw != null)
					bw.close();
			} catch (Exception ex) {
				System.out.println("Error in closing the BufferedWriter: " + ex);
			}
		}
	}

	public static ArrayList<Integer> countGeneSizes(String sourceDBPath) {

		ArrayList<Integer> geneSizes = new ArrayList<Integer>();

		// Total number of entries found in the DB
		long count = 0;

		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(sourceDBPath));

			boolean fastaCode = true;
			int geneSize = 0;
			String contentLine = br.readLine();
			while (contentLine != null) {
				if (contentLine.startsWith(">")) {
					count++;
					if (count % 1000000 == 0)
						System.out.println("MILLION: " + count / 1000000);

					fastaCode = false;
					if (geneSize > 0) {
						geneSizes.add(geneSize);
					}
					geneSize = 0;
//					   String geneAccessionNumber = contentLine.substring(1, contentLine.indexOf(" "));
//					   bw.write(geneAccessionNumber + ",");
				} else
					fastaCode = true;
				if (fastaCode)
					geneSize += contentLine.length();

				contentLine = br.readLine();
			}

			// last gene indexed
			if (geneSize > 0) {
				geneSizes.add(geneSize);
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {

			try {
				if (br != null)
					br.close();
			} catch (IOException ioe) {
				System.out.println("Error in closing the BufferedReader: " + ioe);
			}

		}
//		System.out.println("TOTAL lines count: " + count);

		return geneSizes;
	}
}
