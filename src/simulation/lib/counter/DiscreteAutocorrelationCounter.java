package simulation.lib.counter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ArrayList;;

/**
 * This class implements a discrete time autocorrelation counter
 */
public class DiscreteAutocorrelationCounter extends DiscreteCounter{

	long maxLag = 0;
	
	//See 5.6 "Interne Datenhaltung"
	ArrayList<Double> firstVariables;
	ArrayList<Double> lastVariables;
	double sumCounter;
	ArrayList<Double> listOfSumCounters;
	
	public DiscreteAutocorrelationCounter(String variable, int maxLag) {
		super(variable);
		initVars(maxLag);
		// TODO Auto-generated constructor stub
	}
	
	public DiscreteAutocorrelationCounter(String variable, String type, int maxLag) {
		super(variable);
		initVars(maxLag);
		// TODO Auto-generated constructor stub
	}
	
	private void initVars(int maxLag) {
		firstVariables = new ArrayList<Double>();
		lastVariables = new ArrayList<Double>();
		listOfSumCounters = new ArrayList<Double>();
		this.maxLag = maxLag;
		
		initSumList(maxLag);
	}

	private void initSumList(int maxLag) {
		for(int i = 0; i < maxLag; i++) {
			listOfSumCounters.add(0.0d);
		}
	}
	
	public long getMaxLag() {
		return this.maxLag;
	}
	
	public void setMaxLag(long maxLag) {
		this.maxLag = maxLag;
	}
	
	public void count(double x) {
		super.count(x);
		
		if(firstVariables.size() < this.maxLag) {
			firstVariables.add(x);
		}
		lastVariables.add(x);
		if(lastVariables.size() > this.maxLag) lastVariables.remove(0);
		
		//5.6 "Interne Datenhaltung" sum_k(0:j) x_(i-k) * x_i
		long j = (this.maxLag > (getNumSamples() -1)) ? maxLag : getNumSamples()-1;
		for(int k = 0; k < j; k++) {
			listOfSumCounters.set(k, listOfSumCounters.get(k) + lastVariables.get(lastVariables.size()-1-k) * x);
		}
	}
	
	public double getAutoCovariance(int lag) {
		double result = 0;
		long realLag = (lag < maxLag) ? lag : maxLag;
		
		double sumDiff = 0;
		for(int i = 0; i < realLag; i++) {
			double first = firstVariables.get(i); //sum of x_i for 0<=i<j
			double last = lastVariables.get(lastVariables.size() -1 -i); //sum of n-j<=i<n
			sumDiff = sumDiff + first + last;
		}
		
		//formula 2.34:
		result = 1 / (this.getNumSamples() - lag) * 
				listOfSumCounters.get((int) realLag) - getMean() *
				(2 * getSumPowerOne() - sumDiff) +
				Math.pow(getMean(), 2);
		
		return result;
	}
	
	public double getAutoCorrelation(int lag) {
		double result = 0;
		//See 2.37
		result = getAutoCovariance(lag) / getVariance();
		return result;
	}
	
	public void reset() {
		super.reset();
		firstVariables.clear();
		lastVariables.clear();
		listOfSumCounters.clear();
	}
	
	
	/*
     * TODO Problem 4.1.1 - Implement this class according to the given class diagram!
     * Hint: see section 4.4 in course syllabus
     */


	/**
	 * @see Counter#report()
	 * TODO Uncomment this function if you have implemented the class!
	 */
	@Override
	public String report() {
		String out  = super.report();
		out += ("\n\tCorrelation/Covariance:\n");
		for(int i = 0; i <= (getNumSamples() < maxLag ? getNumSamples() : maxLag); i++){
			out += ("\t\tlag = " + i + "   " +
					"covariance = " + getAutoCovariance(i) + "   " +
					"correlation = " + getAutoCorrelation(i)+"\n");
		}
		return out;
	}
	/**
	 * @see Counter#csvReport(String)
	 * TODO Uncomment this function if you have implemented the class!
	 */
	@Override
	public void csvReport(String outputdir){
	    String content = "";
        for(int i = 0; i <= (getNumSamples() < maxLag ? getNumSamples() : maxLag); i++) {
            content += observedVariable + " (lag=" + i + ")" + ";" + getNumSamples() + ";" + getMean() + ";" +
                    getVariance() + ";" + getStdDeviation() + ";" + getCvar() + ";" + getMin() + ";" + getMax() + ";" +
                    getAutoCovariance(i) + ";" + getAutoCorrelation(i) + "\n";
        }
        String labels = "#counter ; numSamples ; MEAN; VAR; STD; CVAR; MIN; MAX; COV; CORR\n";
        writeCsv(outputdir, content, labels);
	}
}
