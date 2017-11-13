package simulation.lib.counter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * This class implements a discrete time autocorrelation counter
 */
public class DiscreteAutocorrelationCounter {
	/*
     * TODO Problem 4.1.1 - Implement this class according to the given class diagram!
     * Hint: see section 4.4 in course syllabus
     */


	/**
	 * @see Counter#report()
	 * TODO Uncomment this function if you have implemented the class!
	 */
	/*@Override
	public String report() {
		String out  = super.report();
		out += ("\n\tCorrelation/Covariance:\n");
		for(int i = 0; i <= (getNumSamples() < maxLag ? getNumSamples() : maxLag); i++){
			out += ("\t\tlag = " + i + "   " +
					"covariance = " + getAutoCovariance(i) + "   " +
					"correlation = " + getAutoCorrelation(i)+"\n");
		}
		return out;
	}*/
	/**
	 * @see Counter#csvReport(String)
	 * TODO Uncomment this function if you have implemented the class!
	 */
	/*@Override
	public void csvReport(String outputdir){
	    String content = "";
        for(int i = 0; i <= (getNumSamples() < maxLag ? getNumSamples() : maxLag); i++) {
            content += observedVariable + " (lag=" + i + ")" + ";" + getNumSamples() + ";" + getMean() + ";" +
                    getVariance() + ";" + getStdDeviation() + ";" + getCvar() + ";" + getMin() + ";" + getMax() + ";" +
                    getAutoCovariance(i) + ";" + getAutoCorrelation(i) + "\n";
        }
        String labels = "#counter ; numSamples ; MEAN; VAR; STD; CVAR; MIN; MAX; COV; CORR\n";
        writeCsv(outputdir, content, labels);
	}*/
}
