package orfanmine.util;

import java.io.BufferedReader;
import java.io.File;
import java.util.TreeSet;

//ftp.ncbi.nlm.nih.gov\gene\DATA\gene_orthologs.gz
public class NcbiDBType extends OrthologDBType {

	public NcbiDBType(String orthoDB, String targetDB, DBType targetDBType) {
		super(orthoDB, targetDB, targetDBType);
	}

	public long removeOrthologsFromHeaders() {

		TreeSet<String> orthologGeneIDList = loadNCBIOrthologGeneIDs();
		TreeSet<String> existingOrthologAccNrList = identifyNCBIOrthologsAccNrFromGeneID(orthologGeneIDList);

		long count = removeOrthologsFromHeadersStep(existingOrthologAccNrList, 1);

		File resultFile = new File(getTargetDB());
		resultFile.delete();
		File tempFile = new File(getTmpFilePath(getTargetDB()) + "_" + 1);
		tempFile.renameTo(resultFile);

		System.out.println("Removed orthologs: " + count);
		return count;
	}

	private TreeSet<String> identifyNCBIOrthologsAccNrFromGeneID(TreeSet<String> orthologsIDList) {
		BufferedReader brGene2RefSeq = ORFanMineUtils.openReader(ORFanMineUtils.getGene2RefSeq());

		TreeSet<String> existingOrthologAccNrList = new TreeSet<String>();
		try {
			String readLine = brGene2RefSeq.readLine();
			while (readLine != null) {
				String[] strArray = readLine.split("\t");
				/*
				 * [0] GeneID [1] RNA_nucleotide_accession.version [2] protein_accession.version
				 */
				String geneID = strArray[0];
				String accessionNumber = null;
				if (getTargetDBType() == DBType.NUCL)
					accessionNumber = strArray[1];
				else
					accessionNumber = strArray[2];
				if (!accessionNumber.equals("-") && orthologsIDList.contains(geneID)) {
					existingOrthologAccNrList.add(accessionNumber);
				}
				readLine = brGene2RefSeq.readLine();
			}
			ORFanMineUtils.closeReader(brGene2RefSeq);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(brGene2RefSeq);
		}
		System.out.println(
				"[identifyNCBIGeneOrthologsAccNr] NCBI Accession Numbers found: " + existingOrthologAccNrList.size());
		return existingOrthologAccNrList;
	}

	private TreeSet<String> loadNCBIOrthologGeneIDs() {
//		System.out.print("Loading ortholog list ... ");
		BufferedReader brOrhologs = ORFanMineUtils.openReader(getOrthoDB());
		TreeSet<String> orthologGeneIDList = new TreeSet<String>();
		try {
			String readLine = brOrhologs.readLine();
			// skip the first line (headers)
			readLine = brOrhologs.readLine();
			while (readLine != null) {
				String[] strArray = readLine.split("\t");
				/*
				 * [0] #tax_id [1] GeneID [2] relationship [3] Other_tax_id [4] Other_GeneID
				 */
				orthologGeneIDList.add(strArray[1]);
				orthologGeneIDList.add(strArray[4]);

				readLine = brOrhologs.readLine();
			}
			ORFanMineUtils.closeReader(brOrhologs);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println("DONE!");
		System.out.println("Number of orthologs (NCBI harvest): " + orthologGeneIDList.size());
		return orthologGeneIDList;
	}

	public static String getCmdParam() {
		return "ncbi";
	}
}
