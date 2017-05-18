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
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffLoader.ArffReader;

public class Classificador{

	private Classifier classifier;
	private Instances instances;
	private String[] attributes;

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
		BufferedReader in = new BufferedReader(new FileReader("src\\classificador\\Arffs\\PosNegFS_att.arff"));
		ArffReader arff = new ArffReader(in);
		String at[] = new String[arff.getData().numAttributes()-1];
		for (int i = 0; i < at.length; i++) at[i]=arff.getData().attribute(i).name();

		// Finalizando a classifica��o
		//local do modelo de classificacao criado

		String localModelo = "src\\classificador\\Modelos\\J48.model" ;

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
	
	public static void main(String[] args) throws Exception {
		Classificador classificador = getClassificador();
		File file = new File("Pages");
		ArrayList<File> files = new ArrayList<File>(Arrays.asList((file).listFiles()));
		
		new File("Pages\\Positives").mkdir();
		
		int count = 0;
		PrintWriter w = new PrintWriter("Pages\\Positives\\PosPages.txt");
		for (File file2 : files) {
			if(file2.isFile()){
				
				//String dados = new String(Files.readAllBytes(file2.toPath()));
				
				String dados = PreProcesso.getStringPage(file2);;
				if(classificador.classify(dados)){
					w.write("P�gina " + file2.getName() + " � uma p�gina recomendada.\n");
				}
			}
			count++;
		}
		System.out.println("Classifica��o atualizada.");
		w.close();
	}

}