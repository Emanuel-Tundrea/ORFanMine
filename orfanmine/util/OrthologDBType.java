package orfanmine.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.TreeSet;

public abstract class OrthologDBType {

	private static final String dbTmpSuffix = "_tmp";
	private String orthoDB;
	private String targetDB;
	private DBType targetDBType;

	public OrthologDBType(String orthoDB, String targetDB, DBType targetDBType) {
		this.orthoDB = orthoDB;
		this.targetDB = targetDB;
		this.targetDBType = targetDBType;
	}

	public abstract long removeOrthologsFromHeaders();

	protected long removeOrthologsFromHeadersStep(TreeSet<String> orthoDB, int step) {
		String sourceDB = targetDB;
		if (step > 2) {
			File tempFile = new File(getTmpFilePath(targetDB) + "_" + (step - 2));
			tempFile.delete();
		}
		if (step > 1)
			sourceDB = getTmpFilePath(targetDB) + "_" + (step - 1);
		BufferedReader br = ORFanMineUtils.openReader(sourceDB);
		BufferedWriter bw = ORFanMineUtils.getWriter(getTmpFilePath(targetDB) + "_" + step, false);

		long count = 0;
		long removedOrthologs = 0;
		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
//				if (count % 1000000 == 0)
//					System.out.println("MILLION [removeOrthologsFromHeadersStep: " + step + "]: " + count / 1000000);

				// [0] -> accession_number
				// [1] -> tax ID
				String[] strArray = contentLine.split(" ");

				if (orthoDB.contains(strArray[0])) {
					removedOrthologs++;
				} else
					bw.write(contentLine + "\n");
				contentLine = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			ORFanMineUtils.closeWriter(bw);
		}
		return removedOrthologs;
	}

	public String getOrthoDB() {
		return orthoDB;
	}

	public String getTargetDB() {
		return targetDB;
	}

	public DBType getTargetDBType() {
		return targetDBType;
	}

	protected String getTmpFilePath(String tmpFilePath) {
		return (tmpFilePath + dbTmpSuffix);
	}
}
