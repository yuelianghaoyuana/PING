package clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import readers.IReader;
import readers.SessionReaderUrlList;
import comparator.Comparator;
import comparator.SmithWaterman;
import readers.DataNumReaderTab;
import readers.IReader;
import readers.SessionReaderUrlList;
import comparator.Comparator;
import comparator.MinkowskiTab;
import comparator.SimilarityUrls;
import comparator.SmithWaterman;

public class DBSCAN<D> {
	Comparator<D> cp;
	List<D>data;
	List<Integer> assignment; // clId of each member
	List<Integer> unvisited;
	HashMap<Integer, ArrayList<Integer>> clusters; //list of data of each cluster
	List<Integer> clusterIndexs;  
	int clusterSize;
	double eps;
	int minPts;
	int clId; // -2: unvisited, -1: noise, 0-n: cluster
	ArrayList<ArrayList<Double>> distMat;
	int dataLength;
	HashMap<Integer, ArrayList<Integer>> dict_region;
	ArrayList<Integer> Neps; 
	/*
	ex: clusters: {0:[1,2], 1: [0,3,4], 3:[5,6,7]}
		clusterSize: 3
		clusterIndexs: [0,1,3]
		assignment: [1,0,0,1,1,3,3,3]
	*/
	
	
	public DBSCAN(Comparator<D> cp, double eps, int minPts)
	{
		this.cp=cp;
		this.eps=eps;
		this.minPts=minPts;
	}
	
	public List<D> getData()
	{
		return this.data;
	}
	
	public Comparator<D> getComparator()
	{
		return this.cp;
	}
	
	public List<Integer> getAssignment()
	{
		return this.assignment;
	}
	
	public ArrayList<ArrayList<Double>> getDistMat()
	{
		return this.distMat;
	}
	public List<Integer> getClusterIndexs()
	{
		return this.clusterIndexs;
	}
	
	public void setMinPts(int minPts)
	{
		this.minPts= minPts;
	}
	
	
	public int getMinPts()
	{
		return this.minPts;
	}
	
	
	public void setEps(double eps)
	{
		this.eps=eps;
	}
	
	
	public double getEps()
	{
		return this.eps;
	}
	
	//compute distance matrix of each pair of data, up triangle
	public void computeDistMat()
	{
		this.distMat=new ArrayList<ArrayList<Double>>();
		for(int i=0; i< this.dataLength;i++)
		{
			ArrayList<Double> distance = new ArrayList<Double>();
		   for(int j=0; j< this.dataLength;j++)
		   {
			  if(j<=i) 
				  distance.add(0.0);
			  if(j>i)
			   distance.add( this.cp.compare(data.get(i) , data.get(j)));    
		   }
		   distMat.add(distance);
		}
	}
	
	// check if a point is visited or not
	public boolean isUnvisited(int point )
	{
		return this.assignment.get(point)==-2 ;
	}
	
	//key: point value
	//index of points in his region of eps
	public void computeRegionDict(double eps)
	{
		
		for (int i=0;i<this.dataLength;i++)
		{
			ArrayList<Integer> region= new ArrayList<Integer>();
			region= getRegionOfPoint(i);
		    this.dict_region.put(i, region);
		}
	}
	
	
	public ArrayList<Integer> getNeps()
	{
		this.Neps=new ArrayList<Integer>();
		for(int i=0; i< this.data.size();i++)
		{
			this.Neps.add(this.dict_region.get(i).size());
		}
		return this.Neps;
	}
	
	
	
	public ArrayList<Integer> getRegionOfPoint(int i)
	{
		ArrayList<Integer> region= new ArrayList<Integer>();
		//the region of point i
		 region.add(i); // himself, and is the first
		//compare the front i's dist
	    for(int r=0; r<i;r++ )
	    	{
	    		if(this.distMat.get(r).get(i)<eps) region.add(r);
	    	}
	  //compare the last n-i's dist
	    for (int c=i+1;c< this.dataLength;c++)
	    {
	    	if (this.distMat.get(i).get(c)< eps) region.add(c);
	    }
	   
	    return region;
	}
	
	public HashMap<Integer, ArrayList<Integer>> getDictRegion()
	{
		return this.dict_region;
	}
	
	
	public void printDistMat()
	{
		for(int i=0; i< this.dataLength; i++)
		{
			for (int j=0; j< this.dataLength; j++)
			{
				System.out.print(String.valueOf(this.distMat.get(i).get(j))+" ");
			}
			System.out.print("\n");
		}
	}
	
	public void cluster(List<D> data)
	{
		//set params
		this.data=data;
		this.dataLength=data.size();
		this.dict_region= new HashMap<Integer, ArrayList<Integer>>();
		
		//dist_mat
		computeDistMat();
		//printDistMat();
		computeRegionDict(this.eps);
		
		//ids
		this.assignment= new ArrayList<Integer>();
		for(int i=0; i< this.dataLength; i++)
		{
			this.assignment.add(-2);//-2: unvisited
		}
		
		
		int clId=0;
		this.clusterIndexs=new ArrayList<Integer>();
		for (int i=0; i< this.dataLength; i++)
		{
			//if not visitied, check if can be expand	
			if (isUnvisited(i))
			{
				//check expand
				if(expandCluster( i,  clId,  this.eps, this.minPts)) 
					{
						this.clusterIndexs.add(clId);
						clId+=1; 
					}
			}
			
		}
		//clusters: 0:(clId-1)
		this.clusterSize= this.clusterIndexs.size();
		this.clusters= new HashMap<Integer, ArrayList<Integer>>();
		for (int cl=0; cl< this.clusterSize; cl++)
		{
			this.clusters.put(cl, new ArrayList<Integer>() );
		}
		
		
		for(int i=0; i< this.dataLength;i++)
		{	
			this.clusters.get(this.assignment.get(i)).add(i); 
		}
	}
	
	//get neighbors of a point
	public ArrayList<Integer> regionQuery(int point)
	{
		return this.dict_region.get(point);
	}
	
	//change cluster id of a set of points named seeds
	public void changeAssignments(List<Integer> seeds, int clId)
	{
		for  (int point:seeds)
		{
			this.assignment.set(point, clId);
		}
	}
	
	//change cluster id of a point 
	public void changeAssignment(int point, int clId)
	{
			this.assignment.set(point, clId);
		
	}
	//Expand cluster
	public boolean expandCluster( int point, int clId, double eps, int minPts)
	{
		ArrayList<Integer> seeds= regionQuery(point);
		//noise
		if (seeds.size()< this.minPts) 
			{
				changeAssignment(point, -2);
				return false;
			}
		//core point
		else
		{		
				changeAssignments(seeds, clId); //add point to cluster
				seeds.remove(0);  //!!!the first is himself
				
				//neighbors.remove((Integer) point);   //decomment for test 
			
				ArrayList<Integer> current_neighbors; 
			while(!seeds.isEmpty())  //if has neighbor, continue
			{
				int currentPoint= seeds.get(0);  //each time get the first point in the list as currentPoint 
				current_neighbors= regionQuery(currentPoint);
				if(current_neighbors.size()>= minPts) //neighbors of point is also core point
				{
					for (Integer currentNeighborPt: current_neighbors)
					{
						//not visited, chance to expand,so add it to seeds
						//noise, no need to add in the list of seed
						if (isUnvisited(currentNeighborPt))  
							seeds.add(currentNeighborPt);	

						changeAssignment(currentNeighborPt, clId);
					}			
				}
				seeds.remove(0);	//each time clear the current point whose position is 0
			}
			return true;
		}
	}
			
	//cluster assignment for each data
	public void printAssignments()
	{
		for (int i=0;i< this.dataLength; i++)	
			System.out.println(this.assignment.get(i));
	}
	
	//get the data index of the cluster I
	public List<Integer> getMembersOfCluster(int clusterId)
	{
		return this.clusters.get(clusterId);
	}
	
	//get the size of the cluster I
	public int getClusterSize(int clusterId)
	{
		return this.clusters.get(clusterId).size();
	}
	
	public int getClustersSize()
	{
		return this.clusterSize;
	}
	//-----------------------------------------------------------------------------------------------
	//this is part of evaluation, i'm not sure about it
	public int[] createPartition()
	{
	    	int[] part = new int[this.dataLength];
	    for (int i=0; i< this.dataLength; i++)
	    {
	    	part[i]= this.assignment.get(i);
	    }
	    	
	    	return part;
	 }
	 
	    public double eval_rand(int [] partition, List<String> theLabels){
	        double res = 0d;
	        double cpt = 0d;
	        for (int i=0; i < this.dataLength; i++){
	            for (int j=i+1; j < this.dataLength; j++){
	                boolean b1 = (partition[i] == partition[j]);
	                boolean b2 = theLabels.get(i).equalsIgnoreCase(theLabels.get(j));
	                if ((b1 && b2) || (!b1 && !b2)) res ++;
	                cpt ++;
	            }
	        }
	        return (1d - (res/cpt));
	    }
	  //-----------------------------------------------------------------------------------------------
	 
	
	
		
	 public static void main(String[] args) {
//Data numbers
	        try {
	            // Creation de la metrique et du lecteur de jeu de donnees
	        	Comparator<float[]> cp = new MinkowskiTab(2);
	            IReader<float[]> stream = new DataNumReaderTab();
	        	
	            // Gestion des chemins d'acces aux donnees
	            String chemin = "data/";
	            String filename = "liste_alea4.txt";
	            String absolutePath = chemin + filename;
	    		System.out.println("Reading " + absolutePath);
	    		
	    		// Determination et lecture du nom et du nombre de groupes des differents fichiers a traiter
	    		File src = new File(absolutePath);
	            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(src)));
	            String ligne = "", fichier; //, k;
	            long start, stop;
	            while ((ligne = br.readLine()) != null) {
	            	fichier = ligne.split(" ")[0];
	                    
	                // Lecture effective du fichier de donnees
	                List<float[]> ds = stream.read(chemin + "/" + fichier);
	                    	
	                start = System.currentTimeMillis();
	                    
	                DBSCAN<float[]> algo = new DBSCAN<float[]>(cp, 2000000, 2 );                
	                    
	                algo.cluster((ArrayList<float[]>) ds);     
	                int[] resultat = algo.createPartition();
                    
	                stop = System.currentTimeMillis();
	                    
	                System.out.println(fichier + " : " + algo.eval_rand(resultat, stream.getLabels()) + " [" + ((stop - start) / 1000.0) + " s.]");
	                stop = System.currentTimeMillis();

	            }
	        } catch (Exception e) {e.printStackTrace();}
	    }
		
		 
//Data: Sessions		 
//		  try {
//	            // Creation de la metrique et du lecteur de jeu de donnees
//	            //Comparator<List<Float>> cp = new Minkowski<Float>(2);
//	            //IReader<List<Float>> stream = new DataNumReader<List<Float>>();
//	            
//	        	//Comparator<float[]> cp = new MinkowskiTab(2);
//	            //IReader<float[]> stream = new DataNumReaderTab();
//	     
//	        	
//	           IReader<List<Integer>> stream = new SessionReaderUrlList();
//	        	
//	            // Gestion des chemins d'acces aux donnees
//	            String chemin = "data/";
//	            //String filename = "liste_alea4.txt";
//	            String filename = "liste_alea4_2.txt";
//	            String absolutePath = chemin + filename;
//	    		System.out.println("Reading " + absolutePath);
//	    		
//	    		// Determination et lecture du nom et du nombre de groupes des differents fichiers a traiter
//	    		File src = new File(absolutePath);
//	            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(src)));
//	            String ligne = "", fichier; //, k;
//	            System.out.println(ligne);
//	            long start, stop;
//	            while ((ligne = br.readLine()) != null) {
//	            	fichier = ligne.split(" ")[0];
//	            	List<List<Integer>> ds = stream.read(chemin + "/" + fichier);
//	            	List<String> urls=stream.getLabels();
//	            	SimilarityUrls sim_urls= new SimilarityUrls(urls, "/") ;
//	            	Comparator<List<Integer>> cp = new SmithWaterman(sim_urls,-0.5,-0.5); 
//	            	start = System.currentTimeMillis();
//	            	
//	            	DBSCAN<List<Integer>> algo = new DBSCAN<List<Integer>>(cp, 0.5, 5 );                 
//	                algo.cluster((ArrayList<List<Integer>>) ds);   
//	                int[] resultat = algo.createPartition();
//	                    
//	                stop = System.currentTimeMillis();
//	                    
//	                System.out.println(fichier + " : " + algo.eval_rand(resultat, stream.getLabels()) + " [" + ((stop - start) / 1000.0) + " s.]");
//	            }
//	            br.close();
//	        } catch (Exception e) {e.printStackTrace();}
//	 }
	
	
}
	