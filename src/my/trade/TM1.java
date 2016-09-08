package my.trade;

import java.util.List;
import java.util.Random;

import javax.cache.Cache.Entry;

import my.own.AskRecord;
import my.own.AskRecordFactory;
import my.own.BidRecord;
import my.own.BidRecordFactory;
import my.util.BidRule;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;


public class TM1 {

	static int BID_COUNT = 100_000;
	public static IgniteLogger logger = null;
	public static boolean RESET = true;
	public static boolean CLEAR = true;
	private static AskOrderEngine askeng = null;
	private static BidOrderEngine bideng  = null;
	
	
	/**
	 * @param bCache
	 * @param min
	 * @param max
	 * @param tm1
	 */
	private static void case1() {
		IgniteCache<Long, BidRecord> bCache = bideng.getCache();
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		long tm0 = 0;
		long tm1 = 0;

		List<Entry<Long, BidRecord>> bidOrders = bideng.matchObjectQuery(0, 500, 500,
			bCache.size(CachePeekMode.PRIMARY));

		for (Entry<Long, BidRecord> e : bidOrders) {
			long bid = (long) e.getKey();
			try (Transaction tx = Ignition.ignite().transactions().txStart(
			// TransactionConcurrency.OPTIMISTIC,
			// TransactionIsolation.SERIALIZABLE
				)) {
				// TODO
				BidRecord bidrec = bCache.getAndRemove(bid);
				if (bidrec == null)
					continue;

				double price = bidrec.getAmount();
				double lw = bidrec.getLimitWin();
				double lp = bidrec.getLimitPlace();
				double tk = bidrec.getWin();
				tm0 = System.nanoTime();
				double tktLeft = askeng.tradeTicket2(price, lw, lp, tk);
				tm1 = System.nanoTime() - tm0;
				min = Math.min(tm1, min);
				max = Math.max(tm1, max);
				if (tktLeft == 0) {
					String info = String.format("BidOrder %d, matched $%.2f,%.1f/%.1f, (%.2f)", bid, price,
						lw, lp, tk);
					logger.info(info);
				} else {
					bidrec.setWin(tk);
					bidrec.setPlace(tk);
					bCache.put(bid, bidrec);
				}
				tx.commit();
			}
		}
		bidOrders.clear();
		String minfo = String.format("Max: %,d ; Min: %,d", max, min);
		print(minfo);

	}
	
	/**
	 * 
	 */
	private static void case2() {
		double tk = 1;
		for (int i = 0; i < BID_COUNT; i++) {
			double tktLeft = askeng.tradeTicket1(5.1, 1, 1, tk);
		}
	}

	/**
	 * @param bCache
	 */
	private static void case3() {
		IgniteCache<Long, BidRecord> bCache = bideng.getCache();
		List<Entry<Long, BidRecord>> bidOrders = bideng.matchObjectQuery(0, 500, 500, bCache.size(CachePeekMode.PRIMARY));

		for (Entry<Long, BidRecord> e : bidOrders) {
			BidRecord bidrec = e.getValue();
			if (bidrec == null)
				continue;

			long bid = bidrec.getBid();
			double price = bidrec.getAmount();
			double lw = bidrec.getLimitWin();
			double lp = bidrec.getLimitPlace();
			double tk = bidrec.getWin();
			double tktLeft = askeng.tradeTicket2(price, lw, lp, tk);
			if (tktLeft == 0) {
				String info = String.format("BidOrder %d, matched $%.2f,%.1f/%.1f, (%.2f)", bid,
					price, lw, lp, tk);
				logger.info(info);
			} else {
				bidrec.setWin(tktLeft);
				bidrec.setPlace(tktLeft);
				bCache.put(bid, bidrec);
			}
		}

	}

	/**
	 * @param min
	 * @param max
	 * @param random
	 */
	private static void case4() {
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		Random random = new Random();
		long tm0;
		long tm1;
		double tk = 1;

		for (long i = 0; i < BID_COUNT; i++) {
			
			double price = BidRule.AMOUNT_SEGMENT[random.nextInt(BidRule.AMOUNT_SEG_SIZE)];
			double lw = 5 + random.nextInt(296);
			double lp = 5 + random.nextInt(96);
			lp = Math.min(lw, lp);

			tm0 = System.nanoTime();
			double tktLeft = askeng.tradeTicket2(price, lw, lp, tk);
			tm1 = System.nanoTime() - tm0;
			min = Math.min(tm1, min);
			max = Math.max(tm1, max);
			String mat = (tk == tktLeft) ? "no match" : "matched";
			String info = String.format("BidOrder %d, %s $%.2f,%.1f/%.1f, (%.2f)", i, mat, price, lw,
				lp, tk);
			logger.info(info);

		}
	}

	/**
	 * @param bCache
	 */
	private static void case5() {
		long tm0;
		long tm1;
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		Random random = new Random();

		IgniteCache<Long, BidRecord> bCache = bideng.getCache();

		for (long i = 0; i < BID_COUNT; i++) {

			int race = 1;
			int horse = 1;
			double win, place;
			win = place = 1; // + random.nextInt(5)
			double amount = BidRule.AMOUNT_SEGMENT[random.nextInt(BidRule.AMOUNT_SEG_SIZE)];
			double limitWin = 5 + random.nextInt(296);
			double limitPlace = 5 + random.nextInt(96);
			limitPlace = Math.min(limitWin, limitPlace);
			String uid_a = "abcd" + String.valueOf(i % 100);
			String currency = "SG";
			String raceType = "3H";
			String raceDate = "2016-08-12";
			double sgWin = 0;
			double sgPlace = 0;
			long date_a = System.currentTimeMillis();

			BidRecord bidrec = BidRecordFactory.genBidRecord(race, horse, win, place, amount, limitWin,
				limitPlace, uid_a, currency, raceType, raceDate, sgWin, sgPlace, date_a, 0);

			// TODO
			try (Transaction tx = Ignition.ignite().transactions()
				.txStart(TransactionConcurrency.OPTIMISTIC, TransactionIsolation.SERIALIZABLE)) {

				long bId = bidrec.getBid();
				double tk = bidrec.getWin();
				tm0 = System.nanoTime();
				double tktLeft = askeng.tradeTicket2(amount, limitWin, limitPlace, tk);
				tm1 = System.nanoTime() - tm0;
				min = Math.min(tm1, min);
				max = Math.max(tm1, max);
				if (tktLeft == 0) {
					String info = String.format("BidOrder %d, matched $%.2f,%.1f/%.1f, (%.2f)", bId, amount,
						limitWin, limitPlace, tk);
					logger.info(info);
				} else {
					bidrec.setWin(tktLeft);
					bidrec.setPlace(tktLeft);
					bCache.put(bId, bidrec);
				}
				if (tktLeft == tk) {
					String mat = (tk == tktLeft) ? "no match" : "matched";
					String info = String.format("BidOrder %d, %s $%.2f,%.1f/%.1f, (%.2f)", bId, mat, amount,
						limitWin, limitPlace, tk);
					logger.info(info);
				}

				tx.commit();
			}
		}
		String minfo = String.format("Max: %,d ; Min: %,d", max, min);
		print(minfo);

	}

	/**
	 * @param askeng
	 * @param bideng
	 * @param bCache
	 * @param aCache
	 * @param min
	 * @param random
	 * @return
	 */
	private static void case6() {
		final int len = BID_COUNT;
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		Random random = new Random();

		long tm0 = 0;
		long tm1 = 0;

		IgniteCache<Long, AskRecord> aCache = askeng.getCache();
		IgniteCache<Long, BidRecord> bCache = bideng.getCache();

		AskRecord[] aArr = new AskRecord[len];
		BidRecord[] bArr = new BidRecord[len];
		int aIx = 0;
		int bIx = 0;
		int INNER_LOOP = 2;

		for (long i = 0; i < BID_COUNT; i++) {

			int race = 1;
			int horse = 1;
			double win, place;
			win = 1 + random.nextInt(5);
			place = win;
			double amount = BidRule.AMOUNT_SEGMENT[random.nextInt(BidRule.AMOUNT_SEG_SIZE)];
			double limitWin = 10 + random.nextInt(30) * 10;
			double limitPlace = Math.max(5, random.nextInt(10) * 10);
			limitPlace = Math.min(limitWin, limitPlace);
			String uid_a = "abcd" + String.valueOf(i % 100);
			String currency = "SG";
			String raceType = "3H";
			String raceDate = "2016-08-12";
			double sgWin = 0;
			double sgPlace = 0;
			long date_a = System.currentTimeMillis();

			aArr[aIx++] = AskRecordFactory.genAskRecord(race, horse, win, place, amount, limitWin,
				limitPlace, uid_a, currency, raceType, raceDate, sgWin, sgPlace, date_a, 0);

			bArr[bIx++] = BidRecordFactory.genBidRecord(race, horse, win, place, amount, limitWin,
				limitPlace, uid_a, currency, raceType, raceDate, sgWin, sgPlace, date_a, 0);

			/*
			 * if (i % INNER_LOOP == 0) {
			 * 
			 * aArr[aIx++] = AskRecordFactory.genAskRecord(race, horse, win,
			 * place, amount, limitWin, limitPlace, uid_a, currency, raceType,
			 * raceDate, sgWin, sgPlace, date_a, 0);
			 * 
			 * } else {
			 * 
			 * bArr[bIx++] = BidRecordFactory.genBidRecord(race, horse, win,
			 * place, amount, limitWin, limitPlace, uid_a, currency, raceType,
			 * raceDate, sgWin, sgPlace, date_a, 0); }
			 */
		}

		int aIx1 = 0;
		int bIx1 = 0;

		for (long i = 0; i < BID_COUNT; i++) {
			try (Transaction tx = Ignition.ignite().transactions().txStart()) {
				// TransactionConcurrency.PESSIMISTIC,
				// TransactionIsolation.READ_COMMITTED)) {

				if (i % INNER_LOOP == 0) {
					AskRecord askrec = aArr[aIx1++];
					// TODO
					double win = askrec.getWin();
					double place = askrec.getPlace();
					double amount = askrec.getAmount();
					double limitWin = askrec.getLimitWin();
					double limitPlace = askrec.getLimitPlace();

					long bId = askrec.getBid();
					final double tk = win;
					tm0 = System.nanoTime();
					final double tktLeft = bideng.tradeTicket2(amount, limitWin, limitPlace, tk);
					if (tktLeft > 0) {
//						AskRecord newAR = askrec;
//						if (tktLeft != tk) {
//							final double winNew = tktLeft;
//							final double placeNew = tktLeft;

//							newAR = AskRecordFactory.makeAskRecord(bId, 1, 1, winNew, placeNew, amount,
//								limitWin, limitPlace, askrec.getUid_a(), askrec.getCurrency(),
//								askrec.getRaceType(), askrec.getRaceDate(), askrec.getSgWin(),
//								askrec.getSgPlace(), askrec.getDateA(), 0);
					    assert (tktLeft >0) : "(tktLeft >0) ? " + tktLeft; 
					    double w =  tktLeft;
					    double p =  tktLeft;
							askrec.setWin(w);
							askrec.setPlace(p);
							askrec.setDateA(System.currentTimeMillis());							
//							newAR = askrec;
//						}
//						aCache.put(bId, newAR);
						aCache.put(bId, askrec);
					}
					tm1 = System.nanoTime() - tm0;

					if (tktLeft < tk) {
						String info = String.format("AskOrder %d, matched $%.2f,%.1f/%.1f, (%.2f -> %.2f)",
							bId, amount, limitWin, limitPlace, tk, tktLeft);
						logger.info(info);
					}

					if (tktLeft == tk) {
						String mat = (tk == tktLeft) ? "no match" : "matched";
						String info = String.format("AskOrder %d, %s $%.2f,%.1f/%.1f, (%.2f) (%.2f)", bId,
							mat, amount, limitWin, limitPlace, tk, tktLeft);
						logger.info(info);
					}
				} else {

					BidRecord bidrec = bArr[bIx1++];
					// TODO
					double win = bidrec.getWin();
					double place = bidrec.getPlace();
					double amount = bidrec.getAmount();
					double limitWin = bidrec.getLimitWin();
					double limitPlace = bidrec.getLimitPlace();

					long bId = bidrec.getBid();
					double tk = win;
					tm0 = System.nanoTime();
					double tktLeft = askeng.tradeTicket2(amount, limitWin, limitPlace, tk);
					if (tktLeft > 0 ) {
//						BidRecord newBR = bidrec;
//						if (tktLeft != tk) {
//							final double winNew = tktLeft;
//							final double placeNew = tktLeft;

//							newBR = BidRecordFactory.makeBidRecord(bId, 1, 1, winNew, placeNew, amount,
//								limitWin, limitPlace, bidrec.getUid_a(), bidrec.getCurrency(),
//								bidrec.getRaceType(), bidrec.getRaceDate(), bidrec.getSgWin(),
//								bidrec.getSgPlace(), bidrec.getDateA(), 0);

							bidrec.setWin(tktLeft);
							bidrec.setDateA(System.currentTimeMillis());
							bidrec.setPlace(tktLeft);
//							newBR = bidrec;

//						}
//						bCache.put(bId, newBR);
						bCache.put(bId, bidrec);
					}
					tm1 = System.nanoTime() - tm0;
					if (tktLeft < tk) {
						String info = String.format("BidOrder %d, matched $%.2f,%.1f/%.1f, (%.2f -> %.2f)",
							bId, amount, limitWin, limitPlace, tk, tktLeft);
						logger.info(info);
					}
					if (tktLeft == tk) {
						String mat = (tk == tktLeft) ? "no match" : "matched";
						String info = String.format("BidOrder %d, %s $%.2f,%.1f/%.1f, (%.2f)", bId, mat,
							amount, limitWin, limitPlace, tk);
						logger.info(info);
					}

				}
				tx.commit();
			}
			if (tm1 != 0) {
				min = Math.min(tm1, min);
				max = Math.max(tm1, max);
			}

		}
		String minfo = String.format("Max: %,d ; Min: %,d", max, min);
		print(minfo);

		/*
		 * double txt = 10000D; double b1 = bideng.tradeTicket2(3.8, 500, 500,
		 * txt); print("bid ticket:" + (txt - b1)); double a1 =
		 * askeng.tradeTicket2(5.1, 0, 0, txt); print("ask ticket:" + (txt -
		 * a1));
		 */

	}

	private static void init() {
		askeng = AskOrderEngine.getInstance();
		bideng  = BidOrderEngine.getInstance();
		
	}

	public static void mainTest() {

		IgniteConfiguration icfg = new IgniteConfiguration();
		icfg.setMetricsLogFrequency(0);
		icfg.setPeerClassLoadingEnabled(false);
		icfg.setRebalanceThreadPoolSize(2);
		icfg.setPublicThreadPoolSize(64);
		icfg.setSystemThreadPoolSize(32);

		icfg.setClientMode(true);

		try (Ignite ignite = Ignition.start(icfg)) {
			logger = ignite.configuration().getGridLogger();

			if (RESET) {
				ignite.destroyCache("BIDORDER");
				ignite.destroyCache("ASKORDER");
			}
			init();

			if (RESET) {
//				bideng.addData();
//				askeng.addData();
			}

			System.out.println("1");
			askeng.scanQuery(true);
			bideng.scanQuery(true);

			System.out.println("2");

			askeng.deductTest();
			bideng.deducTest();

			System.out.println("3");
			long t0 = System.nanoTime();

			IgniteCache<Long, BidRecord> bCache = bideng.getCache();
			IgniteCache<Long, AskRecord> aCache = askeng.getCache();
			
			if (CLEAR) {
				aCache.clear();
				bCache.clear();
			}

			int caseNo = 6;

			if (caseNo == 1) {
				case1();
			} else if (caseNo == 2) {
				case2();
			} else if (caseNo == 3) {
				case3();
			} else if (caseNo == 4) {
				case4();
			} else if (caseNo == 5) {
				case5();
			} else if (caseNo == 6) {
				case6();
			}

			long t1 = System.nanoTime() - t0;
			print("end: taken : " + t1 / 1000_000 + " ms.");

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private static void print(String msg) {
		logger.info(msg);
		System.out.println(">>> " + msg);
	}
	
	public static void main(String[] args) {
		mainTest();
	}


}
