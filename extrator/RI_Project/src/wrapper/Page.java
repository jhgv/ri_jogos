package wrapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.compress.utils.Charsets;

public class Page {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		URL url = new URL("http://www.livrariacultura.com.br/p/games/consoles/mega-drive-46536114");
		getHtml(url);
	}
	
	public static void getHtml(URL url) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(),Charsets.UTF_8));

        PrintWriter writer = new PrintWriter("..\\RI_Project\\documentos\\bfs\\cultura\\positives\\doc1.html", "UTF-8");
		
        String content = "";
        try {
	        StringBuilder sb = new StringBuilder();
	        String line = in.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = in.readLine();
	        }
	        content = sb.toString();
	    } finally {
	        in.close();
	    }
        
        
        
        writer.println(content);
        writer.flush();
        writer.close();
	}
}