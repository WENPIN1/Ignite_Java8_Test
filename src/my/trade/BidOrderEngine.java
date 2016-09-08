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
import java.util.concurrent.ConcurrentSkipListMap;

import javax.cache.Cache.Entry;
import javax.cache.processor.EntryProcessor;

import my.own.BidRecord;
import my.own.BidRecordEntryProcessor;
import my.own.BidRecordFactory;
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
public class BidOrderEngine {

	/** Cache name. */
	private static final String CACHE_NAME = "BIDORDER";
	// private static final String QUEUE_NAME = ; //"20160810|3H|3|2|B|V|WP";

	/** Number of keys. */
	private static final int BID_COUNT =52021;
	private static final int LOOP = 2000;
	private static final boolean IS_RESET = true; //Boolean.getBoolean("reset");
	private static final boolean IS_EXIT = false;
	
	@LoggerResource
	private static IgniteLogger logger ;
	private static IgniteAtomicSequence bidSeqNo ;
	private IgniteCache<Long, BidRecord> bCache = null; // .withKeepBinary();
	private Ignite ignite;
	
	private EntryProcessor<Long, BidRecord, BidRecord> ep = null;
	private String sql = "select bid from BidRecord where (amount >=? and (LIMITWIN <=? and LIMITPLACE <=?))"
		+ " and (LIVE =? and WIN > 0 and PLACE > 0) LIMIT ?";
	private SqlFieldsQuery query = null;

	private String oql = "(amount >=? and (LIMITWIN <=? and LIMITPLACE <=?))"
		+ " and (LIVE =? and WIN > 0 and PLACE > 0) LIMIT ?";

//	private SqlQuery<Long, BidRecord> objQuery = new SqlQuery<Long, BidRecord>(BidRecord.class, oql);

	
	private BidOrderEngine() {
		this.ignite = Ignition.ignite();
		init();
	}
	
	public static BidOrderEngine getInstance() {
		return new BidOrderEngine();
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

		try (IgniteDataStreamer<Long, BidRecord> stmr = ignite.dataStreamer(bCache.getName())) {
			// Stream entries.
			stmr.allowOverwrite(true);
			long t01 = System.currentTimeMillis();
			for (int i = 0; i < BID_COUNT; i++) {

				int race = 1;
				int horse = 1;
				double win, place;
				win = place = 1; // + random.nextInt(5)
				double amount = BidRule.AMOUNT_SEGMENT[random.nextInt(BidRule.AMOUNT_SEG_SIZE)];
				double limitWin = 5 + random.nextInt(59) *5;
				double limitPlace = 5 + random.nextInt(19) *5;
				limitPlace = Math.min(limitWin, limitPlace);
				String uid_a = "abcd" + String.valueOf(i % 100);
				String currency = "SG";
				String raceType = "3H";
				String raceDate = "2016-08-12";
				double sgWin = 0;
				double sgPlace = 0;
				long date_a = System.currentTimeMillis();

				BidRecord b = BidRecordFactory.genBidRecord(race, horse, win, place, amount, limitWin,
					limitPlace, uid_a, currency, raceType, raceDate, sgWin, sgPlace, date_a, 0);

				stmr.addData(b.getBid(), b);
			}
			long t02 = System.currentTimeMillis();

			if (true) {
				String info01 = String.format(">Bid Initialising completed !. (%,d) ms.", t02 - t01);
				print(info01);
			}

		}
	}



	public void deducTest() {
		tradeTicket(3.8, 300, 300, 0);
	}
	
	
	
	public IgniteCache<Long, BidRecord> getCache() {
		return bCache;
	}

	public void init() throws IgniteException {
		try {
			logger = ignite.configuration().getGridLogger();
			
			print("=== Test for BidOrderEngine ===");
			//
			print("> BidOrderEngine started.");

			CacheConfiguration<Long, BidRecord> cfg = new CacheConfiguration<>();

//			cfg.setCacheMode(CacheMode.REPLICATED); // GOOD
			cfg.setName(CACHE_NAME);
			cfg.setMemoryMode(CacheMemoryMode.OFFHEAP_TIERED);
			//cfg.setMemoryMode(CacheMemoryMode.ONHEAP_TIERED);
			//cfg.setEvictionPolicy(new FifoEvictionPolicy<Long, BidRecord>(1));
//			cfg.setOffHeapMaxMemory(10_000_000_000L);
			// cfg.setMemoryMode(CacheMemoryMode.ONHEAP_TIERED);
			// cfg.setEvictionPolicy(new RandomEvictionPolicy(100_000));
			cfg.setSqlOnheapRowCacheSize(100_000);
			cfg.setBackups(1);
			cfg.setIndexedTypes(Long.class, BidRecord.class, Double.class, BidRecord.class, String.class,
				BidRecord.class, Integer.class, BidRecord.class);
			// cfg.setStatisticsEnabled(true);
			// cfg.setManagementEnabled(true);
			cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
			cfg.setStartSize(150000);
//			cfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
			cfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC); 
			cfg.setCacheMode(CacheMode.PARTITIONED); // TEST --> GOOD
			
//			cfg.setCopyOnRead(false);

			NearCacheConfiguration<Long, BidRecord> nearCfg = new NearCacheConfiguration<>();
			bCache = ignite.getOrCreateCache(cfg, nearCfg);
//			ep = new BidRecordEntryProcessor();
			query = new SqlFieldsQuery(sql);
			//query.setPageSize(50_000);

			AtomicConfiguration acfg = ignite.configuration().getAtomicConfiguration();
			acfg.setAtomicSequenceReserveSize(1000);
			acfg.setBackups(1);
			ignite.configuration().setAtomicConfiguration(acfg);

//			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
//			String dateText = sdf.format(new Date());
//			Long dateLong = Long.parseLong(dateText);

//			bidSeqNo = ignite.atomicSequence("BID_NO", dateLong * 100_000_000, true);
			bidSeqNo = ignite.atomicSequence("BID_NO", 1 * 100_000_000, true);
			print("Get first Bid Sequence value:" + bidSeqNo.get());

			// Auto-close cache at the end of the example.
			/*CollectionConfiguration qcfg = new CollectionConfiguration();
			qcfg.setMemoryMode(CacheMemoryMode.OFFHEAP_VALUES);
			qcfg.setOffHeapMaxMemory(0);
			qcfg.setBackups(1);
			qcfg.setCacheMode(CacheMode.PARTITIONED);*/

		}

    	catch(Exception e) {
    		logger.warning(e.toString());
    	}

	}

	public List<Entry<Long, BidRecord>> matchObjectQuery(double amount, double limitWin, double limitPlace,
		int sizeLimit) {

		SqlQuery<Long, BidRecord> objQuery = new SqlQuery<Long, BidRecord>(BidRecord.class, oql);

		objQuery.setArgs(amount, limitWin, limitPlace, 0, sizeLimit);

		try (QueryCursor<Entry<Long, BidRecord>> cursor = bCache.query(objQuery)) {
			return cursor.getAll();
		}

	}


	public List<List<?>> matchQuery(double amount, double limitWin, double limitPlace, int sizeLimit) {
	
//		SqlFieldsQuery query = new SqlFieldsQuery(sql);
	
		query.setArgs(amount, limitWin, limitPlace, 0, sizeLimit);

		
		try (QueryCursor<List<?>> cursor = bCache.query(query))
		{
			return cursor.getAll();
		}
	
		
	}

	public	void resetData() {
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

					BidRecord b = BidRecordFactory.genBidRecord(race, horse, win, place, amount, limitWin, limitPlace, uid_a, currency, raceType, raceDate, sgWin, sgPlace, date_a, 0);
					boolean b0 = bCache.putIfAbsent(b.getBid(), b);
					if (! b0) {
						print("BID _key overlapped! " + b.getBid());
					}
					
				}
				long t02 = System.currentTimeMillis();
				
				if (IS_RESET) {
					String info01 = String.format("> Initialising completed !. (%,d) ms.", t02-t01);
					print(info01);
				}

			} catch(Exception e) {

			}
	}

	//TODO unable to change scan query
	public void scanQuery(boolean print) {
		// String CACHE_NAME = "20160810|3H|3|2|B|V|WP";
		IgniteCache<Long, BidRecord> cache = bCache; // Ignition.ignite().cache(CACHE_NAME);
														// // .withKeepBinary();
		String x = "abcd12";

		@SuppressWarnings("unchecked")
		ScanQuery<Long, BidRecord> qry = new ScanQuery<Long, BidRecord>(); //(k, v) -> v.getUid_a().equals(x));
		try (QueryCursor<Entry<Long, BidRecord>> cur = cache.query(qry)) {
			List<Entry<Long, BidRecord>> rows = cur.getAll();
			if (print)
				print("Scan match size: " + rows.size());
		}

		ScanQuery<Long, BidRecord> qry1 = new ScanQuery<Long, BidRecord>(); //(k, v) -> (!v.getUid_a().equals(x)));
		try (QueryCursor<Entry<Long, BidRecord>> cur = cache.query(qry1)) {
			List<Entry<Long, BidRecord>> rows = cur.getAll();
			if (print)
				print("Scan not match size: " + rows.size());
		}

		long t0 = System.nanoTime();

		ConcurrentSkipListMap<Long, Double> chm = new ConcurrentSkipListMap<Long, Double>();


		List<Entry<Long, BidRecord>> rows = null;
		try (QueryCursor<Entry<Long, BidRecord>> cur = cache.query(new ScanQuery<Long, BidRecord>())) {
			rows = cur.getAll();
		}


		for(Entry<Long, BidRecord> e : rows) {
			BidRecord v = e.getValue();
			Double d1 = v.getWin();
			Long bLevel = v.getBidLevel();
			Double d = chm.putIfAbsent(bLevel, d1);
			if (d != null) {
				d1 = d + d1;
				boolean success = chm.replace(bLevel, d, d1);
				if (!success)
					System.out.println("fail !!! " + bLevel + ", " + d + ", " + d1);
			}

		}

		long t1 = System.nanoTime() - t0;
		if (print) {
			print("cost : " + t1 / 1000 + " us. size: " + chm.size());
			print(chm.toString().replace(',', '\n'));
		}
	}

	/**
	 * @param bCache
	 */
	public double tradeTicket(double amount, double limitWin, double limitPlace, double ticketAsk) {
		// individual removal in 1 tx ==> faster

		long t0 = System.nanoTime();

		List<Entry<Long, BidRecord>> objList = matchObjectQuery(amount, limitWin, limitPlace, (int) ticketAsk);

		long t1 = System.nanoTime();

		double ticketAskAfter = ticketAsk;
		if (objList.size() > 0) {
			List<String> tradeList = new ArrayList<>();
			for (Entry<Long, BidRecord> e : objList) {
				Long recordId = (Long) e.getKey();
				BidRecord bBefore = bCache.invoke(recordId, ep, ticketAskAfter);

				if (bBefore == null)
					continue;

				double tickLast = bBefore.getWin(); // WIN+PLACE
				double ticketTx = Math.min(ticketAskAfter, tickLast);
				ticketAskAfter -= ticketTx;

				tradeList.add(String.format("#%d$%.2f,%.1f/%.1f;%.2f", recordId, bBefore.getAmount(),
					bBefore.getLimitWin(), bBefore.getLimitPlace(), ticketTx));
				if (ticketAskAfter == 0)
					break;
			}

			if (tradeList.size() > 0) {
				long t2 = System.nanoTime();
				logger.info(String.format(
					"MatchB bid items: %,d taken %,d + %,d us. %s for %,d trades & %,d cache size",
					objList.size(), (t1 - t0) / 1000, (t2 - t1) / 1000, Arrays.toString(tradeList.toArray()),
					tradeList.size(), bCache.size()));
			}
		}

		return ticketAskAfter;

	}

	/**
	 * @param bCache
	 */
	public double tradeTicket1(double amount, double limitWin, double limitPlace, double ticketAsk) {
		// individual removal in 1 tx ==> faster

		long t0 = System.nanoTime();

		List<List<?>> rl = matchQuery(amount, limitWin, limitPlace, (int) ticketAsk);

		long t1 = System.nanoTime();

		double ticketAskAfter = ticketAsk;
		List<String> tradeList = new ArrayList<>();
		if (rl.size() > 0) {

			// try (Transaction tx = Ignition.ignite().transactions().txStart())
			// {
			for (List<?> l : rl) {
				BidRecord b = bCache.getAndRemove((Long) l.get(0));

				if (b == null)
					continue;

				Long recordId = b.getBid();
				double ticketBid = b.getWin(); // WIN+PLACE
				double ticketTx = Math.min(ticketAskAfter, ticketBid);

				ticketAskAfter -= ticketTx;
				ticketBid -= ticketTx;

				if (ticketBid > 0) {
					b.setWin(ticketBid);
					b.setPlace(ticketBid);
					b.setDateA(System.currentTimeMillis());
					boolean a0 = bCache.putIfAbsent(recordId, b);
					if (!a0) {
						logger.warning("Not absent on putting recordId:" + recordId);
					}
				}

				tradeList.add(String.format("#%d$%.2f,%.1f/%.1f;%.2f", recordId, b.getAmount(),
					b.getLimitWin(), b.getLimitPlace(), ticketTx));

				if (ticketAskAfter == 0)
					break;
				// tx.commit();
			}
			if (tradeList.size() > 0) {
				long t2 = System.nanoTime();
				logger.info(String.format(
					"BMatch bid items: %,d taken %,d + %,d us. %s for %,d trades & %,d cache size",
					rl.size(), (t1 - t0) / 1000, (t2 - t1) / 1000, Arrays.toString(tradeList.toArray()),
					tradeList.size(), bCache.size()));
			}
		}

		return ticketAskAfter;

	}

	/**
	 * @param bCache
	 */
	public double tradeTicket2(double amount, double limitWin, double limitPlace, final double ticketAsk) {
		// individual removal in 1 tx ==> faster
	
		long t0 = System.nanoTime();
	
		List<Entry<Long, BidRecord>> rl = matchObjectQuery(amount, limitWin, limitPlace, (int) ticketAsk);
	
		long t1 = System.nanoTime();
	
		double ticketAskAfter = ticketAsk;
		List<String> tradeList = new ArrayList<>();
		if (rl.size() > 0) {
	
			// try (Transaction tx = Ignition.ignite().transactions().txStart())
			// {
			for (Entry<Long, BidRecord> entry : rl) {
				BidRecord b = bCache.getAndRemove(entry.getKey());
	
				if (b == null)
					continue;
	
				Long recordId = b.getBid();
				double ticketBid = b.getWin(); // WIN+PLACE
				double ticketTx = Math.min(ticketAskAfter, ticketBid);
	
				ticketAskAfter -= ticketTx;
				ticketBid -= ticketTx;
	
				if (ticketBid > 0) {
					b.setWin(ticketBid);
					b.setPlace(ticketBid);
					b.setDateA(System.currentTimeMillis());
					boolean a0 = bCache.putIfAbsent(recordId, b);
					if (!a0) {
						logger.warning("Not absent on putting recordId:" + recordId);
					}
				}
	
				tradeList.add(String.format("#%d$%.2f,%.1f/%.1f;%.2f", recordId, b.getAmount(),
					b.getLimitWin(), b.getLimitPlace(), ticketTx));
	
				if (ticketAskAfter == 0)
					break;
				// tx.commit();
			}
			if (tradeList.size() > 0) {
				long t2 = System.nanoTime();
				logger.info(String.format(
					"BMatch bid items: %,d taken %,d + %,d us. %s for %,d trades & %,d cache size",
					rl.size(), (t1 - t0) / 1000, (t2 - t1) / 1000, Arrays.toString(tradeList.toArray()),
					tradeList.size(), bCache.size()));
			}
		}
	
		return ticketAskAfter;
	
	}
}
