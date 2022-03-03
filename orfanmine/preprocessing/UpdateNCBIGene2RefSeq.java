package orfanmine.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import orfanmine.util.ORFanMineUtils;

public class UpdateNCBIGene2RefSeq {

	private String filePath = null;

	// args[0] - ftp.ncbi.nlm.nih.gov\gene\DATA\gene_orthologs.gz
	// 20/Nov/2021 -
	// Total number of lines: 50,653,662
	// Lines with Acc #: 47,528,328
	// Nucl Acc # : 41,407,965
	// Prot Acc # : 42,589,715
	public static void main(String[] args) {

		// Timer
		long startTime = System.nanoTime();

		if (args.length < 1) {
			System.out.println("[UpdateNCBIGene2RefSeq] Error: Please specify the 'gene2refseq' file path!");
			printUsage();
			return;
		}

//		UpdateNCBIGene2RefSeq serviceObj = new UpdateNCBIGene2RefSeq("e:\\NCBI\\Orthologs\\Gene\\gene2refseq");

		UpdateNCBIGene2RefSeq serviceObj = new UpdateNCBIGene2RefSeq(args[0]);

		serviceObj.updateGene2RefSeqTable();

		long endTime = System.nanoTime();
		long timeInSeconds = (endTime - startTime) / 1000000000;
		System.out.println("Execution time: " + (timeInSeconds / 60) + " min " + (timeInSeconds % 60) + " sec");
	}

	public UpdateNCBIGene2RefSeq(String filePath) {
		this.filePath = filePath;
	}

	private boolean updateGene2RefSeqTable() {
		if (filePath == null) {
			System.out.println("[updateGene2RefSeqTable] Error: 'gene2refseq' file path not found!");
			return false;
		}

		BufferedReader br = ORFanMineUtils.openReader(filePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(ORFanMineUtils.getGene2RefSeq(), false);

		long count = 0;
		long newCount = 0;
		long ntAccNrCount = 0;
		long nrAccNrCount = 0;
		String saveNtAccNr = "-";
		String saveNrAccNr = "-";

		try {
			String readLine = br.readLine();
			// skip the header
			readLine = br.readLine();
			while (readLine != null) {
				count++;
//				if (count % 1000000 == 0)
//					System.out.println("MILLION [updateGene2RefSeqTable]: " + count / 1000000);

				String[] strArray = readLine.split("\t");
				/*
				 * [1] GeneID [3] RNA_nucleotide_accession.version [5] protein_accession.version
				 */
				String geneID = strArray[1];
				String ntAccNr = strArray[3];
				String nrAccNr = strArray[5];
				// remove duplicates
				if (saveNtAccNr.equals(ntAccNr) && saveNrAccNr.equals(nrAccNr)) {
					readLine = br.readLine();
					continue;
				} else {
					saveNtAccNr = ntAccNr;
					saveNrAccNr = nrAccNr;
				}
				// remove missing accession numbers
				if (!(ntAccNr.equals("-") && nrAccNr.equals("-"))) {
					if (ntAccNr.indexOf(".") > 0) {
						ntAccNr = ntAccNr.substring(0, ntAccNr.indexOf("."));
						ntAccNrCount++;
					}
					if (nrAccNr.indexOf(".") > 0) {
						nrAccNr = nrAccNr.substring(0, nrAccNr.indexOf("."));
						nrAccNrCount++;
					}
					bw.write(geneID + "\t" + ntAccNr + "\t" + nrAccNr + "\n");
					newCount++;
				}

				readLine = br.readLine();
			}
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Total number of lines: " + count + "\nLines with Acc #: " + newCount);
		System.out.println("Nucl Acc # : " + ntAccNrCount + "\nProt Acc # : " + nrAccNrCount);
		return true;
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println("\tjava orfanbase.preprocessing.util.UpdateNCBIGene2RefSeq 'gene2refseq' file path");
	}
}
