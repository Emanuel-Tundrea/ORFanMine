package orfanmine.util;

import java.io.BufferedWriter;

public class Test {

	public static void main(String[] args) {
		BufferedWriter bw = ORFanMineUtils.getWriter(ORFanMineUtils.getWorkSpaceDir() + "output.csv", false);
		try {
			boolean isFirst = true;
			for (String str : args)
				if (isFirst) {
					isFirst = false;
					bw.write(str);
				} else
					bw.write("," + str);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ORFanMineUtils.closeWriter(bw);
		}
	}

}
