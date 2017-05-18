/*
 * Universidade Federal de Pernambuco
 * Centro de Inform�tica (CIn)
 * Recupera��o de Informa��o
 * 
 * Ana Caroline Ferreira de Fran�a (acff)
 * Thiago Aquino Santos (tas4)
 * Victor Sin Yu Chen (vsyc)
 */

package crawler;

import java.io.IOException;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		long timeInMilis = System.currentTimeMillis();

		try {
			SpiderFactory sf = new SpiderFactory();
			// sf.startCrawlers("bfs");
			sf.startCrawlers();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(System.currentTimeMillis() - timeInMilis);
	}
}