package readers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionReaderUrlList implements IReader<List<Integer>>{
	// Mode bavard ou non
	final boolean verbose = false;
	// Taille du jeu de donnees : nombre de sessions
	int count;
	// Nombre d'attributs : nombre d'attributs
    int natt;
    // Niveau de generalisation
    int generalisation = -1;
    // Tableau des donnees numeriques
    List<List<Integer>> data;             
    // Liste de toutes les URLS
    List<String> urls;     
    // Nom du fichier source
    String src;                 
    // Pointeur vers la ligne courante du fichier source
    BufferedReader br;          
	// Ligne courante du fichier
    String line;
    
    // Fermeture du flux de donnees en lecture
    public void close() {
		try{
			br.close();
		}catch (IOException e){e.printStackTrace();}
	}

    // Nombre de sessions dans le jeu de donnees
	public int count() {
		return data.size();
	}

	// Retourne le jeu de donnees actuel ou complet selon la methode d'ouverture du flux de donnees
	public List<List<Integer>> getDataSet() {
		return this.data;
	}

	// Cette fonction est detournee de son role premier pour les sessions web : elle retourne la liste des urls
	public List<String> getLabels() {
		return this.urls;
	}

	public void next(int n) {
		try{
			if (this.br == null) br = new BufferedReader(new InputStreamReader(new FileInputStream(this.src)));
			// Au cas ou, mais normalement inutile (eventuellement a supprimer plus tard)
			this.data = new ArrayList<List<Integer>>();
			
			int taille = 0;
			Pattern p = Pattern.compile("^(.*?) \"(.*?)\" \"(.*?)\" (.*?) (.*?)$");
	        Matcher m;
	        
	        //int val = -1;
			
	        List<Integer> tmpses = new ArrayList<Integer>();
			
			int compteur = 0;
			
			while ((line = br.readLine())!= null && compteur < n){
				if (line.matches("session-stop.*")) {
	                this.count ++;
	                compteur++; // TODO : ajout pour voir
	                // Ajout de la session courante a la base de donnees
	                this.data.add(tmpses);
	                // Remise a zero de la session courante
	                tmpses = new ArrayList<Integer>();
	            } else if (line.matches("length:.*")) {
	                taille = (new Integer(line.split(":")[1].trim())).intValue();
	            } else if (line.matches("urls:.*")) {
	                for (int i = 0; i < taille; i++) {
	                    // Decoupage de la ligne du fichier
	                    m = p.matcher(br.readLine());

	                    if (m.matches()) {
	                        String url = m.group(1).trim();
	                        url = pretraitement_url(url, generalisation); // Pas de generalisation
	                        /*
	                        if ((val = url.indexOf("?", 0)) > -1){
	                            url = url.substring(0, val);
	                        }
	                        */
	                        
	                        // Recuperation de l'entier representatif de l'url
	                        Integer urlId = urls.indexOf(url);
	                        
	                        // Ajout a la session courante
	                        tmpses.add(urlId);
	                    }
	                } // end for taille
	            } // match urls:
	        }	
		} catch (IOException e){e.printStackTrace();}
		
	}

	public void open(String filename) {
		try{
			this.count = 0;
			this.data = new ArrayList<List<Integer>>();
			this.urls = new ArrayList<String>();
			this.src = filename;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

			int taille = 0;

            Pattern p = Pattern.compile("^(.*?) \"(.*?)\" \"(.*?)\" (.*?) (.*?)$");
            Matcher m;
            
            //int val = -1;
			
			/*** Premiere passe sur l'ensemble du fichier pour compter les urls ****/
			while ((line = br.readLine())!=null){
				if (line.matches("session-stop.*")) {
                    this.count ++;
                } else if (line.matches("length:.*")) {
                    taille = (new Integer(line.split(":")[1].trim())).intValue();
                } else if (line.matches("urls:.*")) {
                    for (int i = 0; i < taille; i++) {
                        // Decoupage de la ligne du fichier
                        m = p.matcher(br.readLine());

                        if (m.matches()) {
                            String url = m.group(1).trim();
                            url = pretraitement_url(url, generalisation); // Pas de generalisation
	                        /*
	                        if ((val = url.indexOf("?", 0)) > -1){
	                            url = url.substring(0, val);
	                        }
	                        */
                            // Enrichissement de la liste des urls si besoin
                            if (!urls.contains(url)) urls.add(url);
                        }
                    } // end for taille
                } // match urls:
	        }
			this.natt = this.urls.size();
			if (verbose) System.out.println(count + " sessions with " + natt + " attributes each");
			this.br = null;
		} catch (Exception e) {e.printStackTrace();}
	}

	public List<List<Integer>> read(String filename) {
		try{
			this.count = 0;
			this.data = new ArrayList<List<Integer>>();
			this.urls = new ArrayList<String>();
			this.src = filename;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			
			int taille = 0;

            Pattern p = Pattern.compile("^(.*?) \"(.*?)\" \"(.*?)\" (.*?) (.*?)$");
            Matcher m;
            
            //int val = -1;
			
            List<Integer> tmpses = new ArrayList<Integer>();
            
			while ((line = br.readLine())!=null){
				if (line.matches("session-stop.*")) {
                    this.count ++;
                    this.data.add(tmpses);
                    tmpses = new ArrayList<Integer>();
                } else if (line.matches("length:.*")) {
                    taille = (new Integer(line.split(":")[1].trim())).intValue();
                } else if (line.matches("urls:.*")) {
                    for (int i = 0; i < taille; i++) {
                        // Decoupage de la ligne du fichier
                        m = p.matcher(br.readLine());

                        if (m.matches()) {
                            String url = m.group(1).trim();
                            url = pretraitement_url(url, generalisation); // Pas de generalisation
	                        /*
	                        if ((val = url.indexOf("?", 0)) > -1){
	                            url = url.substring(0, val);
	                        }
	                        */
                            // Enrichissement de la liste des urls si besoin
                            if (!urls.contains(url)) urls.add(url);
                            
                            // Recuperation de l'entier representatif de l'url
                            Integer urlId = urls.indexOf(url);
                            
                            // Ajout a la session courante
                            tmpses.add(urlId);
                        }
                    } // end for taille
                } // match urls:
	        }
			this.natt = this.urls.size();
			br.close();
			
			if (verbose) System.out.println(count + " sessions with " + natt + " attributes each");
			return this.data;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
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
			br = new BufferedReader(new InputStreamReader(new FileInputStream(this.src)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.count = 0;
	}
	
	private String pretraitement_url(String url, int generalisation)
	{
		// Suppression de la partie "http query"
		int val = url.indexOf("?", 0);
		if (val > -1) url = url.substring(0, val);
		
		// Suppression du protocole
		val = url.indexOf("://", 0);
		if (val > -1) url = url.substring(val + 3, url.length());
		
		// Suppression d'un eventuel / en debut :
		val = url.indexOf("/", 0);
		if (val > -1) url = url.substring(val + 1, url.length());
		
		// Generalisation
		if (generalisation <= 0) return url;
		
		String[] tmp = url.split("/");
		if (tmp.length <= generalisation) return url;
		
		String res = tmp[0] + "/"; // On ne peut pas generaliser au point d'avoir une url vide a la fin (generalisation <= 0)

		for (int i = 1; i < Math.min(generalisation, tmp.length); i++)
		{
			res = res + tmp[i] + "/";
		}
		return res;
	}

	// Methode permettant de definir avant une analyse le niveau de generalisation des urls.
	// Par defaut, le niveau = -1 (voir la variable 'generalisation')
	public void setGeneralisationLevel(int level)
	{
		this.generalisation = level;
	}
	
	// Remise du flux au debut
	public void reset() {
		try {
			if (br != null) br.close();
			br = new BufferedReader(new InputStreamReader(new FileInputStream(src)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int size() {
		return this.count;
	}
}
