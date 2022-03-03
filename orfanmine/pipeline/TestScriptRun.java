package orfanmine.pipeline;

import java.io.IOException;

public class TestScriptRun {

	public static void main(String[] args) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		String[] cmd = new String[4 + args.length];
		cmd[0] = "java";
		cmd[1] = "-cp";
		cmd[2] = "code";
		cmd[3] = "orfanmine.util.Test";
		int i = 4;
		for (String arg : args)
			cmd[i++] = arg;

		processBuilder.command(cmd);
		try {
			Process process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
