package my.own;

import java.io.Serializable;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;


public class BidRecordEntryProcessor implements EntryProcessor<Long, BidRecord, BidRecord>, Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3989499091824672679L;




	public BidRecordEntryProcessor() {

	}



	@Override
	public BidRecord process(MutableEntry<Long, BidRecord> entry, Object... args)
		throws EntryProcessorException {
		if (args.length < 1)
			return null;
		double tradingTicket = (double) args[0];
		final int modeWinPlace = (args.length == 2) ? (int) args[1] : 0;
		BidRecord bOld = entry.getValue();
		long currentTimeMillis = System.currentTimeMillis();
		
		if (bOld != null) {
			bOld.setDateA(currentTimeMillis);
			double tk = (modeWinPlace == 2) ? bOld.getPlace() : bOld.getWin();

			double remaing = tk - tradingTicket;

			if (remaing <= 0) {
				entry.remove();			
			
			} else {
				BidRecord bNow = entry.getValue();
				bNow.setDateA(currentTimeMillis);
				switch (modeWinPlace) {
					case 1 :
						bNow.setWin(remaing);
						break;
					case 2 :
						bNow.setPlace(remaing);
						break;
					default :
						bNow.setWin(remaing);
						bNow.setPlace(remaing);
				}
				entry.setValue(bNow);
			}
		}

		return bOld;

	}

}
