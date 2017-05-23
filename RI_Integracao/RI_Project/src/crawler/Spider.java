package crawler;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import crawler.crawlercommons.BaseRobotRules;
import crawler.crawlercommons.BaseRobotsParser;
import crawler.crawlercommons.SimpleRobotRules;
import crawler.crawlercommons.SimpleRobotRulesParser;

public class Spider implements Runnable {

	// Constants
	private static final String USER_AGENT = "Mozilla/5.0 AppleWebKit/537.36 Chrome/52.0.2743.82 Safari/537.36 OPR/39.0.2256.48";
	private static final int MAXIMUM_QUANTITY_PAGES = 1000;

	protected static final String NAGEM_CONSTANT = "/home/index.php";

	// Variables
	protected String domain;
	protected String host;
	protected List<String> linksToVisit;
	protected List<String> trashToVisit;
	protected Set<String> visitedLinks;
	protected Map<String, String> cookies;
	protected String type;

	private Dictionary goodTerms;
	private Dictionary badTerms;

	protected BaseRobotsParser srrp;
	protected BaseRobotRules srr;

	private PrintWriter pw;
	private StringBuffer filePath;
	private int fileIndex;

	// Constructor
	public Spider(String type, String domain) {
		this.domain = domain;
		this.host = discoverHost(domain);
		this.linksToVisit = new LinkedList<String>();
		this.trashToVisit = new LinkedList<String>();
		this.visitedLinks = new LinkedHashSet<String>();
		this.cookies = new HashMap<String, String>();
		this.type = type;

		this.srrp = new SimpleRobotRulesParser();
		this.srr = new SimpleRobotRules();

		this.filePath = discoverFilePath(domain);
		this.fileIndex = 0;
	}

	private String discoverHost(String domain) {
		StringBuffer url = new StringBuffer();

		if (domain == "steampowered")
			url.append("store.steampowered.com");
		else if (domain == "gamestop")
			url.append("www.gamestop.com");
		else if (domain == "store.playstation") {
			url.append("store.playstation.com");
		} else {
			url.append("www.");
			url.append(domain);
			url.append(".com.br");
		}

		return url.toString();
	}

	private StringBuffer discoverFilePath(String domain) {
		StringBuffer path = new StringBuffer(SpiderFactory.DOCUMENTOS_PATH + this.type + "/").append(domain);
		File f = new File(path.toString());

		if (!f.exists())
			f.mkdirs();

		return path;
	}

	@Override
	public void run() {
		String http = (this.domain == "walmart" || this.domain == "store.playstation") ? "https://" : "http://";
		String url = http + this.host;
		try {
			// Request the robots.txt
			byte[] robotsTxt = requestRobotsTxt(url);

			// Parse the robots.txt
			this.srr = this.srrp.parseContent(url, robotsTxt, "text/html", USER_AGENT);

			// Search the domain for pages
			if (this.domain == "nagem")
				url += NAGEM_CONSTANT;

			searchPages(url, 3000, 5000);

			// Save the links of the visited pages
			saveVisitedLinks();
		} catch (Exception e) {
			String message = new StringBuffer("Crawler for <<").append(url).append(">> could not run successfully")
					.toString();
			System.out.println(message);
			SpiderFactory.error = true;

			synchronized (this.pw) {
				this.pw.println(new StringBuffer(new Date().toString()).append(": ").append(message).append("."));
			}
		}
	}

	private byte[] requestRobotsTxt(String url) throws MalformedURLException, IOException {
		StringBuffer path = new StringBuffer("documentos/robots/");
		// String path = "documentos/robots/";
		File f = new File(path.toString());

		if (!f.exists())
			f.mkdir();

		InputStream is;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		f = new File(new StringBuffer(path).append(this.domain).append(".txt").toString());
		boolean fileExists = false;

		if (f.isFile()) {
			is = new FileInputStream(f);
			fileExists = true;
		} else
			is = new URL(url + "/robots.txt").openStream();

		byte[] buffer = new byte[1024];
		int length;

		while ((length = is.read(buffer)) != -1)
			baos.write(buffer, 0, length);

		if (!fileExists)
			baos.writeTo(new FileOutputStream(f));

		return baos.toByteArray();
	}

	private void searchPages(String url, int incTimeOut, int incTimeSleep) throws InterruptedException {
		this.linksToVisit.add(url);
		String nextUrl = nextUrl();

		// flux control variables
		boolean pause = false;
		int timeOut = 8000, maxTimeOut = 50000;
		int timeSleep = incTimeSleep << 1, maxTimeSleep = 600000;
		int count = 0;

		System.out.println("Starting crawler......");

		while (this.visitedLinks.size() < MAXIMUM_QUANTITY_PAGES && nextUrl != null) {
			if (pause) {
				Thread.sleep(timeSleep);
				pause = false;
			}

			try {
				System.out.println("---- Crawling ---- " + nextUrl);
				crawl(nextUrl, timeOut);
				this.visitedLinks.add(nextUrl);
				System.out.println("OK");
			} catch (IOException e) {
				System.out.println(e.getMessage() + " for the URL " + nextUrl);

				if (count++ % 5 != 4)
					continue;
			} catch (Exception e) {
				System.out.println(e.getMessage() + " for the URL " + nextUrl);
				pause = true;

				if (timeOut < maxTimeOut)
					timeOut += incTimeOut;

				if (timeSleep < maxTimeSleep)
					timeSleep += incTimeSleep;

				if (count++ % 5 != 4)
					continue;
			} finally {
				nextUrl = nextUrl();
			}
		}
	}

	
	private String nextUrl() {
		String next;

		do {
			if (this.linksToVisit.isEmpty()) {
				if (this.getClass() == Spider.class && !this.trashToVisit.isEmpty())
					next = this.trashToVisit.remove(0);
				else
					return null;
			} else {
				next = this.linksToVisit.remove(0);
			}
		} while (this.visitedLinks.contains(next));

		return next;
	}

	public void crawl(String url, int timeout) throws IOException, InterruptedException {
		if(this.type == "heuristica"){
			createDictionaries();
			crawlHeuristic(url, timeout);
		}else{
			crawlBfs(url, timeout);
		}
		
	}
	
	

	public void crawlHeuristic(String url, int timeout) throws IOException, InterruptedException {
		SpiderFactory.DOCUMENTOS_PATH += "/heuristica";
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
	
	public void crawlBfs(String url, int timeout) throws IOException, InterruptedException {
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

			if (tempLink.contains(toCompare) && this.srr.isAllowed(tempLink))
				this.linksToVisit.add(tempLink);
		}
	}

	protected Response connect(String url, int timeout) throws IOException {
		Connection c = Jsoup.connect(url);
		c.userAgent(USER_AGENT);
		c.header("Accept-Language", "pt-br");
		c.referrer("http://www.google.com");
		c.cookies(this.cookies);
		c.timeout(timeout);
		c.ignoreHttpErrors(true);

		return c.execute();
	}

	protected void saveHtml(String doc) throws IOException {
		StringBuffer path = new StringBuffer(this.filePath.toString());
		path.append("/doc" + (this.fileIndex++) + ".html"); // Para ficar docXX.html
		FileWriter fwDoc = new FileWriter(path.toString());
		fwDoc.write(doc);
		fwDoc.close();
	}

	private void saveVisitedLinks() throws IOException {
		StringBuffer path = new StringBuffer(this.filePath.toString());
		path.append("/links_visitados.txt");

		FileWriter fwLinks = new FileWriter(path.toString());
		Iterator<String> itr = this.visitedLinks.iterator();

		while (itr.hasNext()) {
			fwLinks.write(itr.next());
			fwLinks.write("\r\n");
		}

		fwLinks.close();
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
