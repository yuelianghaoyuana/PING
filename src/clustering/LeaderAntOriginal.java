/*
 * leaderAnt.java
 *
 * Created on 20 octobre 2006, 10:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import readers.DataNumReaderTab;
import readers.IReader;
import readers.SessionReaderUrlList;
import comparator.Comparator;
import comparator.MinkowskiTab;
import comparator.SimilarityUrls;
import comparator.SmithWaterman;
import comparator.WebLevenshtein;



public class LeaderAntOriginal<D> {
    
	// Constante pour activer le mode debug avec les sorties ecran intermediaires
	final boolean verbose = false;
	// Nombre de tests pou la determination de la distance entre 1 fourmi et 1 nid
    final int nbtests;        	 
    // Facteur de taille pour la suppression des nids                    	
    final float taille;      		
    // Taille de l'echantillon pour l'apprentissage du seuil de reconnaissance
    final float echantillon;
    // Liste des donnees deja classees : au depart false
    boolean [] tabu;    	
    // Nombre de donnees
    int count;
    // Comparateur entre donnees
    final Comparator<D> cp;
    // Jeu de donnees
    List<D> dat;
    // Liste d'affectation des individus aux differents clusters
    List<List<Integer>> clusters;  
    // Generateur de nombres aleatoires
    Random r;
    // Valeur du seuil de reconnaissance
    float template;
    
    /**
     * Constructeur par defaut
     * @param tests : nombre de rencontre avec chaque nid
     * @param taille : taille minimale des nids qui seront supprimes a la fin (exprimee en pourcentage de l'effectif total)
     * @param echantillon : pourcentage de l'effectif total servant a apprendre le template
     */
    public LeaderAntOriginal(Comparator<D> _cp, int _tests, float _taille, float _echantillon) {
        this.nbtests = _tests;
        this.taille = _taille;
        this.echantillon = _echantillon;
        this.cp = _cp;
    }
    
    public LeaderAntOriginal(Comparator<D> cp2, float template2) {
		this.cp = cp2;
		this.template = template2;
		this.nbtests = 10;
        this.taille = 0.05f;
        this.echantillon = 0.1f;
	}

    public List<D> getCentresList() {
		List<D> c = new ArrayList<D>();
		for (int i = 0; i < clusters.size(); i++){
			c.add(this.dat.get(clusters.get(i).get(0)));
		}
		return c;
	}
    
	/***
     * Retourne la valeur du seuil qui a ete appris
     * @return
     */
    public double getTemplateValue(){
    	return (double) this.template;
    }
    
    /***
     * Retourne le nombre de clusters decouverts
     * @return
     */
    public int getNumberOfClusters(){
    	return this.clusters.size();
    }
    
    // Construit et retourne la liste des donnees pour chaque cluster
    public List<List<D>> getClusters(){
    	List<List<D>> dat_clusters = new ArrayList<List<D>>();
    	for (int i = 0; i < clusters.size(); i++){
    		List<D> cluster = new ArrayList<D>();
    		for (int j = 0; j < clusters.get(i).size(); j++)
    			cluster.add(dat.get(clusters.get(i).get(j)));
    		dat_clusters.add(cluster);
    	}
    	return dat_clusters;
    }
    
    // Retourne la taille du cluster 'i'
    public int getClusterSize(int i){return this.clusters.get(i).size();}
    private void calculeTemplateClassic(){
    	
        this.template = 0;
        int nbrenc = (int) (echantillon * count);
        for (int k = 0; k < nbrenc; k++){
        	int i = r.nextInt(count);
        	int j = i;
        	while (i == j) j = r.nextInt(count);
        	try {
				this.template += cp.compare(dat.get(i), dat.get(j));
			} catch (Exception e) {
				System.err.println("Error while computing distance between points " + i + " and " + j);
				e.printStackTrace();
			}
        }
        this.template = this.template / nbrenc;
        if (verbose) System.out.println("Learning template " + this.template);
    }
    
    /**
     * Calcule la partition pour le jeu de donnees passe en parametre
     * @param dat : jeu de donnees
     */
    public void cluster(List<D> _dat){
    	
    	// Preparation des structures de donnees
    	
        this.dat = _dat;        // Jeu de donnees
        count = dat.size();    	// Nombre d'objets
        r = new Random();       // Generateur de nombres aleatoires
        
        this.tabu = new boolean[count];
        for (int z=0; z < count; z++) tabu[z] = false;
        
        this.clusters = new ArrayList<List<Integer>>();
        
        // Calcul du seuil - methode simple (moyenne des distances)
        if (this.template == 0) calculeTemplateClassic();
        
        //System.out.println("Template value: " + this.template);
        
        // Clustering ********************************
        
        if (verbose) System.out.println("Clustering");
        
        for (int k = 0; k < count; k++){
        	
        	// Choix aleatoire d'une fourmi f
            int f = r.nextInt(count);
            while (tabu[f]) f = r.nextInt(count);
            tabu[f] = true;
            
            // Determination du meilleur cluster - nid
            
            int cle = -1;
            float mindist = Float.MAX_VALUE;
            float cmp = 0;
            
            for (int c = 0; c < clusters.size(); c++){
                cmp = compareNid(f, c, this.nbtests);
            	if (cmp < mindist){
                	mindist = cmp;
                    cle = c;
                }
            } // Fin du parcours des clusters
            
             
            if (mindist > template){
                // Creation d'un nouveau cluster et ajout de la donnee dedans
                ArrayList<Integer> tmp = new ArrayList<Integer>();
                tmp.add(f);
                clusters.add(tmp);
            } else {
            	List<Integer> tmp = clusters.get(cle);
            	tmp.add(f);
                clusters.set(cle, tmp);
            }
        }
        
        // Finalisation des nids *********************
        if (taille > 0){
        	if (verbose) System.out.println("Smallest nest suppression");
            ArrayList<Integer> others = new ArrayList<Integer>();
            
            int tailleMin = (int)(taille * count);
            
            // Suppression effective des clusters
            int c = 0;
            while (c < clusters.size()){
            	if (clusters.get(c).size() < tailleMin){
            		others.addAll(clusters.get(c));
            		clusters.remove(c);
            	} else c++;
            }
            
            // Insertion d'une etape de reaffectation des donnees aux clusters les plus proches
            if (clusters.isEmpty()){
            	// Dans ce cas il n'y avait que des petits clusters --> tous les points sont regroupes dans le meme cluster
            	clusters.clear();
            	clusters.add(new ArrayList<Integer>());
            	for (Integer i : others) clusters.get(0).add(i);
            	others.clear();
            }
            
            if (others.size() > 0){
            	// Parcours de l'ensemble des donnees affectees temporairement a other
            	for (Integer f : others){
            		int cle = -1;
                    float mindist = Float.MAX_VALUE;
                    float cmp = 0;
                    
                    for (c = 0; c < clusters.size(); c++){
                        cmp = compareNid(f,c,this.nbtests);
                    	if (cmp < mindist){
                        	mindist = cmp;
                            cle = c;
                        }
                    } // Fin du parcours des clusters existants
                    // Ajout de la donnee f au cluster le plus proche d'indice cle
                    if (cle == -1){
                    	System.out.println("Bug !");
                    }
                    clusters.get(cle).add(f);
            	}
            }
            
        }
        
        if (verbose) System.out.println("Done!");
    }
    
    /**
     * Fonction de comparaison d'une fourmi (donnee) a un nid (cluster)
     * La valeur renvoyee est la moyenne des comparaisons realisee entre une fourmi et nbtests fourmis issus du nid qui est evalue
     * @param fourmi : indice de la fourmi
     * @param nid : indice du nid
     * @param nbtests : nombre de rencontres (maximal) avec les membres du nid
     * @return
     * !!! Si nbtests < 0 alors toutes les fourmis du nid sont rencontrees !!!
     */
    private float compareNid(int fourmi, int nid, int nbtests){
    	List<Integer> fourmis = clusters.get(nid);
    	float compare = 0;
    	if (nbtests > 0){
    		for (int k = 0; k < Math.min(nbtests, fourmis.size()); k++){
    			int f = fourmis.get(r.nextInt(fourmis.size()));
    			try {
    				compare += cp.compare(dat.get(fourmi), dat.get(f));
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    		return compare / Math.min(nbtests, fourmis.size());
    	} else {
    		for (int k = 0; k < fourmis.size(); k++){
    			compare += cp.compare(dat.get(fourmis.get(k)), dat.get(fourmi));
    		}
    		return compare / fourmis.size();
    	}
    }
    
    /**
     * Fonction de generation de la partition finale
     * @return la partition des objets presents dans le jeu de donnees
     */
    public int[] createPartition(){
    	int[] part = new int[count];
    	for (int c = 0; c < clusters.size(); c++){
    		for (int k = 0; k < clusters.get(c).size(); k++) part[clusters.get(c).get(k)] = c;
    	}
    	return part;
    }
    
    public List<Integer> getPartition(){
    	List<Integer> part = new ArrayList<Integer>();
    	int[] p = new int[this.dat.size()];
    	for (int c = 0; c < clusters.size(); c++){
    		for (int k = 0; k < clusters.get(c).size(); k++) 
    			p[clusters.get(c).get(k)] = c;
    	}
    	for (int i = 0; i < p.length; i++) part.add(p[i]);
    	return part;
    }
    
    public double eval_rand(int [] partition, List<String> theLabels){
        double res = 0d;
        double cpt = 0d;
        for (int i=0; i < this.dat.size(); i++){
            for (int j=i+1; j < this.dat.size(); j++){
                boolean b1 = (partition[i] == partition[j]);
                boolean b2 = theLabels.get(i).equalsIgnoreCase(theLabels.get(j));
                if ((b1 && b2) || (!b1 && !b2)) res ++;
                cpt ++;
            }
        }
        return (1d - (res/cpt));
    }
    
    /**
     * Fonction de recuperation des centres : pour le moment le centre du groupe est celui qui l'a initie
     */
    public List<D> getCentres(){
    	List<D> centres = new ArrayList<D>();
    	for (int i = 0; i < clusters.size(); i++) centres.add(this.dat.get(clusters.get(i).get(0)));
    	return centres;
    }
    
    
    /**
     * Fonction de test de l'algorithme
     * @param args
     */
    
    public static void main(String[] args) {
        try {
            // Creation de la metrique et du lecteur de jeu de donnees
            //Comparator<List<Float>> cp = new Minkowski<Float>(2);
            //IReader<List<Float>> stream = new DataNumReader<List<Float>>();
            
        	//Comparator<float[]> cp = new MinkowskiTab(2);
            //IReader<float[]> stream = new DataNumReaderTab();
     
        	
           IReader<List<Integer>> stream = new SessionReaderUrlList();
        	
            // Gestion des chemins d'acces aux donnees
            String chemin = "data/";
            //String filename = "liste_alea4.txt";
            String filename = "liste_alea4_2.txt";
            String absolutePath = chemin + filename;
    		System.out.println("Reading " + absolutePath);
    		
    		// Determination et lecture du nom et du nombre de groupes des differents fichiers a traiter
    		File src = new File(absolutePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(src)));
            String ligne = "", fichier; //, k;
            System.out.println(ligne);
            long start, stop;
            while ((ligne = br.readLine()) != null) {
            	fichier = ligne.split(" ")[0];
            	
                // Lecture effective du fichier de donnees
                //List<float[]> ds = stream.read(chemin + "/" + fichier);
            	List<List<Integer>> ds = stream.read(chemin + "/" + fichier);
            	List<String> urls=stream.getLabels();
            	SimilarityUrls sim_urls= new SimilarityUrls(urls, "/") ;
            	Comparator<List<Integer>> cp = new SmithWaterman(sim_urls,-0.5,-0.5); //0.3122169265716237 [179.589 s.]
//            	Comparator<List<Integer>> cp = new WebLevenshtein(sim_urls,-0.5,-0.5); //0.20101871594408904 [144.694 s.]
            	start = System.currentTimeMillis();
                    
//                LeaderAntOriginal<float[]> algo = new LeaderAntOriginal<float[]>(cp, 10, 0.05f, 0.1f);                   
//                algo.cluster((ArrayList<float[]>) ds);
                
              LeaderAntOriginal<List<Integer>> algo = new LeaderAntOriginal<List<Integer>>(cp, 10, 0.05f, 0.1f);                  
                algo.cluster((ArrayList<List<Integer>>) ds);   
                int[] resultat = algo.createPartition();
                    
                stop = System.currentTimeMillis();
                    
                System.out.println(fichier + " : " + algo.eval_rand(resultat, stream.getLabels()) + " [" + ((stop - start) / 1000.0) + " s.]");
            }
            br.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    // Renvoie le poids associe a chaque cluster = le nombre de donnees dans chaque cluster
	public List<Float> getCentresWeights() {
		List<Float> w = new ArrayList<Float>();
		for (int i = 0; i < clusters.size(); i++) w.add(new Float(clusters.get(i).size()));
		return w;
	}
}