package classificador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.FileUtils;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffLoader.ArffReader;

public class Classificador{

	private Classifier classifier;
	private Instances instances;
	private String[] attributes;
	private static String domain[] = { "americanas", "fastgames", "magazineluiza",
			"saraiva", "livrariacultura", "gamestop", "submarino", "walmart" , "store.playstation", "steampowered"};

	public Classificador(Classifier classifier, Instances instances, String[] attributes){
		this.classifier = classifier;
		this.instances = instances;
		this.attributes = attributes;
	}

	/* Metodo retorna se uma pagina pertence `a classe positiva
	 */
	public boolean classify(String page) throws Exception{
		boolean relevant = false;
		double[] values = getValues(page);
		//weka.core.Instance instanceWeka = new weka.core.Instance(1, values);
		weka.core.Instance instanceWeka = new SparseInstance(1, values);
		instanceWeka.setDataset(instances);
		double classificationResult = classifier.classifyInstance(instanceWeka);
		if (classificationResult == 0) {
			relevant = true;
		}
		else {
			relevant = false;
		}
		return relevant;
	}

	/* Metodo retorna as probabilidades da pagina pertencer `as classes
       positiva e negativa
	 */
	public double[] distributionForInstance(String page) throws Exception{
		double[] result = null;
		double[] values = getValues(page);
		weka.core.Instance instanceWeka = new SparseInstance(1, values);
		instanceWeka.setDataset(instances);
		result = classifier.distributionForInstance(instanceWeka);
		return result;
	}

	private double[] getValues(String pagina) {

		int countAtt = this.attributes.length - 1;
		double[] values = new double[countAtt];

		//Implementar a extracao da pagina dos termos usados como features pelo classificador e criar um vetor de double com a frequencia desses termos na pagina
		ArrayList<String> listTokens = new ArrayList<String>();
		String tokens[] = pagina.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			listTokens.add(tokens[i]);
		}

		// Descobrindo a quantidade de vezes do elemento
		Map<String,Integer> map = new HashMap<>();
		for (String s : listTokens) {
			Integer n = map.get(s);
			if(n == null){
				n = 1;
			}else{
				n++;
			}
			map.put(s, n);
		}

		for (int i = 0; i < countAtt; i++) {
			if (map.containsKey(this.attributes[i])) {
				values[i] = map.get(this.attributes[i]);
			} else {
				values[i] = 0;
			}
		}
		return values;
	}	

	public static Classificador getClassificador() throws IOException, ClassNotFoundException{

		// Gerando array de string para os atributos do .arff 

		Classificador classify = null;
		BufferedReader in = new BufferedReader(new FileReader("src/classificador/Arffs/PosNegFS_att.arff"));
		ArffReader arff = new ArffReader(in);
		String at[] = new String[arff.getData().numAttributes()-1];
		for (int i = 0; i < at.length; i++) at[i]=arff.getData().attribute(i).name();

		// Finalizando a classifica��o
		//local do modelo de classificacao criado

		String localModelo = "src/classificador/Modelos/J48.model" ;

		//features do classificador
		String[] attributes = at;
		InputStream is = new FileInputStream(localModelo);
		ObjectInputStream objectInputStream = new ObjectInputStream(is);
		Classifier classifier = (Classifier) objectInputStream.readObject();

		weka.core.FastVector vectorAtt = new weka.core.FastVector();
		for (int i = 0; i < attributes.length; i++) {
			vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
		}
		String[] classValues =  { "Positives", "Negatives" };
		weka.core.FastVector classAtt = new weka.core.FastVector();
		for (int i = 0; i < classValues.length; i++) {
			classAtt.addElement(classValues[i]);
		}
		vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
		Instances insts = new Instances("classification", vectorAtt, 1);
		insts.setClassIndex(attributes.length);
		Classificador classificador = new Classificador(classifier, insts, attributes);

		return classificador;

	}

	public static void classificador() throws Exception {
		Classificador classificador = getClassificador();

		int countDomain = -1;

		while(countDomain++ < domain.length - 1){
			
			File file = new File("documentos/heuristica/" + domain[countDomain] );
			System.out.println(file.toURL().toString());
			ArrayList<File> files = new ArrayList<File>(Arrays.asList((file).listFiles()));
			File fileLinks = new File("documentos/heuristica/" + domain[countDomain]+ "/links_visitados.txt" );
			ArrayList<Integer> links_Ids = new ArrayList<>();

			new File(file.getPath()+ "/positives/").mkdir();

			int count = 0;
			
			
			for (File file2 : files) {
				if(file2.isFile() && !(file2.getName().equals("links_visitados.txt"))){
					//String dados = new String(Files.readAllBytes(file2.toPath()));
					String page = PreProcesso.getStringPage(file2);
					
					if (classificador.classify(page)) {
						File fileResult = new File(file.getPath() + "/Positives/" + file2.getName());
						setFile(setTextToHtml(file2), fileResult);
						links_Ids.add(count);
					}
					count++;
				}
				
			}
			
			getPos_Links(file.getPath() + "/Positives/posLinks.txt", fileLinks, links_Ids);
		}
	}
	
	public static void getPos_Links(String pathNewFile, File linkFiles, ArrayList<Integer> lines) {
		File urls = new File(pathNewFile);
		try {
			PrintWriter printWriter = new PrintWriter(urls);
			for (Integer line : lines) {
				String lineFile = (String) FileUtils.readLines(linkFiles).get(line);
				printWriter.println(lineFile);
			}
			printWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void setFile(String text, File file) {
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(text);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String setTextToHtml(File file) throws IOException{
		int len;
		char[] chr = new char[4096];
		final StringBuffer buffer = new StringBuffer();
		final FileReader reader = new FileReader(file);
		try {
			while ((len = reader.read(chr)) > 0) {
				buffer.append(chr, 0, len);
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
	}

}