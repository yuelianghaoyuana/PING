package readers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataNumReaderTab implements IReader<float[]>{
	
	final String sep = " ";
	
	List<float[]> data;			// Liste des donnees codees sous forme de listes
	List<String> labels;	// Liste des labels associes
	
	BufferedReader br;		// Acces au flux du fichier
	int count;				// Compteur du nombre de lignes
	String line;			// Derniere ligne parcourue
	
	String filename;
		
	public void close() {
		try{
			if (br != null) br.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public List<String> getLabels() {
		return this.labels;
	}

	public void next(int n) {
		this.data.clear();
		this.labels.clear();
		try {
			int nb = 0; // Nombre de lignes lues
			boolean fini = false;
			while (nb < n && !fini){
				line = this.br.readLine();
				if (line !=null){
					traitementLigne(line, sep);
					count ++;
					nb++;
				} else {
					fini = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void open(String filename) {
		try{
			this.filename = filename;
			this.count = 0;
			this.data = new ArrayList<float[]>();
			this.labels = new ArrayList<String>();
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			while ((line = br.readLine())!=null){
	            count ++;
	        }

			br.close();
			// Remise du flux au debut
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

		} catch (Exception e) {e.printStackTrace();}
	}

	public List<float[]> read(String filename) {
		try{
			this.count = 0;
			this.data = new ArrayList<float[]>();
			this.labels = new ArrayList<String>();
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			while ((line = br.readLine())!=null){
				traitementLigne(line, sep);
	            count ++;
	        }
			return this.data;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	
	// Lecture d'une ligne d'un fichier "data"
	// Hypotheses :
	// - Les valeurs sont separees par le caractere sep
	// la derniere valeur est le label de la classe
	
	private void traitementLigne(String line, String sep){
		String[] tmp = line.split(sep);
		int i;
		float[] vect = new float[tmp.length - 1];
		for (i = 0; i < tmp.length - 1; i++){
			vect[i] = new Float(tmp[i]).floatValue();
		}
		
		this.data.add((float[]) vect);
		this.labels.add(tmp[tmp.length - 1]);
	}

	public List<float[]> getDataSet() {
		return this.data;
	}

	public int count() {
		return this.data.size();
		// return this.count;
	}

	public void start() {
		if (br != null){
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(this.filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.count = 0;
	}

	// Remise du flux au debut
	public void reset() {
		try {
			if (br != null) {
				br.close();
			}
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int size() {
		return this.count;
	}
}
