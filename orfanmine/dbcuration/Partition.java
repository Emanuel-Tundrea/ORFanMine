package orfanmine.dbcuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import orfanmine.util.DBType;
import orfanmine.util.ORFanMineUtils;
import orfanmine.util.TaxNode;
import orfanmine.util.TaxRank;

public class Partition {

	// database type: NUCL || PROT
	private DBType type;

	// the taxonomic granularity to partition (genus, family, ...). GENUS, by
	// default.
	private TaxRank level = TaxRank.GENUS;

	// the minimum number of sequences to qualify a level for having its own
	// database partition
	private long minNrSeqs;

	public static void main(String[] args) {
//		String[] argsNew = { "-type", "nucl" };
//		args = argsNew;

		if (args.length < 2) {
			System.out.println("[Partition] Error: Wrong syntax");
			printUsage();
			return;
		}

		DBType dbType = null;
		TaxRank level = TaxRank.GENUS;
		long minNrSeqs = -1;// ORFanMineUtils.getProtMinNrSeqsPartitionDB();

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-type":
				if ((i + 1) < args.length)
					dbType = DBType.getDBTypeArg(args[++i]);
				break;
			case "-level":
				if ((i + 1) < args.length)
					level = TaxRank.identifyRank(args[++i]);
				break;
			case "-min_nr_seqs":
				if ((i + 1) < args.length)
					minNrSeqs = Integer.valueOf(args[++i]);
				break;
			}
		}
		if (dbType == null) {
			System.out.println(
					"[Partition] Error: Argument \"-type\". Mandatory value is missing:  `String, `nucl', `prot''");
			printUsage();
			return;
		}
		if (minNrSeqs < 0) {
			if (dbType == DBType.NUCL)
				minNrSeqs = ORFanMineUtils.getNuclMinNrSeqsPartitionDB();
			else
				minNrSeqs = ORFanMineUtils.getProtMinNrSeqsPartitionDB();
		}

		Partition p = new Partition(dbType, level, minNrSeqs);
		TreeMap<Integer, Integer> ranksMap = p.buildPartitionStatistics();
		TreeSet<Integer> dbList = p.dbList(ranksMap);
		System.out.println("Config size: " + dbList.size());
		p.partitionDB(dbList);
		p.generateMakeBlastDBScript(dbList);
	}

	public Partition(DBType type) {
		this.type = type;
		if (type == DBType.NUCL)
			minNrSeqs = ORFanMineUtils.getNuclMinNrSeqsPartitionDB();
		else
			minNrSeqs = ORFanMineUtils.getProtMinNrSeqsPartitionDB();
	}

	public Partition(DBType type, TaxRank level, long minNrSeqs) {
		this.type = type;
		this.level = level;
		this.minNrSeqs = minNrSeqs;
	}

	public void generateMakeBlastDBScript(TreeSet<Integer> dbList) {
		BufferedWriter bwMakeBlastDBScript = ORFanMineUtils.getWriter(ORFanMineUtils.getMakeBlastDBScript(), false);
		try {
//			if (type == DBType.NUCL) {
//				bwMakeBlastDBScript.write("rm " + ORFanMineUtils.getNuclDB() + "\n");
//				bwMakeBlastDBScript.write("rm " + ORFanMineUtils.getNuclDBHeaders() + "\n");
//			} else {
//				bwMakeBlastDBScript.write("rm " + ORFanMineUtils.getProtDB() + "\n");
//				bwMakeBlastDBScript.write("rm " + ORFanMineUtils.getProtDBHeaders() + "\n");
//			}
			for (Integer rankID : dbList) {
				String dbPartitionName = ORFanMineUtils.getDBPartitionFileName(rankID, type);
				bwMakeBlastDBScript.write("makeblastdb -in " + dbPartitionName + " -dbtype " + type.getName()
						+ " -parse_seqids -taxid_map " + ORFanMineUtils.getTaxIDDBPartitionFileName(rankID, type)
						+ " -blastdb_version 5\n");
//				bwMakeBlastDBScript.write("rm " + dbPartitionName + "\n");
//				bwMakeBlastDBScript.write("rm " + ORFanMineUtils.getTaxIDDBPartitionFileName(rankID, type) + "\n");
			}
			bwMakeBlastDBScript.write("makeblastdb -in " + ORFanMineUtils.getUnclassifiedDBName(type) + " -dbtype "
					+ type.getName() + " -parse_seqids -taxid_map " + ORFanMineUtils.getTaxIDUnclassifiedDBName(type)
					+ " -blastdb_version 5\n");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeWriter(bwMakeBlastDBScript);
		}
	}

	public void partitionDB(TreeSet<Integer> dbList) {
		long count = 0;

		String dbPath = null;
		if (type.equals(DBType.NUCL))
			dbPath = ORFanMineUtils.getNuclDB();
		else
			dbPath = ORFanMineUtils.getProtDB();
		BufferedReader br = ORFanMineUtils.openReader(dbPath);

		// prepare output databases
		TreeMap<Integer, BufferedWriter> dbMap = new TreeMap<Integer, BufferedWriter>();
		TreeMap<Integer, BufferedWriter> dbTaxIDMap = new TreeMap<Integer, BufferedWriter>();
		for (Integer rankID : dbList) {
			String dbPartitionPath = ORFanMineUtils.getDBPartitionFilePath(rankID, type);
			BufferedWriter bw = ORFanMineUtils.getWriter(dbPartitionPath, false);
			dbMap.put(rankID, bw);
			String dbTaxIDPartitionPath = ORFanMineUtils.getTaxIDDBPartitionFilePath(rankID, type);
			BufferedWriter bwTaxID = ORFanMineUtils.getWriter(dbTaxIDPartitionPath, false);
			dbTaxIDMap.put(rankID, bwTaxID);
		}
		BufferedWriter bwUnclassified = ORFanMineUtils.getWriter(ORFanMineUtils.getUnclassifiedDBPath(type), false);
		BufferedWriter bwTaxIDUnclassified = ORFanMineUtils.getWriter(ORFanMineUtils.getTaxIDUnclassifiedDBPath(type),
				false);
		BufferedWriter bwClassified = ORFanMineUtils.getWriter(ORFanMineUtils.getClassifiedDBPath(type), false);
		BufferedWriter bwTaxIDClassified = ORFanMineUtils.getWriter(ORFanMineUtils.getTaxIDClassifiedDBPath(type),
				false);

		try {
			BufferedWriter currentWriter = null;
			String contentLine = br.readLine();
			while (contentLine != null) {
				if (contentLine.startsWith(">")) {
					count++;
					if (count % 1000000 == 0)
						System.out.println("MILLION: " + count / 1000000);

					String[] strArray = contentLine.split(" ");
					String accessionNumber = strArray[0].substring(1);
					Integer taxon = Integer.valueOf(strArray[1]);
					Integer rankID = TaxNode.identifyLineageSpecificRank(taxon, level);

					// save in the unclassified dump
					if (rankID == null) {
						currentWriter = bwUnclassified;
						bwTaxIDUnclassified.write(accessionNumber + " " + taxon + "\n");
					} else {
						// save in the selected database partition
						if (dbList.contains(rankID)) {
							currentWriter = dbMap.get(rankID);
							BufferedWriter taxIDMapWriter = dbTaxIDMap.get(rankID);
							taxIDMapWriter.write(accessionNumber + " " + taxon + "\n");
						}
						// save in the classified dump
						else {
							currentWriter = bwClassified;
							bwTaxIDClassified.write(accessionNumber + " " + taxon + "\n");
						}
					}
				}
				currentWriter.write(contentLine + "\n");
				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bwUnclassified);
			ORFanMineUtils.closeWriter(bwTaxIDUnclassified);
			ORFanMineUtils.closeWriter(bwClassified);
			ORFanMineUtils.closeWriter(bwTaxIDClassified);
			Set set = dbMap.entrySet();
			Iterator iterator = set.iterator();
			while (iterator.hasNext()) {
				Map.Entry mentry = (Map.Entry) iterator.next();
				BufferedWriter bw = (BufferedWriter) mentry.getValue();
				ORFanMineUtils.closeWriter(bw);
			}
			Set setTaxID = dbTaxIDMap.entrySet();
			Iterator iteratorTaxID = setTaxID.iterator();
			while (iteratorTaxID.hasNext()) {
				Map.Entry mentry = (Map.Entry) iteratorTaxID.next();
				BufferedWriter bw = (BufferedWriter) mentry.getValue();
				ORFanMineUtils.closeWriter(bw);
			}
		}
		System.out.println("TOTAL sequence count: " + count);

	}

	public TreeSet<Integer> dbList(TreeMap<Integer, Integer> ranksMap) {
		String configurationFilePath = null;
		if (type.equals(DBType.NUCL))
			configurationFilePath = ORFanMineUtils.getNuclDBConfiguration();
		else
			configurationFilePath = ORFanMineUtils.getProtDBConfiguration();
		BufferedWriter bw = ORFanMineUtils.getWriter(configurationFilePath, false);

		TreeSet<Integer> dbList = new TreeSet<Integer>();
		Set set = ranksMap.entrySet();
		Iterator iterator = set.iterator();
		try {
			while (iterator.hasNext()) {
				Map.Entry mentry = (Map.Entry) iterator.next();
				Integer rankID = (Integer) mentry.getKey();
				Integer countSeqsNr = (Integer) mentry.getValue();
				if (countSeqsNr >= minNrSeqs) {
					dbList.add(rankID);
					bw.write(rankID + " ");
				}
//				if ((countSeqsNr > 5) && (countSeqsNr < 15)) {
//					System.out.println(rankID + "->" + countSeqsNr);
//				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeWriter(bw);
		}
		return dbList;
	}

	public static TreeSet<Integer> loadDBList(DBType dbType) {
		String configurationFilePath = null;
		if (dbType.equals(DBType.NUCL))
			configurationFilePath = ORFanMineUtils.getNuclDBConfiguration();
		else
			configurationFilePath = ORFanMineUtils.getProtDBConfiguration();
		BufferedReader br = ORFanMineUtils.openReader(configurationFilePath);
		TreeSet<Integer> dbList = new TreeSet<Integer>();
		try {
			String contentLine = br.readLine();
			String[] strArray = contentLine.split(" ");
			for (String str : strArray) {
				Integer rankID = Integer.valueOf(str);
				dbList.add(rankID);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
		}
		return dbList;
	}

	public TreeMap<Integer, Integer> buildPartitionStatistics() {
		long count = 0;
		long countUnclassified = 0;

		TreeMap<Integer, Integer> ranksMap = new TreeMap<Integer, Integer>();
		String headerFilePath = null;
		if (type.equals(DBType.NUCL))
			headerFilePath = ORFanMineUtils.getNuclDBHeaders();
		else
			headerFilePath = ORFanMineUtils.getProtDBHeaders();
		BufferedReader br = ORFanMineUtils.openReader(headerFilePath);

		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
				if (count % 10000000 == 0)
					System.out.println("MILLION: " + count / 10000000 + "0");

				String[] strArray = contentLine.split(" ");
				Integer taxon = Integer.valueOf(strArray[1]);
				Integer rankID = TaxNode.identifyLineageSpecificRank(taxon, level);

				if (rankID == null)
					countUnclassified++;
				else {
					if (ranksMap.containsKey(rankID)) {
						Integer countSeqsNr = ranksMap.get(rankID);
						countSeqsNr++;
						ranksMap.put(rankID, countSeqsNr);
					} else {
						ranksMap.put(rankID, 1);
					}
				}
				contentLine = br.readLine();
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
		}
		System.out.println("TOTAL sequence count: " + count);
		System.out.println("TOTAL sequence unclassified: " + countUnclassified);
		System.out.println("TOTAL generaMap count: " + ranksMap.size());

		return ranksMap;

	}

	public static void printUsage() {
		System.out.println("USAGE:");
		System.out.println(
				"\tjava orfanmine.dbcuration.Partition -type molecule_type [-level taxonomicGranularity] [-min_nr_seqs minNumberSequences]");
		System.out.println("\t\tmolecule_type: \"nucl\" || \"prot\"");
		System.out.println("\t\tlevel: \"genus\" [default] || \"family\" ...");
		System.out.println(
				"\t\tmin_nr_seqs: the minimum number of sequences to qualify a level for having its own database partition. By default:  "
						+ ORFanMineUtils.getProtMinNrSeqsPartitionDB() + " for proteins and "
						+ ORFanMineUtils.getNuclMinNrSeqsPartitionDB() + " for nucleotides.");
	}
}
