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
		if(!(titulo.equalsIgnoreCase("Sem titulo") || titulo.equalsIgnoreCase(""))){
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
		if (elem != null && !elem.toString().equals("") && !elem.text().contains("Console")){
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
			titulo = "Sem titulo";
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
		Elements elem = doc.getElementsByAttributeValue("class", "details_block");
		String[] aux2 = new String[4];
		
		//Recupera Titulo
		if (elem != null && !elem.text().equals("")){
			String[] aux = elem.text().split("Release");
			aux[0] = aux[0].replace("Title: ", "");
			aux[0] = aux[0].replace("Genre:", "");
			aux[0] = aux[0].replace("Developer:", "");
			aux[0] = aux[0].replace("Publisher:", "");
	
			aux2 = aux[0].split("  ");
			
			titulo = aux2[0];
			
		}else{
			titulo = "Sem titulo";
		}
		
		//Recupera Preco
		elem = doc.getElementsByAttributeValue("class", "game_purchase_price price");
		if(elem == null || elem.text().equals("")){
			elem = doc.getElementsByAttributeValue("class", "discount_final_price");
		}
		
		if (elem != null){
			if(elem.text().length() >=10) {
				String []aux = elem.text().split(" R");
				preco = aux[0];
			} else {
				preco = elem.text();
			}
		}
		
		//Recupera Dados
		
		StringBuffer sb = new StringBuffer("");
		elem = doc.getElementsByAttributeValue("class", "details_block");
		
		if (elem != null) {
			sb.append("Gênero: "+aux2[1]+" \r\n");
			sb.append("Desenvolvedor: "+aux2[2]+" \r\n");
			sb.append("Distribuidora: "+aux2[3]+" \r\n");

			dados = sb.toString();
		} else{
			dados = "Sem Dados";
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
		if (titulo.equals("") || titulo.contains("Console")) {
			titulo = "Sem Titulo";
		}else{
			String[] aux = titulo.split("-");
			titulo = aux[0];
		}
		
		if(titulo.contains("Sem T")){
			pi.setPreco("Sem Preco");
			pi.setTitulo(titulo);
			pi.setDados("Sem dados");
		}else{
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
		}
		
		
		return pi;
	}
	
	public PageInfo getInfoCultura(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = doc.title();
		String preco = "";
		String dados ="";
		Elements elem = doc.getElementsByAttributeValue("class", "ribbons");
		
		//Recupera Titulo
		if (elem != null && elem.text().contains("onsole")) {
			titulo = "Sem titulo";
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
								|| (desc.get(i)[0].equals("áudio")) || (desc.get(i)[0].equals("idiomas"))|| (desc.get(i)[0].equals("plataforma"))) {

							sBuffer.append(desc.get(i)[0] + ": " + desc.get(i)[1] + "\r\n");
						}else if((desc.get(i)[0].equals("desenvolvedor"))){
							String[] aux = desc.get(i)[1].split(">");
							desc.get(i)[1] = aux[1].split("<")[0];
							
							sBuffer.append(desc.get(i)[0] + ": " + desc.get(i)[1] + "\r\n");
						}else if((desc.get(i)[0].equals("categoria"))){
							String[] aux= desc.get(i)[1].split("</a>");
							desc.get(i)[1] = aux[aux.length-1].split(">")[1];
							
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
		String dados = "";
		
		Elements elem = doc.getElementsByAttributeValue("itemprop", "name");
		if (elem != null) {
			if(elem.text().contains("-")){
				titulo = elem.text().split("-")[0];
				System.out.println("aqui");
			} else {
				titulo = elem.text();
				
			}
		} else {
			titulo = "Sem título";
		}
		//Recupera Preco
		elem = doc.getElementsByAttributeValue("class", "preco-avista precoAvista");
		if (elem != null && !elem.toString().equals("")) {
			if(elem.text().length() < 10) {
				preco = elem.text();
			} else {
				preco = elem.text().split(" R")[0];
			}
		} else{
			preco = "Sem preço";
		}

		//Recupera Dados
		
		elem = doc.getElementsByAttributeValue("cellpadding", "0");
		//System.out.println(elem.toString());
		StringBuffer sb = new StringBuffer("");
		ArrayList<String[]> desc = new ArrayList<String[]>();
		
		if (elem != null && elem.text().contains("Gênero")) {
			
			String[] aux = elem.toString().split("</table>");
			for (int i = 0; i < aux.length; i++) {
				desc.add(aux[i].split("</strong>"));
				
				if(!desc.get(i)[0].endsWith("font>")){
					String[] aux2 = desc.get(i)[0].split(">");
					desc.get(i)[0] = aux2[aux2.length-1];
				}else{
					desc.get(i)[0] = desc.get(i)[0].replace("</font>", "");
					String[] aux3 = desc.get(i)[0].split(">");
					desc.get(i)[0] = aux3[aux3.length-1];
				}
				
				if(!desc.get(i)[1].endsWith("font>")){
					String[] aux2 = desc.get(i)[1].split(">");
					desc.get(i)[1] = aux2[aux2.length-1];
				}else{
					desc.get(i)[1] = desc.get(i)[1].replace("</font>", "");
					String[] aux3 = desc.get(i)[1].split(">");
					desc.get(i)[1] = aux3[aux3.length-1];
				}
				
				if(desc.get(i)[0].equalsIgnoreCase("gênero") || desc.get(i)[0].equalsIgnoreCase("quantidade de jogadores") 
						|| desc.get(i)[0].equalsIgnoreCase("idade recomendada")|| desc.get(i)[0].equalsIgnoreCase("marca")){
					sb.append(desc.get(i)[0]+": "+desc.get(i)[1]+"\r\n");
				}
			}
		} else{
			dados = "";
		}	
		pi.setPreco(preco);
		pi.setTitulo(titulo);
		pi.setDados(dados);
		
		return pi;
	}
	
	public PageInfo getInfoMagazine(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = doc.title();
		String preco = "";
		String dados ="";
		
		titulo = titulo.split(" - ")[0];
		
		if(titulo.contains("Console")){
			titulo = "Sem Titulo";
		}
		//Recupera Preco
		Elements elem = doc.getElementsByAttributeValue("class", "js-price-value");
		
		if (elem != null  ){
			if(!elem.text().contains("XXX")){
				preco = elem.text();
			}else{
				preco = "Sem preço";
			}
			
		}else{
			preco = "Sem preço";
		}
		//Recupera Dados
		boolean isConsole = false;
		elem = doc.getElementsByAttributeValue("class", "fs-row");
		StringBuffer sb = new StringBuffer("");

		if (elem != null) {
			for (Element e : elem) {
				if (e.text().contains("Informações")){
					sb.append(e.text().substring(e.text().indexOf("Marca"), e.text().indexOf("Referência")) + "\r\n");
				}
				if (e.text().contains("Gênero") || e.text().contains("Idioma")
						|| e.text().contains("Idade recomendada")){
					sb.append(e.text() + "\r\n");
				}
				if(e.text().contains("Console") ){
					isConsole = true;
				}
			}

			dados = sb.toString();
		} else{
			dados = "Sem Dados";
		}
		
		if(!isConsole){
			pi.setPreco(preco);
			pi.setTitulo(titulo);
			pi.setDados(dados);
		}else{
			pi.setTitulo("Sem titulo");
		}
		
		
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
			titulo = "Sem Titulo";
		}
		//Recupera Preco
		elem = (doc.getElementsByAttributeValue("class", "ats-prodBuy-price"));
		
		if(elem != null && !elem.toString().equals("")){
			String[] aux = elem.text().split(" ");
			preco = aux[0];
		}else{
			preco = "Sem Preco";
		}
		
		
		//Recupera Dados
		boolean isConsole = false;
		elem = (doc.getElementsByAttributeValue("class", "gameinfo nograd grid_15 ats-prodRating-gameInfo"));
		
		ArrayList<String[]> desc = new ArrayList<String[]>();
		StringBuffer sBuffer = new StringBuffer("");
		
		if (elem != null) {
			for (Element e : elem) {
				String texto[] = e.toString().toLowerCase().split("</li>");
				
				// Formatação da saída dos dados
				for (int i = 0; i < texto.length - 1; i++) {
					desc.add(texto[i].split("<span>"));
					String[] aux = desc.get(i)[0].split(">");
					String[] aux2 = desc.get(i)[1].split("<");
					
					desc.get(i)[0] = aux[aux.length-1];
					desc.get(i)[1] = aux2[0];
					
					if (desc.get(i)[0].equals(" developer: ") || desc.get(i)[0].equals(" category: ")
							|| desc.get(i)[0].equals(" publisher: ") || desc.get(i)[0].equals(" platform: ")) {
						desc.get(i)[0] = desc.get(i)[0].replace(" ", "");
						sBuffer.append(desc.get(i)[0] +" "+ desc.get(i)[1] + " \r\n");
						if(desc.get(i)[1].equalsIgnoreCase("Systems")){
							
							isConsole = true;
						}
					} 
				}
			}

			dados = sBuffer.toString();
		} else {
			dados = "Sem dados";
		}
       
		if(!isConsole){
			pi.setPreco(preco);
			pi.setTitulo(titulo);
			pi.setDados(dados);
		}else{
			pi.setTitulo("Sem titulo");
			pi.setPreco("Sem preco");
			pi.setDados("Sem dados");
		}
		
		
		
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
		Elements elem = doc.getElementsByAttributeValue("class", "mp-tit-name prodTitle");
		if (elem != null && elem.text().contains("Game")) {
			titulo = elem.text().substring(elem.text().indexOf("Game"), elem.text().length());
		} else {
			titulo = "Sem título";
		}
		
		//Recupera Preco
		elem = doc.getElementsByAttributeValue("itemprop", "price/salesPrice");
		if (elem != null) {
			preco = elem.text();
		} else {
			preco = "Sem preço";
		}
		//Recupera Dados
		elem = doc.getElementsByAttributeValue("class", "ficha-tecnica");
		ArrayList<String[]> desc = new ArrayList<String[]>();
		StringBuffer sb = new StringBuffer("");

		if (elem != null) {
			for (Element e : elem) {
				String texto[] = e.toString().toLowerCase().split("</tr>");

				// Formatação da saída dos dados
				for (int i = 0; i < texto.length - 1; i++) {
					desc.add(texto[i].split("</th>"));
					desc.get(i)[0] = desc.get(i)[0].replace("<tbody>", "");
					desc.get(i)[0] = desc.get(i)[0].replace("<tr>", "");
					desc.get(i)[0] = desc.get(i)[0].replace("<th>", "");
					desc.get(i)[0] = desc.get(i)[0].replaceAll("\n", "");
					desc.get(i)[0] = desc.get(i)[0].replaceAll(" ", "");
					desc.get(i)[1] = desc.get(i)[1].replaceAll(" ", "");
					desc.get(i)[1] = desc.get(i)[1].replaceAll("\n", "");
					desc.get(i)[1] = desc.get(i)[1].replace("<td>", "");
					desc.get(i)[1] = desc.get(i)[1].replace("</td>", "");

					if ((desc.get(i)[0].equalsIgnoreCase("Gênero")) || (desc.get(i)[0].equalsIgnoreCase("Classificação indicativa"))
							|| (desc.get(i)[0].equalsIgnoreCase("Desenvolvedor")) || (desc.get(i)[0].equalsIgnoreCase("Áudio"))
							|| (desc.get(i)[0].equalsIgnoreCase("idiomas"))) {

						sb.append(desc.get(i)[0] + ": " + desc.get(i)[1] + "\r\n");
					} else if (desc.get(i)[0].equalsIgnoreCase("FaixaEtária")) {
						sb.append("Faixa Etária: " + desc.get(i)[1] + "\r\n");
					}
				}
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
	
	public PageInfo getInfoWallmart(Document doc){
		PageInfo pi = new PageInfo();
		String titulo = "";
		String preco = "";
		String dados ="";
		
		//Recupera Titulo
		Elements elem = doc.getElementsByAttributeValue("class", "product-title");
		
		titulo= elem.text();
		//Recupera Preco
		Elements o = doc.getElementsByAttributeValue("class", "int");
		Elements p = doc.getElementsByAttributeValue("class", "dec");

		if (o != null) {
			if(o.text().contains(" ")){
				String inte = o.text().substring(0, o.text().indexOf(' '));
				String dec = p.text().substring(0, p.text().indexOf(' '));
				preco= (inte + dec);
			} else {
				preco = (o.text() + p.text());
			}
		} else {
			preco = "Sem preço";
		}
		
		//Recupera Dados
		StringBuffer sb = new StringBuffer("");
		Elements genero = doc.getElementsByAttributeValue("class", "value-field Genero");
		Elements marca = doc.getElementsByAttributeValue("itemprop", "brand");
		Elements faixa = doc.getElementsByAttributeValue("class", "value-field Faixa-Etaria");
		Elements idioma = doc.getElementsByAttributeValue("class", "value-field Idiomas-Audio");

		if (genero != null && ! genero.toString().equals("")) {
			sb.append("Gênero: " + genero.text().substring(0, genero.text().length()) + "\r\n");
		}

		if (marca != null && !marca.toString().equals("")) {
			sb.append("Marca: " + marca.text().substring(0, marca.text().length()) + "\r\n");
		}

		if (faixa != null && !faixa.toString().equals("")) {
			sb.append("Faixa: " + faixa.text().substring(0, faixa.text().length()) + "\r\n");
		}

		if (idioma != null && !idioma.toString().equals("")) {
			sb.append("Idioma: " + idioma.text().substring(0, idioma.text().length()) + "\r\n");
		}

		dados = sb.toString();
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