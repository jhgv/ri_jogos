package classificador;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import weka.core.Instances;
import weka.core.converters.TextDirectoryLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class PreProcessBase {
	
	// Usado só no PreProcessamento da Base, na criação dos arquivos .arff 
	public static void main(String[] args) throws Exception {
		TextDirectoryLoader textD = createArff();
		System.out.println(textD);
	    Instances dataFiltered = getVector(textD);
	    
	    //create flat file
	    PrintWriter writer = new PrintWriter("src\\classificador\\Arffs\\Test0.arff", "UTF-8");
	    writer.println(dataFiltered);
	    writer.close();
	    
	}
	
	
	public static TextDirectoryLoader createArff() throws IOException{
		
		TextDirectoryLoader textD = new TextDirectoryLoader();
		File file = new File("src\\classificador\\Examples");
		textD.setDirectory(file);
		return textD;
	}
	
	// criando BagOfWords
	public static Instances getVector(TextDirectoryLoader textD) throws Exception{
		
		Instances dataReturn;
		Instances data = textD.getDataSet(); 
		
		 StringToWordVector filter = new StringToWordVector();
		 filter.setInputFormat(data);        
		 dataReturn = Filter.useFilter(data, filter);
		 
		 return dataReturn;
	}
	
}
