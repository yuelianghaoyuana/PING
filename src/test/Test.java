package test;
import java.util.List;

import comparator.JaccardSet;
import clustering.LeaderAntOriginal;
import readers.*;

public class Test {
	public static void main(String[] args) throws Exception {
		 JaccardSet cp_jacc= new JaccardSet();
		LeaderAntOriginal cluster= new	LeaderAntOriginal(cp_jacc, 10,0.05f,0.1f);
		DataNumReaderTab  flux= new DataNumReaderTab();
		
		List<float[]> data= flux.read("data/art5_alea.data");
		cluster.cluster(data);
		
	
	}
	

}
