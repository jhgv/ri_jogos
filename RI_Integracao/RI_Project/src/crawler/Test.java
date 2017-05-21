
package crawler;

import java.io.IOException;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		long timeInMilis = System.currentTimeMillis();

		try {
			SpiderFactory sf = new SpiderFactory();
			 sf.startCrawlers("bfs");
//			sf.startCrawlers();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(System.currentTimeMillis() - timeInMilis);
	}
}