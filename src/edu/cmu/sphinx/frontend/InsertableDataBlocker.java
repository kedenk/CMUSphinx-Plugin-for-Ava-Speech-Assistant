package edu.cmu.sphinx.frontend;

import java.util.LinkedList;
import java.util.List;

public class InsertableDataBlocker extends BaseDataProcessor{

	private final int endSignalDuration = 100; 
	
	List<Data> insertionDatas = new LinkedList<Data>();
	
	
	@Override
	public Data getData() throws DataProcessingException {
		
        if (!insertionDatas.isEmpty())
        {
            insertionDatas.remove(0);
            try {
				throw new InterruptedException();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return getPredecessor().getData();
	}
	
	
    public void injectInterrupt()
    {
        insertionDatas.add(new DataEndSignal(this.endSignalDuration));
    }

}
