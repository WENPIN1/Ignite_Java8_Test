package my.own;

import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.AtomicConfiguration;


public class BidRecordFactory {

	private static final IgniteAtomicSequence bidSeqNo = init();

	private static IgniteAtomicSequence init() {
		AtomicConfiguration acfg = new AtomicConfiguration();
		acfg.setAtomicSequenceReserveSize(0);
		acfg.setBackups(1);
		Ignition.ignite().configuration().setAtomicConfiguration(acfg);

		IgniteAtomicSequence bidSeqNo = Ignition.ignite().atomicSequence("BID_NO",
			(System.currentTimeMillis() / 1000L) * 100000, true);
		System.out.println("BID_NO first seq value:" + bidSeqNo.get());
		return bidSeqNo;

	}

	public static BidRecord getEmptyBidRecord() {

		return new BidRecord(bidSeqNo.getAndIncrement());
	}

	public static BidRecord genBidRecord(int race, int horse, double win, double place,
		double amount, double limitWin, double limitPlace, String uid_a, String currency, String raceType,
		String raceDate, double sgWin, double sgPlace, long date_a, int live) {

		return new BidRecord(bidSeqNo.getAndIncrement(), race, horse, win, place, amount, limitWin, limitPlace, uid_a, currency,
			raceType, raceDate, sgWin, sgPlace, date_a, live);
	}
	
	public static BidRecord makeBidRecord(long bid, int race, int horse, double win, double place,
		double amount, double limitWin, double limitPlace, String uid_a, String currency, String raceType,
		String raceDate, double sgWin, double sgPlace, long date_a, int live) {

		return new BidRecord(bid, race, horse, win, place, amount, limitWin, limitPlace, uid_a, currency,
			raceType, raceDate, sgWin, sgPlace, date_a, live);
	}
	

}
