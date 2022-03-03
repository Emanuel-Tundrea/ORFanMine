package orfanmine.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.TreeSet;

//https://v101.orthodb.org/download/odb10v1_gene_xrefs.tab.gz
public class OrthoDBType extends OrthologDBType {

	private static final String orthoDBSuffix = "_ncbi.tab";
	private static final String ncbiProtIdentifier = "NCBIproteinAcc";
	private static final String ncbiNuclIdentifier = "NCBIgenename";

	public OrthoDBType(String orthoDB, String targetDB, DBType targetDBType) {
		super(orthoDB, targetDB, targetDBType);
	}

	public long removeOrthologsFromHeaders() {

		identifyNCBIAccNrFromOrthoDB();

		BufferedReader br = ORFanMineUtils.openReader(getOrthoDBInternalFileName());

		long count = 0;
		long removedOrthologs = 0;
		int step = 0;

		TreeSet<String> orthoList = new TreeSet<String>();

		try {
			String orthoAccNr = br.readLine();
			while (orthoAccNr != null) {
				count++;
				orthoList.add(orthoAccNr);
				if ((count % 20000000) == 0) {
					step++;
					removedOrthologs += removeOrthologsFromHeadersStep(orthoList, step);
					orthoList.clear();
				}
				orthoAccNr = br.readLine();
			}
			step++;
			removedOrthologs += removeOrthologsFromHeadersStep(orthoList, step);
			orthoList.clear();
			if (step > 1) {
				File tempFile = new File(getTmpFilePath(getTargetDB()) + "_" + (step - 1));
				tempFile.delete();
			}
			File resultFile = new File(getTargetDB());
			resultFile.delete();
			File tempFile = new File(getTmpFilePath(getTargetDB()) + "_" + step);
			tempFile.renameTo(resultFile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
		}
		System.out.println("Removed orthologs: " + removedOrthologs);
		return removedOrthologs;
	}

	private long identifyNCBIAccNrFromOrthoDB() {
		BufferedReader br = ORFanMineUtils.openReader(getOrthoDB());
		BufferedWriter bw = ORFanMineUtils.getWriter(getOrthoDBInternalFileName(), false);

		long count = 0;
		long ncbiAccNr = 0;

		try {
			String readLine = br.readLine();
			while (readLine != null) {
				count++;
//				odb10v1_OG_xrefs.tab
//				1.	OG unique id
//				2.	external DB or DB section
//				3.	external identifier
//				4.	number of genes in the OG associated with the identifier

				String[] strArray = readLine.split("\t");
				String dbTypeIdentifier = null;
				if (getTargetDBType() == DBType.NUCL)
					dbTypeIdentifier = ncbiNuclIdentifier;
				else
					dbTypeIdentifier = ncbiProtIdentifier;
				String externalIdentifier = strArray[2];
				if (externalIdentifier.equals(dbTypeIdentifier)) {
					String accessionNumber = strArray[1].substring(0, strArray[1].indexOf("."));
					bw.write(accessionNumber + "\n");
					ncbiAccNr++;
				}

				readLine = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		System.out.println("Total line count [extractNCBIAccNrFromOrthoDB]: " + count
				+ "\nNCBI Accession Numbers found: " + ncbiAccNr);
		return ncbiAccNr;
	}

	private String getOrthoDBInternalFileName() {
		return (getOrthoDB() + orthoDBSuffix);
	}

	public static String getCmdParam() {
		return "ortho";
	}

}
