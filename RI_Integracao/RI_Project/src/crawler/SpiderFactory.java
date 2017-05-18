package crawler;

import java.io.File;
import java.io.IOException;

public class SpiderFactory {

	private static final String domain[] = { "americanas", "fastgames", "magazineluiza",
			"nagem", "saraiva", "steampowered", "submarino", "walmart" };

	public static String ARTIFACT_PATH = "documentos\\";

	private Thread threads[];
	public static boolean error = false;

	public SpiderFactory() {
		this.threads = new Thread[10];
	}

	public void startCrawlers() throws IOException, InterruptedException {
		File f = new File(ARTIFACT_PATH);

		if (!f.exists())
			f.mkdirs();

		startHeuristicCrawlers();

		for (Thread t : this.threads){
			t.join();
		}	
		
		System.out.println("Terminou o crawler");

	}

	
	private void startHeuristicCrawlers() throws RuntimeException {
		ARTIFACT_PATH += "heuristica\\";
		for (int i = 0; i < 10; i++)
			(this.threads[i] = new Thread(new HeuristicSpider(SpiderFactory.domain[i]))).start();
	}
}
