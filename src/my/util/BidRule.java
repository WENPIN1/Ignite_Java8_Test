package my.util;



public class BidRule {
	
	final public static double[] AMOUNT_SEGMENT= 
		{
			3.8, 3.81, 3.82, 3.83, 3.84,3.85, 3.86, 3.87, 3.88, 3.89,
			3.9, 3.95, 
			4.0, 4.05, 4.1, 4.15, 4.2, 4.25, 4.3, 4.35, 4.4, 4.45,
			4.5, 4.55, 4.6, 4.65, 4.7, 4.75, 4.8, 4.85, 4.9, 4.95, 
			5.0, 5.05, 5.1
		};
	
	final public static int AMOUNT_SEG_SIZE = AMOUNT_SEGMENT.length;
	
	final public static double AMOUNT_LIMITWIN_MAX = 300;
	
	final public static double AMOUNT_LIMITPLACE_MAX = 100;


}
