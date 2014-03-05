package comparator;

import java.util.List;

public class JaccardSet implements Comparator<List<Integer>>{

	// Retourne la distance de Jaccard exprimee entre les deux ensembles d'entiers
	public double compare(List<Integer> dat1, List<Integer> dat2)
	{
		double res = 0;
        for (Integer val : dat1){
            if (! dat2.contains(val)) res ++;
        }
        for (Integer val : dat2){
            if (! dat1.contains(val)) res ++;
        }
        return res;
	}

}
