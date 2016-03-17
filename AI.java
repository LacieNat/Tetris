
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import org.apache.commons.math3.distribution.NormalDistribution;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lacie
 */
public class AI {
    private static AI ai = null;
    private static int weightSize = 22;
    
    private static final double noise = 4.0;
    
    private boolean learning;
    private int iterations = 0;
    
    private BufferedReader br = null;
    private double[] mean = new double[weightSize];
    private double[] variance = new double[weightSize];
    
    private ArrayList<ArrayList<Double>> weightSamples = new ArrayList<>();
    private ArrayList<Double> scores = new ArrayList<>();
    
    //AI is a singleton class
    public static AI newInstance(boolean learning) {
        
        if(ai == null) {
            ai = new AI(learning);
        } 
        
        return ai;
    }
    
    //private constructor
    private AI(boolean learning) {
        Arrays.fill(variance, 100);
        this.learning = learning;
        
        File f = new File("ai.txt"); 
        
        
        try {
            f.createNewFile();
            br = new BufferedReader(new FileReader("ai.txt"));
            
            setIterFromFile(br.readLine());
            setMeanFromFile(br.readLine());
            setVarFromFile(br.readLine());
        } catch(IOException e) {
        }
    }
    
    //generate sample data using box-muller transform
    public ArrayList<ArrayList<Double>> generateWeights() {
        System.out.println("Generating weights");
        int n = 100;
        
        if(iterations>=100) {
            n = 100;
        }
        
        Random r = new Random();
        NormalDistribution x; 
        weightSamples.clear();
        
        for(int i=0; i<n; i++) {
            ArrayList<Double> weights = new ArrayList<>();
            
            for(int j=0; j<weightSize; j++) {
                x = new NormalDistribution(mean[j], Math.sqrt(variance[j]));
                weights.add(x.sample());
            }
            weightSamples.add(weights);
        }
        
        return weightSamples;
    }
   
    
    
    
    
    
    
    //***** FILE OPERATIONS *****//
    public void setIterFromFile(String line) {
        if(line == null)
            return;
        
        iterations = Integer.parseInt(line)<100?0:Integer.parseInt(line)-Integer.parseInt(line)%100;
        //System.out.println(iterations);
    }
    
    public void setMeanFromFile(String line) {
        if(line == null)
            return;
        String[] meanInString = line.split(" ");
        
        for(int i=0; i<meanInString.length; i++) {
            mean[i] = Double.parseDouble(meanInString[i]);
        }
        //System.out.println(mean.length);
    }
    
    public void setVarFromFile(String line) {
        if(line == null)
            return;
        String[] varInString = line.split(" ");
        
        for(int i=0; i<varInString.length; i++) {
            variance[i] = Double.parseDouble(varInString[i]);
        }
        //System.out.println(variance.length);
    }
    
    public void saveFile() {
        try {
           FileWriter fw = new FileWriter("ai.txt", false); 
           PrintWriter pw = new PrintWriter(fw);
           
           pw.println(iterations);
           pw.println(convertString(mean));
           pw.println(convertString(variance));
        } catch(IOException e) {
            
        }
    }
    
    
    



    //***** HELPER FUNCTIONS *****//
    public String convertString(double[] arr) {
        String s = "";
        for(int i=0; i<arr.length; i++) {
            s +=arr[i];
        }
        
        return s;
    }
    
    public void addIteration(double score) {
        iterations++;
        System.out.println(iterations);
        scores.add(score);
        System.out.println("Adding score: " + score);
        
        if(iterations==100) {
            computeStats();           
        }
        
        else if(iterations>100) {
            if(iterations%100==0) {
                computeStats();
            }
        }
    }
    
    public Integer[] sortScores() {
        ArrayIndexComparator a = new ArrayIndexComparator(scores);
        Integer[] idx = a.createIndexArray();
        
        Arrays.sort(idx, a);
        
        return idx;
    }
    
    public void computeStats() {
        Integer[] sortedIdx = sortScores();
        ArrayList<ArrayList<Double>> selectedWeights = new ArrayList<>();
        
        System.out.print("TOP SCORES: ");
        //select the 10 best samples
        for(int i=0; i<10; i++) {
            selectedWeights.add(weightSamples.get(sortedIdx[i]));
            //System.out.print(scores.get(sortedIdx[i]) + " ");
        }
        
        computeMean(selectedWeights);
        computeVariance(selectedWeights);
        saveFile();
        scores.clear();
    }
    
    public void computeMean(ArrayList<ArrayList<Double>> selectedWeights) {
        for(int i=0; i<weightSize; i++) {
            mean[i] = 0;
            for(int j=0; j<selectedWeights.size(); j++) {
                mean[i] += selectedWeights.get(j).get(i);
            }
            mean[i]/=10;
        }
    }
    
    public void computeVariance(ArrayList<ArrayList<Double>> selectedWeights) {
        for(int i=0; i<22; i++) {
            variance[i] = 0;
            for(int j=0; j<selectedWeights.size(); j++) {
                variance[i] += Math.pow((selectedWeights.get(j).get(i) - mean[i]),2);
            }
            variance[i]/=9;
            variance[i]+=4;
        }
    }
}
