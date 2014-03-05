package comparator;

import java.util.List;

/**
 * Classe qui permet de calculer la similartie topographique entre deux urls a partir
 * d'un dictionnaire d'urls passe en parametre du constructeur
 * @author Nicolas
 *
 */

public class SimilarityUrls implements Comparator<Integer>{

	List<String> urls;
	String separator = "/"; // Initialement ":"
	
	public SimilarityUrls(List<String> urls, String separator) throws Exception
	{
		if (urls == null) throw new Exception("Parametre vide");
		this.urls = urls;
		if (separator != null) this.separator = separator;
	}
	
	/**
     * Fonction qui calcule la similarite topographique entre 2 chaines
     * qui representent un chemin et dont les portions de chemin sont separees
     * par le caractere separator.
     * @param path1 : premier chemin a comparer
     * @param path2 : second chemin a comparer
     * @param separator : caractere de separation des elements du chemin
    */
	public double compare(Integer dat1, Integer dat2) {
		String path1 = urls.get(dat1);
		String path2 = urls.get(dat2);
		
		int cmp = path1.compareTo(path2);
        if ( cmp == 0) return 1d;

        if (cmp > 0){
            // path1 > path2
            String tmp = path2;
            path2 = path1;
            path1 = tmp;
        }

        // Dans tous les cas url1 < url2 desormais
        
        String[] tab1 = path1.split(separator);
        String[] tab2 = path2.split(separator);

        if ((tab1.length == 0) || (tab2.length == 0)) return 0d;

        int taille_min = Math.min(tab1.length, tab2.length);
        int taille_max = Math.max(tab1.length, tab2.length);

        int i = 0;
        int simi = 0;
        while ((i < taille_min) && (tab1[i].equalsIgnoreCase(tab2[i]))){
            simi = simi + taille_max - i;
            i++;
        }
        
        double nb  = taille_max * (taille_max + 1) / 2d ;
        
        double simil = simi / nb;

        
        return simil;
	}
	
}
