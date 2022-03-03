package orfanmine.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class IndexGenera {

	public static void main(String[] args) {
//		String headerFilePath = ORFanMineUtils.getProtDBHeaders();// 29095 GENERA (224 > 10k)) // 5187 FAMILIES (211 >
		// 10k)
		String headerFilePath = ORFanMineUtils.getNuclDBHeaders();// 37815 GENERA (73 > 10k) // 6165 FAMILIES (87 > 10k)

		IndexGenera g = new IndexGenera();
		g.buildPartitionStatistics(headerFilePath, TaxRank.GENUS);
	}

	public TreeMap<Integer, Integer> buildPartitionStatistics(String headerFilePath, TaxRank partitionRank) {
		long count = 0;
		long countUnclassified = 0;

		TreeMap<Integer, Integer> generaMap = new TreeMap<Integer, Integer>();
		BufferedReader br = ORFanMineUtils.openReader(headerFilePath);
		BufferedWriter bw = ORFanMineUtils.getWriter("k:/ORFanMine/workspace/genera.nucl", false);

		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
				if (count % 1000000 == 0)
					System.out.println("MILLION: " + count / 1000000);

				String[] strArray = contentLine.split(" ");
				Integer taxon = Integer.valueOf(strArray[1]);
				Integer rankID = TaxNode.identifyLineageSpecificRank(taxon, partitionRank);

				if (rankID == null)
					countUnclassified++;
				else {
					if (generaMap.containsKey(rankID)) {
						Integer countSeqsNr = generaMap.get(rankID);
						countSeqsNr++;
						generaMap.put(rankID, countSeqsNr);
					} else {
						generaMap.put(rankID, 1);
					}
				}

				contentLine = br.readLine();
			}

			Set set = generaMap.entrySet();
			Iterator iterator = set.iterator();
			while (iterator.hasNext()) {
				Map.Entry mentry = (Map.Entry) iterator.next();
				bw.write(mentry.getKey() + "\t" + mentry.getValue() + "\n");
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		System.out.println("TOTAL sequence count: " + count);
		System.out.println("TOTAL sequence unclassified: " + countUnclassified);
		System.out.println("TOTAL generaMap count: " + generaMap.size());

		return generaMap;

	}

}
