package crawler;

import java.io.File;
import java.io.IOException;

public class SpiderFactory {

	private static final String domain[] = { "americanas", "fastgames", "magazineluiza",
			"saraiva", "livrariacultura", "gamestop", "submarino", "walmart" , "store.playstation", "steampowered"};
	

	public static String DOCUMENTOS_PATH = "documentos/";

	private Thread threads[];
	public static boolean error = false;

	public SpiderFactory() {
		this.threads = new Thread[10];
	}

	public void startCrawlers(String abordagem) throws IOException, InterruptedException {
		File f = new File(DOCUMENTOS_PATH);

		if (!f.exists())
			f.mkdirs();
		
		for (int i = 0 ; i < SpiderFactory.domain.length; i++){
			(this.threads[i] = new Thread(new Spider(abordagem, SpiderFactory.domain[i]))).start();
		}
		

		for (Thread t : this.threads){
			t.join();
		}	
		
		System.out.println("Terminou o crawler");

	}
	
}
