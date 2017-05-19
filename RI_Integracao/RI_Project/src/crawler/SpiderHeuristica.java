package crawler;

import java.util.List;
import java.util.LinkedList;
import java.io.PrintWriter;
import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SpiderHeuristica extends Spider {

	private Dictionary goodTerms;
	private Dictionary badTerms;

	public SpiderHeuristica(String domain) {
		super(domain);
		createDictionaries();
	}

	private void createDictionaries() {
		goodTerms = new Dictionary();
		badTerms = new Dictionary();

		String[] words = "3ds_game_games_jogo_jogos_pc_ps1_ps2_ps3_ps4_psp_ps-1_ps-2_ps-3_ps-4_ps-vita_xbox_wii"
				.split("_");
		for (String w : words) {
			goodTerms.addWord(w);
		}

		words = "acessorio_acessorios_amiibo_capa_card_carregador_cartao_case_console_consoles_controle_guitarra_headset_mouse_teclado"
				.split("_");
		for (String w : words){
			badTerms.addWord(w);
		}
			

	}

	
	@Override
	void crawl(String url, int timeout) throws IOException, InterruptedException {
		Connection.Response html = connect(url, timeout);
		Thread.sleep(1000);

		// Get the page body
		saveHtml(html.body());
		this.cookies.putAll(html.cookies());

		// Get the next links
		Document doc = html.parse();
		Elements links = doc.select("a[href]");
		String tempLink;
		String toCompare = "://" + this.host;

		for (Element link : links) {
			tempLink = link.absUrl("href").toLowerCase();

			if (tempLink.length() > 140)
				continue;
			else if (tempLink.contains("#"))
				tempLink = tempLink.substring(0, tempLink.indexOf("#"));

			if (tempLink.contains(toCompare) && this.srr.isAllowed(tempLink))
				selectHeuristic(tempLink);
		}
	}

	private void selectHeuristic(String url) {
		if (this.domain == "steampowered") {
			steamHeuristic(url);
		} else {
			String[] words = { "" };
			boolean gD = (this.domain == "americanas" || this.domain == "fnac" || this.domain == "livrariacultura"
					|| this.domain == "magazineluiza" || this.domain == "nagem" || this.domain == "submarino");

			if (url.contains("br/"))
				words = url.substring(url.indexOf("br/")).split("[/-]");

			standardHeuristic(url, words, gD);
		}
	}

	
	private void steamHeuristic(String url) {
		if (url.contains("?l="))
			return;

		if (url.contains(".com/app") && !url.contains("agecheck"))
			this.linksToVisit.add(0, url);
		else
			this.linksToVisit.add(url);
	}

	private void standardHeuristic(String url, String[] words, boolean gD) {
		boolean bT = badTerms.contains(words), gT = goodTerms.contains(words);

		if (bT && gT)
			this.linksToVisit.add(url);
		else if (!bT && gT && !gD)
			this.linksToVisit.add(0, url);
		else if (!bT && gT && gD) {
			if (url.contains("/produto/") || url.contains("/p/") || url.endsWith("/p") || url.contains("/eletronicos/"))
				this.linksToVisit.add(0, url);
			else
				this.linksToVisit.add(url);
		} else
			this.trashToVisit.add(url);
	}
}

class Dictionary {
	List<String> words;

	public Dictionary() {
		this.words = new LinkedList<String>();
	}

	/***
	 * This method will add a String s to the dictionary.
	 * 
	 * @param s
	 *            the String to be added to the dictionary
	 */
	public void addWord(String s) {
		words.add(s);
	}

	/***
	 * This method will check if the dictionary contains any of the words
	 * received.
	 * 
	 * @param toBeChecked
	 *            list with the words to be checked
	 * @return TRUE if the dictionary contains any words received; FALSE
	 *         otherwise
	 */
	public boolean contains(String[] toBeChecked) {
		for (String word : words) {
			for (String check : toBeChecked) {
				if (word.equals(check))
					return true;
			}
		}

		return false;
	}
}