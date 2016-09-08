package my.own;

import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.AtomicConfiguration;


public class AskRecordFactory {

	private static final IgniteAtomicSequence bidSeqNo = init();

	private static IgniteAtomicSequence init() {
		AtomicConfiguration acfg = new AtomicConfiguration();
		acfg.setAtomicSequenceReserveSize(0);
		acfg.setBackups(1);
		Ignition.ignite().configuration().setAtomicConfiguration(acfg);

		IgniteAtomicSequence bidSeqNo = Ignition.ignite().atomicSequence("ASK_NO",
			(System.currentTimeMillis() / 1000L) * 100000, true);
		System.out.println("ASK_NO first seq value:" + bidSeqNo.get());
		return bidSeqNo;

	}

	public static AskRecord getEmptyOfferRecord() {

		return new AskRecord(bidSeqNo.getAndIncrement());
	}

	public static AskRecord genAskRecord(int race, int horse, double win, double place,
		double amount, double limitWin, double limitPlace, String uid_a, String currency, String raceType,
		String raceDate, double sgWin, double sgPlace, long date_a, int live) {

		return new AskRecord(bidSeqNo.getAndIncrement(), race, horse, win, place, amount, limitWin, limitPlace, uid_a, currency,
			raceType, raceDate, sgWin, sgPlace, date_a, live);
	}
	
	public static AskRecord makeAskRecord(long bid, int race, int horse, double win, double place,
		double amount, double limitWin, double limitPlace, String uid_a, String currency, String raceType,
		String raceDate, double sgWin, double sgPlace, long date_a, int live) {

		return new AskRecord(bid, race, horse, win, place, amount, limitWin, limitPlace, uid_a, currency,
			raceType, raceDate, sgWin, sgPlace, date_a, live);
	}
	

}
