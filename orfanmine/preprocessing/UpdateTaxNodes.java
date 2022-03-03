package orfanmine.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import orfanmine.util.ORFanMineUtils;
import orfanmine.util.TaxRank;

public class UpdateTaxNodes {

	private String nodesDumpFilePath = null;

	// args[0] - nodes dump file path
	public static void main(String[] args) {

		// Timer
		long startTime = System.nanoTime();

		if (args.length < 1) {
			System.out.println("[UpdateTaxNodes] Error: Please specify the nodes dump file path!");
			printUsage();
			return;
		}

		UpdateTaxNodes serviceObj = new UpdateTaxNodes(args[0]);

		serviceObj.updateTaxNodesTable();

		long endTime = System.nanoTime();
		long timeInSeconds = (endTime - startTime) / 1000000000;
		System.out.println("Execution time: " + (timeInSeconds / 60) + " min " + (timeInSeconds % 60) + " sec");
	}

	public UpdateTaxNodes(String nodesDumpFilePath) {
		this.nodesDumpFilePath = nodesDumpFilePath;
	}

	private boolean updateTaxNodesTable() {
		if (nodesDumpFilePath == null) {
			System.out.println("[UpdateTaxNodes] Error: Nodes dump file path NULL!");
			printUsage();
			return false;
		}

		BufferedReader br = ORFanMineUtils.openReader(nodesDumpFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter(ORFanMineUtils.getTaxDBNodesFilePath(), false);

		long count = 0;// total = 2,361,824

		String contentLine = null;
		try {
			contentLine = br.readLine();
			while (contentLine != null) {
				count++;
//				if (count % 1000000 == 0)
//					System.out.println("MILLION: " + count / 1000000);
//				[0] - tax_id					-- node id in GenBank taxonomy database
//				[1] - separator
//				[2] - parent tax_id				-- parent node id in GenBank taxonomy database
//				[3] - separator
//				[4] - rank					-- rank of this node (superkingdom, kingdom, ...) 

				String[] strArray = contentLine.split("\\t");

				Integer tax_id = Integer.valueOf(strArray[0]);
				Integer parent_tax_id = Integer.valueOf(strArray[2]);
				String rank = strArray[4];
				if ((tax_id == 1) && (parent_tax_id == 1))
					rank = TaxRank.ROOT.getName();

				bw.write(tax_id + " " + parent_tax_id + " " + rank + "\n");

				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			System.out.println("Could not decode line: " + contentLine + "\n");
			ioe.printStackTrace();
			return false;
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		System.out.println("[updateTaxNodesTable] Line total: " + count + "...");
		return true;
	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println("\tjava orfanbase.preprocessing.util.UpdateTaxNodes nodesDumpFilePath");
	}
}
