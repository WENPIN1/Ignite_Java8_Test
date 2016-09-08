package my.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class BidUtil {

	private static final Log log = LogFactory.getLog(BidUtil.class);
	final private static boolean DEBUG = log.isDebugEnabled();
	final private static boolean TRACE = log.isTraceEnabled();

	final public static long MAXWINPLCLIMIT = 1 << 29;

	// assume limit has only 1 decimal and does not exceed 999
	// price(amount) is greater/equal than 0.01 and less/equal than 9.21 (2^63-1 for signed long)
	// format such that natural order sort will give price asc, limit desc (complementary)
	// used for ConcurrentSkipListMap nature ordering search (orz).
	public static long formatWinPlcLimitKey(double price, double limitWin, double limitPlace) {

		long lw = (long) (limitWin * 10);   // fine-precision to (0.1 ~ 999.9)
		long lp = (long) (limitPlace * 10); // fine-precision to (0.1 ~ 999.9)
		//long p = (long) (Math.rint(price * 100) * 10000000000000000D);       // fine-precision to (0.01 ~ 9.21)
		long p = (long) (price * 100);
		long lw_pad = 9999 - lw; // up-bound to 999.9 
		long lp_pad = 9999 - lp; // up-bound to 999.9
		lp_pad *= 10000L;  // 1E4		
		lw *= 100000000L;  // 1E8 
		lw_pad *= 1000000000000L;  //1E12
		p *= 10000000000000000L;  //1E16
		
		return p + lw_pad + lw + lp_pad + lp;
	}
	
	
	public static double getLevelPrice(long keyLevel) {
		final double price = keyLevel /10_000_000_000_000_000L / 100D;
		return price ;
	}
	
	public static double getLevelLimitWin(long keyLevel) {
		final double limitWin = keyLevel /100_000_000L %10_000L /10D;
		return limitWin;
	}
	
	public static double getLevelLimitPlace(long keyLevel) {
		final double limitPlace = keyLevel %10_000L /10D;
		return limitPlace;
	}
	
	public static String formatPriceLimits(double price, double limitWin, double limitPlace) {

		DecimalFormat df2 = new DecimalFormat("0.00");
		DecimalFormat df1 = new DecimalFormat("0.0");
		String p = BidFormat.formatDecimalHH(price, df2);
		String lw = BidFormat.formatDecimalHH(limitWin, df1);
		String lp = BidFormat.formatDecimalHH(limitPlace, df1);
		StringBuilder sb = new StringBuilder();
		return sb.append(p).append("|").append(lw).append('/').append(lp).toString();
		
	}
	
	public static String formatPriceLimits(long value) {

		DecimalFormat df2 = new DecimalFormat("0.00");
		DecimalFormat df1 = new DecimalFormat("0.0");
		double price = getLevelPrice(value);
		double limitWin = getLevelLimitWin(value);
		double limitPlace = getLevelLimitPlace(value);
		String p = BidFormat.formatDecimalHH(price, df2);
		String lw = BidFormat.formatDecimalHH(limitWin, df1);
		String lp = BidFormat.formatDecimalHH(limitPlace, df1);
		StringBuilder sb = new StringBuilder();
		return sb.append(p).append("|").append(lw).append('/').append(lp).toString();
		
	}
	



}
