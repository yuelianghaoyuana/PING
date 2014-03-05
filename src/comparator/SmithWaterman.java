package comparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmithWaterman implements Comparator<List<Integer>> {
	
	private List<Integer> s1;
	private List<Integer> s2;
	
	//default score value
	private double ins = -0.1, del = -0.1;
	private double match = 1;
	private double mismatch= -0.5;
	Comparator<Integer> sim = null;

	
	public SmithWaterman(){};

	//use for integrate for PING
	public SmithWaterman(Comparator<Integer> sim, double insert, double delete)
	{
		this.sim = sim;
		this.ins = insert;
		this.del = delete;
	}
	
	public SmithWaterman(double mismatch, double match, double insert, double delete)
	{
		this.mismatch = mismatch;
		this.match= match;
		this.ins = insert;
		this.del = delete;
	}
	


	
	public double compare(List<Integer> s1, List<Integer> s2)
	{
		
		int lenI;  //row length of scroe matrix
		int lenJ;  //column length of scroe matrix
		double[][] score_mat;
		int[] maxPos;
		
		//Initialization
		lenI= s1.size()+1;
		lenJ= s2.size()+1;
		score_mat= new double[lenI][lenJ];
		
		fillScoreMat(score_mat);
		maxPos= getMaxPos(score_mat);
//		printScoreMat(score_mat);
		double distance= 1- score_mat[maxPos[0]][maxPos[1]]/(double)Math.max(lenI,lenJ);
		return distance;
	}
	
	
	public double getMaxVal(double a, double b, double c, double d)
	{
		
		return Math.max(a, Math.max(b, Math.max(c,d)));
	}
	
	
	public int getDirect( double left, double diag, double up)
	{
		//direction: 0: left 1: diag 2: up
		int direct= (left>diag)?0:1 ;
		if (direct==0)
			return (left>up)?0:2 ;
		else
			return (diag>up)?1:2; 		
	}
	
	//compute score matrix
	public void fillScoreMat( double[][]score_mat )
	{
		int lenI= score_mat.length;
		int lenJ= score_mat[0].length;
		//first row and col is 0
		for (int i=0; i<lenI; i++) {score_mat[i][0]=0.0;}
		for (int j=0; j<lenJ; j++) {score_mat[0][j]=0.0;}
		

		for(int i=1; i< lenI; i++)
		{
			for (int j=1; j< lenJ; j++)
			{
			   score_mat[i][j]= getMaxVal(score_mat[i-1][j-1]+this.sim.compare(i-1, j-1), score_mat[i-1][j]+this.del,score_mat[i][j-1]+this.ins, 0);
			}
		}
	}
	
	//find the position of max val of score matrix
	public int[] getMaxPos(double[][] score_mat)
	{
		int lenI= score_mat.length;
		int lenJ= score_mat[0].length;
		double maxVal=-1.0;
		int[] maxPos= new int[2];
		for(int i=0; i< lenI; i++)
		{
			for (int j=0; j< lenJ; j++)
			{
				if (score_mat[i][j]> maxVal)
				{
					maxVal=score_mat[i][j];
					maxPos[0]=i;
					maxPos[1]=j;
				}
			}
		}
		
		return maxPos;
	}
	

	
	public void printScoreMat(double[][] score_mat)
	{
		int lenI= score_mat.length;
		int lenJ= score_mat[0].length;
		for(int i=0; i< lenI; i++)
		{
			for (int j=0; j< lenJ; j++)
			{
				System.out.print(String.valueOf(score_mat[i][j])+" ");
			}
			System.out.print("\n");
		}
	}



}