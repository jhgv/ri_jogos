package crawler;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SpiderBfs extends Spider {

	public SpiderBfs(String domain) {
		super(domain);
		// TODO Auto-generated constructor stub
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

			if (tempLink.contains(toCompare) && this.srr.isAllowed(tempLink))
				this.linksToVisit.add(tempLink);
		}
		
	}

}
