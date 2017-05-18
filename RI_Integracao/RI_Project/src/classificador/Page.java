package classificador;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Page {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		URL url = new URL("http://www.gamestop.com/xbox-one/games/injustice-2-ultimate-edition/141806");
		PreProcesso.getHtml(url);
	}

}
