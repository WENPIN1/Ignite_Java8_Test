package my.util;

import java.text.DecimalFormat;


public class BidFormat {

	public static String formatDecimal(double x) {
		int intx = (int) x;
		if (x - intx == 0)
			return Integer.toString(intx);
		else
			return Double.toString(x);
	}

	public static String formatDecimalHH(double x, DecimalFormat df2) {
		int intx = (int) x;
		if (x - intx == 0)
			return Integer.toString(intx);
		else {
			return df2.format(x);
		}
	}

	public static int formatInt(double x) {
		return (int) Math.ceil(x);
	}

}
