package classificador;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import weka.core.Instances;
import weka.core.converters.TextDirectoryLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class PreProcessBase {
	
	// Usado s� no PreProcessamento da Base, na cria��o dos arquivos .arff 
	public static void main(String[] args) throws Exception {
		TextDirectoryLoader textD = createArff();
		System.out.println(textD);
	    Instances dataFiltered = getVector(textD);
	  
	    //criando arquivo .arff com conteudo
	    PrintWriter writer = new PrintWriter("src/classificador/Arffs/PosNeg.arff", "UTF-8");
	    writer.println(dataFiltered);
	    writer.close();
	    
	}
	
	
	public static TextDirectoryLoader createArff() throws IOException{
		
		TextDirectoryLoader textD = new TextDirectoryLoader();
		File file = new File("src/classificador/Examples");
		textD.setDirectory(file);
		return textD;
	}
	
	// criando BagOfWords - Instances
	public static Instances getVector(TextDirectoryLoader textD) throws Exception{
		
		Instances dataReturn;
		Instances data = textD.getDataSet(); 
		
		 StringToWordVector filter = new StringToWordVector();
		 filter.setInputFormat(data);        
		 dataReturn = Filter.useFilter(data, filter);
		 
		 return dataReturn;
	}
}
