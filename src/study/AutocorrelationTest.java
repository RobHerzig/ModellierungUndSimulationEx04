package study;

import simulation.lib.counter.DiscreteAutocorrelationCounter;


public class AutocorrelationTest {
	static DiscreteAutocorrelationCounter counter;
	static int maxLag = 10;
	
    public static void testAutocorrelation() {
        counter = new DiscreteAutocorrelationCounter("Auto Correlation Test", maxLag);
        
        //Only 2s
        System.out.println("COUNT ONLY TWOS!!!");
        countOnlyTwos(10);
        System.out.println("ONE TO MAX!!!");
        oneToMax(10);
        System.out.println("TWO/TWO/MINUS TWO");
        twoMinusTwo(10);
        System.out.println("THREE/MINUSTHREE");
        threeCombo(10);
        System.out.println("STEPS");
        steperrinos(10);
        System.out.println("i-SQUARED");
        squareComboOnTheBlockSmokeTrees(10);
    }
    
    static void cleanList(String exampleName) {
    	counter.reset();
        counter = new DiscreteAutocorrelationCounter("AutoCorr Test: " + exampleName, maxLag);
    }
    
    static void countOnlyTwos(int iterations) {
    	cleanList("COUNT ONLY TWOS");
    	for(int i = 0; i < iterations; i++) {
        	counter.count(2);
        }
    	System.out.println("COUNT ONLY TWOS REPORT:");
        System.out.println(counter.report());
        
    }
    
    static void oneToMax(int iterations) {
    	cleanList("1, 2, .... i");
    	for(int i = 0; i < iterations; i++) {
        	counter.count(i);
        }
    	System.out.println("1, 2, .... i");
        System.out.println(counter.report());
        
    }
    
    static void twoMinusTwo(int iterations) {
    	cleanList("repeat{2,2,-2}");
    	for(int i = 0; i < iterations; i++) {
        	int countable = (i%3 == 2) ? -2 : 2;
        	counter.count(countable);
        }
    	System.out.println("repeat{2,2,-2}");
        System.out.println(counter.report());
        
    }
    
    static void threeCombo(int iterations) {
    	cleanList("repeat{-3,3}");
    	for(int i = 0; i < iterations; i++) {
        	int countable = (i%2 == 0) ? -3 : 3;
        	counter.count(countable);
        }
    	System.out.println("repeat{-3,3}");
        System.out.println(counter.report());
        
    }
    
    static void squareComboOnTheBlockSmokeTrees(int iterations) {
    	cleanList("i squared");
    	for(int i = 0; i < iterations; i++) {
        	counter.count(Math.pow(i, 2)); //In Soviet Russia, adequate is you
        	//delivers very big abs(covariance) as i grows ~ x^2 growth
        }
    	System.out.println("i squared");
        System.out.println(counter.report());
        
    }
    
    static void steperrinos(int iterations) {
    	cleanList("repeat{1,2,3}");
    	int countable = 0;
    	for(int i = 0; i < iterations; i++) {
    		if(i%3 == 0) countable = 1;
    		if(i%3 == 1) countable = 2;
    		if(i%3 == 2) countable = 3;
    		counter.count(countable);
        }
    	System.out.println("repeat{1,2,3}");
        System.out.println(counter.report());
        
    }
    
    public static void main(String[] args) {
		AutocorrelationTest.testAutocorrelation();
	}
}
