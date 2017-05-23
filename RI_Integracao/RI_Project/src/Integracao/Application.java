package Integracao;

import java.io.IOException;

import classificador.Classificador;
import crawler.SpiderFactory;
import wrapper.Wrapper;

public class Application {
	
	private static String domain[] = { "americanas", "fastgames", "magazineluiza","saraiva", "livrariacultura", "gamestop", "submarino", "walmart" , "store.playstation", "steampowered"};
	
	public static void main(String[] args) throws Exception, InterruptedException {
		
		crawler();
		
		classificador();
		
		wrapper();
	}
	
	public static void crawler() throws IOException, InterruptedException {
		SpiderFactory sf = new SpiderFactory();
		sf.startCrawlers("heuristica");
	}
	
	public static void classificador() throws Exception{
		Classificador.classificador();
	}
	
	public static void wrapper(){
		Wrapper wp = new  Wrapper();
		wp.Start(domain, "heuristica");
	}
}
