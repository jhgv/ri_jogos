package wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import org.apache.commons.compress.utils.Charsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Wrapper {
	public static String ARTIFACT_PATH = "..\\RI_Project\\documentos";
	
	public static void main(String[] args){
		String[] doms = new String[2];
		doms[0] = "americanas";
		doms[1] = "gamestop";
		Wrapper w = new Wrapper();
		w.Start(doms,"bfs");
		
	}
	
	public void Start(String [] dominios, String crawlerMethod){

		String actDomain = "";
		AlphanumComparator ac = new AlphanumComparator();

		try {
			for (int i = 0; i < dominios.length; i++) {
				String path = ARTIFACT_PATH + "\\" +  crawlerMethod + "\\"+ dominios[i] + "\\positives\\";
				File inputFile = new File(path + "posLinks.txt");

				String productInfoPath = ARTIFACT_PATH + "\\" + "output_wrapper\\" + dominios[i] + "\\";

				if(!new File(productInfoPath).exists()){
					new File(productInfoPath).mkdirs();
				}

				ArrayList<String> fileNames = new ArrayList<String>(Arrays.asList(new File(path).list()));

				//fileNames.sort(ac);	
				BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), Charsets.UTF_8));

				for (String name : fileNames) {
					actDomain = input.readLine();
					if(actDomain != null){
				
						//String s = input.readLine();

						File outputFile = new File(productInfoPath + "\\" + name +".txt");
						if (!outputFile.exists()) {
							outputFile.createNewFile();
							outputFile.mkdirs();
						}
						PrintWriter pw = new PrintWriter(outputFile);

						//if(s != null){
//							System.out.println(fileName);
							pegarDados(pw, path + name, actDomain);
							pw.println();
						//} 
						pw.close();

					}
				}
				System.out.println("Wrapper para " + dominios[i] + " terminado.");
				input.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void pegarDados(PrintWriter pw, String path, String actDomain) throws IOException {
		File file = new File(path);
		Document doc = Jsoup.parse(file, "UTF-8");
		
		PageInfo info = new PageInfo();
		
		if (actDomain.contains("www.saraiva.com.br")) {
			info = getInfoSaraiva(doc);
		} else if (actDomain.contains("www.walmart.com.br")) {
			info = getInfoWallmart(doc);
		} else if (actDomain.contains("www.magazineluiza.com.br")) {
			info = getInfoMagazine(doc);
		} else if (actDomain.contains("www.americanas.com.br")) {
			info = getInfoAmericanas(doc);
		} else if (actDomain.contains("www.livrariacultura.com.br")) {
			info = getInfoCultura(doc); 
		} else if (actDomain.contains("www.submarino.com.br")) {
			info = getInfoSubmarino(doc);
		} else if (actDomain.contains("www.gamestop.com")) {
			info = getInfoGameStop(doc);
		} else if (actDomain.contains("store.steampowered.com")) {
			info = getInfoSteam(doc);
		} else if (actDomain.contains("www.fastgames.com.br")) {
			info = getInfoFastGames(doc);
		} else if (actDomain.contains("store.playstation.com")) {
			info = getInfoPlayStation(doc);
		}

		String titulo = info.getTitulo();
		if(!(titulo.equalsIgnoreCase("Sem título") || titulo.equalsIgnoreCase(""))){
			String preco = info.getPreco();
			String outrosDados = info.getDados();
			synchronized (pw) {
				//pw.println("URL: " + site);
				pw.println("Titulo: " + titulo);
				pw.println("Preco: " + preco);
				pw.println(outrosDados);
				pw.println();
				pw.flush();
			}
		}
		synchronized (pw) {
			pw.println();
			pw.flush();
		}
	}
	
	public PageInfo getInfoAmericanas(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = "";
		String preco = "";
		String dados ="";
		Elements elem = (doc.getElementsByAttributeValue("class", "product-name"));
		
		//Recupera o Titulo
		String fullTitle = "";
		if (elem != null && !elem.toString().equals("")){
			fullTitle = elem.text();
			if(fullTitle.contains("Game")){
				fullTitle = fullTitle.replace("Game", "");
			}
			String[] aux = fullTitle.split("-");
			if(aux[0].equals(" ")){
				
				titulo = aux[1];
			}else{
				titulo = aux[0];
			}
			
			
		} else{
			titulo = "Sem título";
		}
		
		//Recupera o Preco
		elem = doc.getElementsByAttributeValue("class", "sales-price");

		if (elem != null){
			preco = elem.text();
		}else{
			preco = "Sem preço";
		}
		
		//Recupera os Dados
		elem = doc.getElementsByAttributeValue("class", "table table-striped");
		ArrayList<String[]> desc = new ArrayList<String[]>();
		StringBuffer sBuffer = new StringBuffer("");
		
		if (elem != null) {
			for (Element e : elem) {
				String texto[] = e.toString().toLowerCase().split("</tr>");
				
				// Formatação da saída dos dados
				for (int i = 0; i < texto.length - 1; i++) {
					desc.add(texto[i].split("</td>"));
					String[] aux = desc.get(i)[0].split(">");
					String[] aux2 = desc.get(i)[1].split(">");
					
					desc.get(i)[0] = aux[aux.length-1];
					desc.get(i)[1] = aux2[aux2.length-1];

					if ((desc.get(i)[0].equals("gênero")) || (desc.get(i)[0].equals("classificação indicativa"))
							|| (desc.get(i)[0].equals("desenvolvedor")) || (desc.get(i)[0].equals("áudio"))
							|| (desc.get(i)[0].equals("idiomas") || (desc.get(i)[0].equals("plataforma")))) {

						sBuffer.append(desc.get(i)[0] + ": " + desc.get(i)[1] + " \r\n");
					} else if (desc.get(i)[0].equalsIgnoreCase("Faixa Etária")) {
						sBuffer.append("Faixa Etária: " + desc.get(i)[1] + "\r\n");
					}
				}
			}

			dados = sBuffer.toString();
		} else {
			dados = "";
		}
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		
		return pi;
	}
	
	public PageInfo getInfoSteam(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = "";
		String preco = "";
		String dados ="";
		Elements elem = doc.getElementsByAttributeValue("itemprop", "name");
		
		//Recupera Titulo
		if (elem != null){
			titulo = elem.text();
		}else{
			titulo = "Sem título";
		}
		
		//Recupera Preco
		elem = doc.getElementsByAttributeValue("class", "game_purchase_price price");
		if(elem == null || elem.text().equals("")){
			elem = doc.getElementsByAttributeValue("class", "discount_final_price");
		}

		if (elem != null){
			if(elem.text().length() >=10) {
				preco = elem.text().substring(0, elem.text().indexOf("R$"));
			} else {
				preco = elem.text();
			}
		} else {
			preco = "Sem preço";
		}
		
		//Recupera Dados
		elem = doc.getElementsByAttributeValue("class", "details_block");
		StringBuffer sb = new StringBuffer("");

		if (elem != null) {
			for (Element e : elem) {
				String[] show = e.text().toLowerCase().split(": ");
				for (int i = 0; i < show.length; i++) {
					String saida = "";
					if (show[i].contains("nero")) {
						saida = "Gênero: " +show[i + 1];
						if(saida.contains("desenvolvedor")){
							saida = saida.replace("desenvolvedor", "");
							sb.append(saida + "\r\n");
						}else{
							sb.append(saida + "\r\n");
						}
					} else if (show[i].contains("desenvolvedor")) {
						saida = "Desenvolvedor: " + show[i + 1];
						if(saida.contains("distribuidora")){
							saida = saida.replace("distribuidora", "");
							sb.append(saida + "\r\n");
						}else{
							sb.append(saida + "\r\n");
						}
					}
				}
			}

			dados = sb.toString();
		} else{
			dados = "";
		}
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		return pi;
	}
	
	public PageInfo getInfoSaraiva(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = doc.title();
		String preco = "";
		String dados ="";
		
		//Recupera Titulo
		if (titulo.equals("")) {
			titulo = "Sem Título";
		}
		
		//Recupera Preco
		Elements elem = doc.getElementsByAttributeValue("class", "special-price");
		if (elem != null && elem.size() > 0) {
			preco = elem.toString().substring(elem.toString().indexOf("R$"),
					elem.toString().indexOf("</strong>"));
		} else {

			preco = "Sem preço";
		}
		
		//Recupera Dados
		elem = doc.getElementsByAttributeValue("itemprop", "title");
		String genero = elem.text();
		StringBuffer sb = new StringBuffer("");
		if(genero.length() > 0){
			sb.append("Gênero: " + genero.substring(genero.indexOf("Jogos")) + "\r\n");
		}
		ArrayList<Object> desc = new ArrayList<Object>();
		ArrayList<Object> carc = new ArrayList<Object>();
		ArrayList<Box> fim = new ArrayList<Box>();

		// Pegar os elementos da tabela TH
		for (Element th : doc.getElementsByAttributeValue("class", "label")) {
			desc.add(th.text());
		}

		// Pegar os elementos da tabela TD
		for (Element td : doc.getElementsByAttributeValue("class", "data")) {
			carc.add(td.text());
		}

		if (desc.size() > 0) {
			int i = 0;
			while (i < desc.size() && i < carc.size()) {
				Box box = new Box(desc.get(i), carc.get(i));
				i++;
				fim.add(box);
			}
			i = 0;
			while (i < fim.size()) {
				if (fim.get(i).desc.toString().equalsIgnoreCase("Marca")
						|| fim.get(i).desc.toString().equalsIgnoreCase("Classificação Indicativa")
						|| fim.get(i).desc.toString().equalsIgnoreCase("Idioma do Áudio")) {
					sb.append((fim.get(i).desc.toString() + ": " + fim.get(i).carac.toString()) + "\r\n");
				}
				i++;
			}

			dados = sb.toString();
		} else {
			dados = "";
		}
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		return pi;
	}
	
	public PageInfo getInfoCultura(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = "";
		String preco = "";
		String dados ="";
		Elements elem = doc.getElementsByAttributeValue("itemprop", "name");
		
		//Recupera Titulo
		if (elem != null) {
			titulo = elem.text();
		} else {
			titulo = "Sem título";
		}
		
		//Recupera Preco
		elem = doc.getElementsByAttributeValue("class", "price");
		if (elem != null) {
			if (elem.text().length() > 40) {
				preco = elem.text().substring(elem.text().indexOf("R$"), elem.text().indexOf("pr"));				
			} else if (elem.text().length() >= 25 && elem.text().length() <= 40 || elem.text().contains(" R$ ")) {
				preco = elem.text().substring(elem.text().indexOf("R$"), elem.text().indexOf(" R$ "));
			} else if(elem.text().length() >= 11 && elem.text().length() <= 24) {
				preco = elem.text().substring(elem.text().indexOf("R$"), elem.text().indexOf("pr"));
			} else {
				preco = elem.text();
			}
		} else{
			preco = "Sem preço";
		}
			
		//Recupera Dados
		elem = doc.getElementsByAttributeValue("id", "product-list-detail");
		ArrayList<String[]> desc = new ArrayList<String[]>();
		StringBuffer sBuffer = new StringBuffer("");

		if (elem != null) {
			for (Element e : elem) {
				String texto[] = e.toString().toLowerCase().split("</li>");

				// Formatação da saída dos dados
				for (int i = 0; i < texto.length - 1; i++) {
					desc.add(texto[i].split("</b>"));

					if (desc.get(i)[0].length() > 8) {
						desc.get(i)[0] = desc.get(i)[0].substring(desc.get(i)[0].indexOf("<b>"),
								desc.get(i)[0].indexOf(":"));
						desc.get(i)[0] = desc.get(i)[0].substring(3, desc.get(i)[0].length());

						if ((desc.get(i)[0].equals("gênero")) || (desc.get(i)[0].contains("classificação indicativa"))
								|| (desc.get(i)[0].equals("áudio")) || (desc.get(i)[0].equals("idiomas"))) {

							sBuffer.append(desc.get(i)[0] + ": " + desc.get(i)[1] + "\r\n");
						}
					}
				}
			}

			dados = sBuffer.toString();
		} else{
			dados = "";
		}
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		
		return pi;
	}
	
	public PageInfo getInfoFastGames(Document doc){
		PageInfo pi = new PageInfo();
		
		String titulo = "";
		String preco = "";
		String dados ="";
		
		//Recupera Titulo
		
		//Recupera Preco
		
		//Recupera Dados
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		
		return pi;
	}
	
	public PageInfo getInfoMagazine(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = "";
		String preco = "";
		String dados ="";
		
		//Recupera Titulo
		
		//Recupera Preco
		
		//Recupera Dados
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		
		return pi;
	}
	
	public PageInfo getInfoGameStop(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = "";
		String preco = "";
		String dados ="";
		Elements elem = (doc.getElementsByAttributeValue("class", "grid_17 ats-prod-title"));
		
		//Recupera Titulo
		if (elem != null && !elem.toString().equals("")){
			if(elem.text().contains("by")){
				String[] aux = elem.text().split(" by ");
				titulo = aux[0];
			}else{
				titulo = elem.text();
			}
		}else{
			titulo = "Sem Título";
		}
		//Recupera Preco
		
		//Recupera Dados
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		
		return pi;
	}
	
	public PageInfo getInfoPlayStation(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = "";
		String preco = "";
		String dados ="";
		
		//Recupera Titulo
		
		//Recupera Preco
		
		//Recupera Dados
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		return pi;
	}
	
	public PageInfo getInfoSubmarino(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = "";
		String preco = "";
		String dados ="";
		
		//Recupera Titulo
		
		//Recupera Preco
		
		//Recupera Dados
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		return pi;
	}
	
	public PageInfo getInfoWallmart(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = "";
		String preco = "";
		String dados ="";
		
		//Recupera Titulo
		
		//Recupera Preco
		
		//Recupera Dados
		
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		return pi;
	}
	
	
	
}

class Box {
	Object desc;
	Object carac;

	public Box(Object desc, Object carac) {
		this.desc = desc;
		this.carac = carac;
	}
}