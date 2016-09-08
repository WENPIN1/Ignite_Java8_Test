package my.own;

import java.io.Serializable;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;


public class AskRecordEntryProcessor implements EntryProcessor<Long, AskRecord, AskRecord>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3989499091824672679L;

	public AskRecordEntryProcessor() {

	}

	@Override
	public AskRecord process(MutableEntry<Long, AskRecord> entry, Object... args)
		throws EntryProcessorException {
		if (args.length < 1)
			return null;
		double tradingTicket = (double) args[0];
		int modeWinPlace = (args.length == 2) ? (int) args[1] : 0;
		AskRecord oOld = entry.getValue();
		AskRecord oNow = oOld;
		
		if (oOld != null) {
			oNow.setDateA(System.currentTimeMillis());
			double tk = (modeWinPlace == 2) ? oOld.getPlace() : oOld.getWin();

			double remaing = tk - tradingTicket;

			if (remaing <= 0) {
				entry.remove();	
			
			} else {
				switch (modeWinPlace) {
					case 1 :
						oNow.setWin(remaing);
						break;
					case 2 :
						oNow.setPlace(remaing);
						break;
					default :
						oNow.setWin(remaing);
						oNow.setPlace(remaing);
				}
				oOld = entry.getValue();
				entry.setValue(oNow);
			}
		}

		return oOld;

	}

}
