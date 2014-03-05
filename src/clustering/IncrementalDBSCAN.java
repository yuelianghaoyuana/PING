package clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import comparator.Comparator;
import clustering.DBSCAN;

public class IncrementalDBSCAN <D>{

	DBSCAN<D> dbscan;
	List<D>data;
	List<Integer> clusterIndexs;  
	int clusterSize;
	double eps;
	int minPts;
	List<Integer> assignment;
	ArrayList<ArrayList<Double>> distMat;
	HashMap<Integer, ArrayList<Integer>> dict_region;
	Comparator<D> cp;
	ArrayList<Integer> Neps;
	ArrayList<Integer> regionOfNewData;
	
	boolean in=true; // true: insert , false: delete
	
	public IncrementalDBSCAN( DBSCAN<D> dbscan )
	{
		this.dbscan= dbscan;
		this.data= dbscan.getData();
		this.clusterIndexs= dbscan.getClusterIndexs();
		this.clusterSize= dbscan.getClustersSize();
		this.distMat= dbscan.getDistMat();
		this.assignment=dbscan.getAssignment();
		this.eps= dbscan.getEps();
		this.minPts=dbscan.getMinPts();
		this.dict_region= dbscan.getDictRegion();
		this.cp= dbscan.getComparator();
		this.Neps=dbscan.getNeps();
	}
	
	//TODO
	public void updateCluster(List<D> newDatas)
	{
		
		for(int d=0; d< newDatas.size();d++)
		{
			/*
			updateDistMat(newDatas.get(d));
			
			if(this.in)
				//insertion
			else
				//deletion
				*/
		}
	}
	
	
	/*
	when a new data come, we should update the dataset, the distance matrix,
	the dictionary of region of each point
	*/
	public void updateData(D newData)
	{
		this.data.add(newData);
	}
	
	
	public void updateDistMatrix(D newData)
	{
		double dist;
		int indexOfNewData= this.data.size()-1;  //last data is the newData
		for(int i=0; i<indexOfNewData;i++)
		{
			dist=this.cp.compare(data.get(i) , newData);
			//update DistMat
			this.distMat.get(i).add(dist);
		}
		this.distMat.add( new ArrayList( Arrays.asList(new double[this.data.size()]) ) );	

	}
	
	
	public void updateRegionDict()
	{
		int indexOfNewData= this.data.size()-1;  //index of the newPoint in the whole data set
		this.regionOfNewData= new ArrayList<Integer>();
		this.regionOfNewData.add(indexOfNewData); 
		
	    for(int i=0; i<indexOfNewData;i++ )
	    	{
	    		if(this.distMat.get(i).get(indexOfNewData)<eps) 
	    			{
	    				this.regionOfNewData.add(i);
	    				this.dict_region.get(i).add(indexOfNewData);
	    			}
	    	}
	    this.dict_region.put(indexOfNewData, this.regionOfNewData);
		
	}
		
	public void updateNeps()
	{
		for( int index: this.regionOfNewData)
		{
			this.Neps.set(index, this.Neps.get(index)+1);
		}
		//new Neps for newData
		this.Neps.add(this.dict_region.get(this.data.size()-1).size());
	}
		
	public ArrayList<Integer> get_qprime()
	{
		ArrayList<Integer> qprime= new ArrayList<Integer>();
		int indexOfNewData= this.data.size()-1;
		//for previous data, the Neps should be exactly minPts
		for(int i=0; i< indexOfNewData; i++)
		{
			if(this.Neps.get(i)==this.minPts)
			{
				qprime.add(i);
			}
		}
		
		//for the new data, the Neps should juste >= than minPts
		if( this.Neps.get(indexOfNewData)>= this.minPts)
			qprime.add(indexOfNewData);
		
		return qprime;
	}
	
	
	//check if i is core obj
	public boolean isCore(int i)
	{
		return (this.Neps.get(i)>= this.minPts);
	}
	
	
	
	public void basicUpdate(D newData)
	{
		//update data
		updateData(newData);
		
		//update Distance Matrix
		updateDistMatrix(newData);
		
		//update Region Dictionary
		updateRegionDict();
		//update number of points in region
		updateNeps();
	}
	
	
	public ArrayList<Integer> getSeedForUpdate()
	{
		ArrayList<Integer> qprimeRegion;
		Set<Integer> seeds= new HashSet<Integer>();
		//get qprime
		ArrayList<Integer> qprimes = get_qprime();
		for (int qprime: qprimes)
		{
			qprimeRegion= this.dict_region.get(qprime);
			for (int q: qprimeRegion)
			{
				//check core 
				if (isCore(q)) seeds.add(q);   //Do not know if works for duplicate
			}
		}
		
		ArrayList<Integer> res= new ArrayList<Integer>();
		res.addAll(seeds);
		return res;
	}

	public boolean isAllSameId(ArrayList<Integer> seeds, int id)
	{
		boolean allSame= true;
		for(int seed: seeds)
		{
			if ( this.assignment.get(seed)!= id)
				{
				allSame=false;
					break;
				}
		}
		return allSame;
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
	
	
	
	public void updateCluster()
	{
		int indNewData= this.data.size()-1;
		ArrayList<Integer>seeds= getSeedForUpdate();
		ArrayList<Integer> core_seeds= new ArrayList<Integer>();
		for (int seed: seeds)
		{
			if (isCore(seed))
				core_seeds.add(seed);
		}
		//case: noise
		if (core_seeds.isEmpty())
				dbscan.changeAssignment(indNewData, -1);
		else
		{
		
		if ( core_seeds.contains(indNewData))
		{
			//leave off new data
			core_seeds.remove((Integer) indNewData);	
			//creation
			if(isAllSameId(core_seeds,-1))
				{
					changeAssignments(core_seeds, this.clusterSize); //
					changeAssignment(indNewData, this.clusterSize);
				}
			//absorption
			if (isAllSameId(core_seeds, this.assignment.get(core_seeds.get(0))))
			{
				
			}
			
			
		}
		else
		{
			
			
			
		}
		
		//case: creation
		
		//case: absorption
		
		//case:merge
		
		
		}
	}
	
}
