
import java.util.Arrays;
import java.util.HashMap;
import net.sourceforge.jswarm_pso.FitnessFunction;
import net.sourceforge.jswarm_pso.Particle;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lacie
 */
public class TetrisFitnessFunction extends FitnessFunction {

    Particle[] particles;
    HashMap<Integer, Integer> indexToFitness;
    
    
    public TetrisFitnessFunction() {
        super();
        indexToFitness = new HashMap<>();
        
    }
    
    public void setParticles(Particle[] particles) {
        this.particles = particles;
    }
    
    public void clearHashMap() {
        indexToFitness.clear();
    }
    
    public void set(int index, int clearedRows) {
        indexToFitness.put(index, clearedRows);
    }
    
    public int findParticleIndexFromPosition(double[] pos) {
        int index = -1;
        for(int i=0; i<particles.length; i++) {
            double[] partPos = particles[i].getPosition();
            
            if(Arrays.equals(partPos, pos)) {
                return i;
            }
        }
        
        return index;
    }
    
    @Override
    public double evaluate(double[] pos) {
        int index = findParticleIndexFromPosition(pos);
        
        return indexToFitness.get(index);
    } 
}
