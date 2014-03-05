package comparator;

import java.util.List;

/**
 * Applique une distance d'edition entre les elements des listes dat1 et dat2
 * Les couts pour l'insertion et la substitution sont egaux a 1 par defaut et peuvent etre
 * definis dans le construction. Le cout de substitution est precise par un comparateur passe en
 * parametre du constructeur.
 **/

public class WebLevenshtein implements Comparator<List<Integer>>{

	// Valeurs par defaut
	double ins = 1, del = 1;
	double subval = 0.5;
	Comparator<Integer> sub = null;
	
	/**
	 * Constructeur 1:
	 * @param substitute : comparaison des elements entiers a l'aide d'un second comparateur
	 * @param insert : cout de l'insertion (fixe)
	 * @param delete : cout de la suppression (fixe)
	 */
	public WebLevenshtein(Comparator<Integer> substitute, double insert, double delete)
	{
		this.sub = substitute;
		this.ins = insert;
		this.del = delete;
	}

	/**
	 * Constructeur 2:
	 * @param substitute : cout de la substitution (fixe)
	 * @param insert : cout de l'insertion (fixe)
	 * @param delete : cout de la suppression (fixe)
	 */
	public WebLevenshtein(double substitute, double insert, double delete)
	{
		this.subval = substitute;
		this.ins = insert;
		this.del = delete;
	}
	
	/**
	 * Comparateur (distance d'edition) a proprement parler
	 */
	public double compare(List<Integer> dat1, List<Integer> dat2) {
		int i; // Compteur sur les elements de s1
        int j; // Compteur sur les elements de s2
        int n = dat1.size(); // # elements session 1
        int m = dat2.size(); // # elements session 2
        Integer url1, url2;
        double cost = 0;

        // Initialisation de la matrice de Levenshtein
        double[][] l = new double[n + 1][m + 1]; // l pour Levenshtein

        for (i = 0; i <= n; i++) l[i][0] = i;
        for (j = 0; j <= m; j++) l[0][j] = j;

        // Calcul des valeurs de cout
        for (i = 1; i <= n; i++){
            url1 = dat1.get(i-1);
            for (j = 1; j <= m; j++){
                url2 = dat2.get(j-1);
                cost = 1 - sub.compare(url1, url2);
                l[i][j] = Math.min(Math.min(l[i-1][j] + 1, l[i][j-1] + 1), l[i-1][j-1] + cost); 
            }
        }

        /* Il faut retourner la valeur de la composante en bas a droite de la matrice
         * normalisee par la taille de la session la plus grande */

        return (l[n][m] / (double) Math.max(n,m));
	}

}
