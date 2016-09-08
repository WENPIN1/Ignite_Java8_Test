/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package my.trade;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.cache.Cache.Entry;
import javax.cache.processor.EntryProcessor;

import my.own.AskRecord;
import my.own.AskRecordEntryProcessor;
import my.own.AskRecordFactory;
import my.util.BidRule;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMemoryMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.NearCacheConfiguration;
import org.apache.ignite.resources.LoggerResource;

/**
 * This example demonstrates the simplest code that populates the distributed
 * cache and co-locates simple closure execution with each key. The goal of this
 * particular example is to provide the simplest code example of this logic.
 * <p>
 * Remote nodes should always be started with special configuration file which
 * enables P2P class loading: {@code 'ignite. sh|bat}
 * examples/config/example-ignite.xml'}.
 * <p>
 */
public final class AskOrderEngine {

	/** Cache name. */
	private static final String CACHE_NAME = "ASKORDER";
	// private static final String QUEUE_NAME = ; //"20160810|3H|3|2|B|V|WP";

	/** Number of keys. */
	private static final int BID_COUNT =52021;
	private static final int LOOP = 2000;
	private static final boolean IS_RESET = true; //Boolean.getBoolean("reset");
	private static final boolean IS_EXIT = false;
	
	@LoggerResource
	private static IgniteLogger logger ;

	private static IgniteAtomicSequence orderSeqNo ;
	
	private IgniteCache<Long, AskRecord> aCache;

	
	private Ignite ignite = null;
	
	private EntryProcessor<Long, AskRecord, AskRecord> ep = null;
	
	private String sql = "select bid from AskRecord where (amount <=? and (LIMITWIN >=? and LIMITPLACE >=?))"
		+ " and (LIVE =? and WIN > 0 and PLACE > 0) LIMIT ?";
	private SqlFieldsQuery query = null; //new SqlFieldsQuery(sql);

	private String oql = "(amount <=? and (LIMITWIN >=? and LIMITPLACE >=?))"
		+ " and (LIVE =? and WIN > 0 and PLACE > 0) LIMIT ?";

//	private SqlQuery<Long, AskRecord> objQuery = new SqlQuery<Long, AskRecord>(AskRecord.class, oql);

	
	private AskOrderEngine() {
		this.ignite = Ignition.ignite();
		init();
	}
	
	public static AskOrderEngine getInstance() {
		return  new AskOrderEngine();
	}

	/**
	 * Prints query results.
	 *
	 * @param col
	 *            Query results.
	 */
	private static void print(Iterable<?> col) {
		for (Object next : col)
			System.out.println(">>>     " + next);
	}
	
	/**
	 * Prints message.
	 *
	 * @param msg
	 *            Message to print before all objects are printed.
	 */
	private static void print(String msg) {
		logger.info(msg);
		System.out.println(">>> " + msg);
	}
	
	/**
	 * Prints message and query results.
	 *
	 * @param msg
	 *            Message to print before all objects are printed.
	 * @param col
	 *            Query results.
	 */
	private static void print(String msg, Iterable<?> col) {
		print(msg);
		print(col);
	}
	
	public void addData() {

		Random random = new Random();

		try (IgniteDataStreamer<Long, AskRecord> stmr = ignite.dataStreamer(aCache.getName())) {
			// Stream entries.
			stmr.allowOverwrite(true);
			long t01 = System.currentTimeMillis();
			for (int i = 0; i < BID_COUNT; i++) {

				int race = 1;
				int horse = 1;
				double win, place;
				win = place = 1 ; //+ random.nextInt(5);
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

				AskRecord obj = AskRecordFactory.genAskRecord(race, horse, win, place, amount, limitWin,
					limitPlace, uid_a, currency, raceType, raceDate, sgWin, sgPlace, date_a, 0);

				stmr.addData(obj.getBid(), obj);
			}
			long t02 = System.currentTimeMillis();

			if (true) {
				String info01 = String.format(">Ask Initialising completed !. (%,d) ms.", t02 - t01);
				print(info01);
			}

		}
	}



	public void deductTest() {

		tradeTicket(5.1, 0, 0, 0);
	}
	
	
	
	public IgniteCache<Long, AskRecord> getCache() {
		return aCache;
	}

	public void init() throws IgniteException {
		try {
			logger = ignite.configuration().getGridLogger();

			print("=== Test for askOrderEngine ===");
			//
			print("> askOrderEngine started.");

			CacheConfiguration<Long, AskRecord> cfg = new CacheConfiguration<>();

//			cfg.setCacheMode(CacheMode.REPLICATED); // GOOD
			cfg.setName(CACHE_NAME);
			cfg.setMemoryMode(CacheMemoryMode.OFFHEAP_TIERED);
//			cfg.setOffHeapMaxMemory(10_000_000_000L);
			//cfg.setEvictionPolicy(new FifoEvictionPolicy<Long, AskRecord>(1));
			cfg.setSqlOnheapRowCacheSize(100_000);  // not too much affect
			cfg.setBackups(1);
			cfg.setIndexedTypes(Long.class, AskRecord.class, Double.class, AskRecord.class, String.class,
				AskRecord.class, Integer.class, AskRecord.class);
			// cfg.setStatisticsEnabled(true);
			// cfg.setManagementEnabled(true);
			cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
			cfg.setStartSize(150_000);
//			cfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);  // important for match integrity
			cfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC); 
			cfg.setCacheMode(CacheMode.PARTITIONED); // TEST --> GOOD
//			cfg.setCopyOnRead(false);
			
			NearCacheConfiguration<Long, AskRecord> nearCfg = new NearCacheConfiguration<>();
			aCache = ignite.getOrCreateCache(cfg, nearCfg);
//			ep = new AskRecordEntryProcessor();
			query = new SqlFieldsQuery(sql);
			//query.setPageSize(50_000);

			AtomicConfiguration acfg = ignite.configuration().getAtomicConfiguration();
			acfg.setAtomicSequenceReserveSize(1000);
			acfg.setBackups(1);
			ignite.configuration().setAtomicConfiguration(acfg);

			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
			String dateText = sdf.format(new Date());
			Long dateLong = Long.parseLong(dateText);

//			orderSeqNo = ignite.atomicSequence("ASK_NO", dateLong * 100_000_000, true);
			orderSeqNo = ignite.atomicSequence("ASK_NO", 2 * 100_000_000, true);
			print("Get first ASK sequence value:" + orderSeqNo.get());

			// Auto-close cache at the end of the example.
			/*CollectionConfiguration qcfg = new CollectionConfiguration();
			qcfg.setMemoryMode(CacheMemoryMode.OFFHEAP_VALUES);
			qcfg.setOffHeapMaxMemory(0);
			qcfg.setBackups(1);
			qcfg.setCacheMode(CacheMode.PARTITIONED);*/
		} catch (Exception e) {
			logger.warning(e.toString());
		}
	}

	public List<Entry<Long, AskRecord>> matchObjectQuery(double amount, double limitWin, double limitPlace,
		int sizeLimit) {

		SqlQuery<Long, AskRecord> objQuery = new SqlQuery<Long, AskRecord>(AskRecord.class, oql);

		objQuery.setArgs(amount, limitWin, limitPlace, 0, sizeLimit);

		try (QueryCursor<Entry<Long, AskRecord>> cursor = aCache.query(objQuery)) {
			return cursor.getAll();
		}
	}


	public List<List<?>> matchQuery(double amount, double limitWin, double limitPlace, int sizeLimit) {
	
		// TODO reuse SqlFieldsQuery
//		SqlFieldsQuery query = new SqlFieldsQuery(sql);
	
		query.setArgs(amount, limitWin, limitPlace, 0, sizeLimit);

		try (QueryCursor<List<?>> cursor = aCache.query(query))
		{
			return cursor.getAll();
		}
	}

	public void resetData() {
			int loop_count = (IS_RESET) ? BID_COUNT: 0;
//			if (IS_RESET) {
//				ignite.destroyCache(CACHE_NAME);
//				print(CACHE_NAME + " has been destroyed.");
//			}

			try {

				long t01 = System.currentTimeMillis();
				for (int i = 0; i < loop_count; i++) {
					int race = 1 ; 
					int horse = 1 ; 
					double win , place;
					win = place = 1 ; //+ (int ) (Math.random() * 10);
					double amount = BidRule.AMOUNT_SEGMENT[(int) (Math.random() * BidRule.AMOUNT_SEG_SIZE)] ;
					double limitWin = 5.1 + (int) (Math.random() * 295);
					double limitPlace = 5  + (int) (Math.random() * 95);
					limitPlace = Math.min(limitWin, limitPlace);
					String uid_a = "abcd" + String.valueOf(i%100); 
					String currency = "SG"; 
					String raceType = "3H"; 
					String raceDate = "2016-08-12" ; 
					double sgWin = 0;
					double sgPlace = 0; 
					long date_a	= System.currentTimeMillis();				
//					long k = BetUtil.formatWinPlcLimitKey(amount, limitWin, limitPlace);

					AskRecord b = AskRecordFactory.genAskRecord(race, horse, win, place, amount, limitWin, limitPlace, uid_a, currency, raceType, raceDate, sgWin, sgPlace, date_a, 0);
					boolean b0 = aCache.putIfAbsent(b.getBid(), b);
					if (! b0) {
						print("ASK ID overlapped! " + b.getBid());
					}
					
				}
				long t02 = System.currentTimeMillis();
				
				if (IS_RESET) {
					String info01 = String.format("> Reset completed !. (%,d) ms.", t02-t01);
					print(info01);
				}
			} finally {

			}
	}
	
	public void scanQuery(boolean print) {
		//String CACHE_NAME = "20160810|3H|3|2|B|V|WP";
		IgniteCache<Long, AskRecord> cache = aCache; //Ignition.ignite().cache(CACHE_NAME); // .withKeepBinary();
		String x = "abcd12";

		@SuppressWarnings("unchecked")
		ScanQuery<Long, AskRecord> qry = new ScanQuery<Long, AskRecord>(); //(k, v) -> v.getUid_a().equals(x));
		try (QueryCursor<Entry<Long, AskRecord>> cur = cache.query(qry)) {
			List<Entry<Long, AskRecord>> rows = cur.getAll();
			if (print) print("Scan match size: " + rows.size());
		}
		ScanQuery<Long, AskRecord> qry1 = new ScanQuery<Long, AskRecord>();//(k, v) -> (!v.getUid_a().equals(x)));
		try (QueryCursor<Entry<Long, AskRecord>> cur = cache.query(qry1)) {
			List<Entry<Long, AskRecord>> rows = cur.getAll();
			if (print) print("Scan not match size: " + rows.size());
		}
	}

	/**
	 * @param aCache
	 */
	public double tradeTicket(double amount, double limitWin, double limitPlace, double ticketBid) {
		// individual removal in 1 tx ==> faster

		long t0 = System.nanoTime();

		List<Entry<Long, AskRecord>> objList = matchObjectQuery(amount, limitWin, limitPlace, (int) ticketBid);

		long t1 = System.nanoTime();

		double ticketBidAfter = ticketBid;
		if (objList.size() > 0) {
			List<String> tradeList = new ArrayList<>();
			for (Entry<Long, AskRecord> e : objList) {
				Long recordId = e.getKey();
				AskRecord aBefore = aCache.invoke(recordId, ep, ticketBidAfter);

				if (aBefore == null)
					continue;

				double ticketAsk = aBefore.getWin();
				double ticketTx = Math.min(ticketAsk, ticketBidAfter);

				ticketBidAfter -= ticketTx;

				tradeList.add(String.format("#%d$%.02f,%.01f/%.01f;%.2f", recordId, aBefore.getAmount(),
					aBefore.getLimitWin(), aBefore.getLimitPlace(), ticketTx));
				if (ticketBidAfter == 0)
					break;
			}

			if (tradeList.size() > 0) {
				long t2 = System.nanoTime();
				logger.info(String.format(
					"MatchA ask items: %,d taken %,d + %,d us. %s for %,d trades & %,d cache size",
					objList.size(), (t1 - t0) / 1000, (t2 - t1) / 1000, Arrays.toString(tradeList.toArray()),
					tradeList.size(), aCache.size()));
			}
		}

		return ticketBidAfter;

	}

	/**
	 * @param oCache
	 */
	/**
	 * @param amount
	 * @param limitWin
	 * @param limitPlace
	 * @param ticketBid
	 * @return
	 */
	public double tradeTicket1(double amount, double limitWin, double limitPlace, double ticketBid) {
		// individual removal in 1 tx ==> faster

		long t0 = System.nanoTime();

		List<List<?>> rl = matchQuery(amount, limitWin, limitPlace, (int) ticketBid);

		long t1 = System.nanoTime();

		if (rl.size() > 0) {
			List<String> tradeList = new ArrayList<>();

			// try (Transaction tx = Ignition.ignite().transactions().txStart())
			// {
			for (List<?> l : rl) {
				// long bid = (Long) l.get(0);
				// l.clear();
				AskRecord a = aCache.getAndRemove((Long) l.get(0));

				if (a == null)
					continue;

				Long recordId = a.getBid();
				double ticketAsk = a.getWin();
				double ticketTx = Math.min(ticketAsk, ticketBid);

				ticketAsk -= ticketTx;
				ticketBid -= ticketTx;

				if (ticketAsk > 0) {
					a.setWin(ticketAsk);
					a.setPlace(ticketAsk);
					a.setDateA(System.currentTimeMillis());
					boolean a0 = aCache.putIfAbsent(recordId, a);
					assert a0 : "putIfAbsent on putting ask(" + recordId + "):" + a0;
				}

				tradeList.add(String.format("%d#%.2f,%.1f/%.1f;%.2f", recordId, a.getAmount(),
					a.getLimitWin(), a.getLimitPlace(), ticketTx));

				if (ticketBid == 0)
					break;
			}
			// tx.commit();
			// }
			if (tradeList.size() > 0) {
				long t2 = System.nanoTime();
				logger.info(String.format(
					"AMatch ask items: %,d takes %,d + %,d us. %s for %,d trades & %,d (size)", rl.size(),
					(t1 - t0) / 1000, (t2 - t1) / 1000, Arrays.toString(tradeList.toArray()),
					tradeList.size(), aCache.size()));
				tradeList.clear();
			}
			rl.clear();
		}

		return ticketBid;

	}

	/**
	 * @param oCache
	 */
	/**
	 * @param amount
	 * @param limitWin
	 * @param limitPlace
	 * @param ticketBid
	 * @return
	 */
	public double tradeTicket2(double amount, double limitWin, double limitPlace, double ticketBid) {
		// individual removal in 1 tx ==> faster
	
		long t0 = System.nanoTime();
	
		List<Entry<Long, AskRecord>> rl = matchObjectQuery(amount, limitWin, limitPlace, (int) ticketBid);
	
		long t1 = System.nanoTime();
	
		if (rl.size() > 0) {
			List<String> tradeList = new ArrayList<>();
	
			// try (Transaction tx = Ignition.ignite().transactions().txStart())
			// {
			for (Entry<Long, AskRecord> l : rl) {
				// long bid = (Long) l.get(0);
				// l.clear();
				AskRecord a = aCache.getAndRemove(l.getKey());
	
				if (a == null)
					continue;
	
				Long recordId = a.getBid();
				double ticketAsk = a.getWin();
				double ticketTx = Math.min(ticketAsk, ticketBid);
	
				ticketAsk -= ticketTx;
				ticketBid -= ticketTx;
	
				if (ticketAsk > 0) {
					a.setWin(ticketAsk);
					a.setPlace(ticketAsk);
					a.setDateA(System.currentTimeMillis());
					boolean a0 = aCache.putIfAbsent(recordId, a);
					assert a0 : "putIfAbsent on putting ask(" + recordId + "):" + a0;
				}
	
				tradeList.add(String.format("%d#%.2f,%.1f/%.1f;%.2f", recordId, a.getAmount(),
					a.getLimitWin(), a.getLimitPlace(), ticketTx));
	
				if (ticketBid == 0)
					break;
			}
			// tx.commit();
			// }
			if (tradeList.size() > 0) {
				long t2 = System.nanoTime();
				logger.info(String.format(
					"AMatch ask items: %,d takes %,d + %,d us. %s for %,d trades & %,d (size)", rl.size(),
					(t1 - t0) / 1000, (t2 - t1) / 1000, Arrays.toString(tradeList.toArray()),
					tradeList.size(), aCache.size()));
				tradeList.clear();
			}
			rl.clear();
		}
	
		return ticketBid;
	
	}
	
}
