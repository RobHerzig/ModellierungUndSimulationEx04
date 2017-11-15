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
	}
	
	public DiscreteAutocorrelationCounter(String variable, String type, int maxLag) {
		super(variable, type);
		initVars(maxLag);
	}
	
	private void initVars(int maxLag) {
		firstVariables = new ArrayList<Double>();
		lastVariables = new ArrayList<Double>();
		listOfSumCounters = new ArrayList<Double>();
		this.maxLag = maxLag;
		
		initSumList(maxLag);
	}

	private void initSumList(int maxLag) {
//		System.out.println("INIT SUM LIST WITH " + maxLag + " ELEMENTS");
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
//		long j = (this.maxLag <= (getNumSamples() -1)) ? maxLag : (getNumSamples()-1);
		int j = (int) Math.min(getNumSamples()-1, maxLag);
		for(int k = 0; k < j; k++) {
			//System.out.println(k + "th iteration");
//			System.out.println("Acquire element " + (k) + " from sumCounterList with " + listOfSumCounters.size());
			listOfSumCounters.set(k, listOfSumCounters.get(k) + lastVariables.get(lastVariables.size()-1-k) * x);
		}
//		System.out.println("Number of sum elements after count: " + listOfSumCounters.size());
	}
	
	public double getAutoCovariance(int lag) {
		double result = 0;
//		System.out.println("MAX LAG: " + maxLag);
		long realLag = (lag < maxLag) ? lag : maxLag;
		if(realLag > listOfSumCounters.size()-1) 
			realLag = listOfSumCounters.size()-1; //otherwise we would divide by zero. quickmafs, smoke trees
		
		double sumDiff = 0;
		for(int i = 0; i < realLag; i++) {
			double first = firstVariables.get(i); //sum of x_i for 0<=i<j
			double last = lastVariables.get(lastVariables.size() -1 -i); //sum of n-j<=i<n
			sumDiff = sumDiff + first + last;
		}
		
//		System.out.println("Elements in sum-list: " + listOfSumCounters.size() + " Requested: " + realLag);
		
		//formula 2.34:
		result = (1d / (this.getNumSamples() - realLag)) * 
				(listOfSumCounters.get((int) realLag) - getMean() *
				(2d * getSumPowerOne() - sumDiff)) +
				Math.pow(getMean(), 2d);
		
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
		System.out.println("RESET DISCRETE AUTOCORRELATION COUNTER");
		firstVariables.clear();
		lastVariables.clear();
		listOfSumCounters.clear();
	}
	

	/**
	 * @see Counter#report()
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
