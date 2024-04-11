import java.util.ArrayList;
import java.util.List;

public class BufHashTbl {
    private final List<List<BufTblRecord>> records; //Forced to make double list of lists because java doesn't support arrays of generics for some dumb reason
    private final int tableSize = 10;

    public BufHashTbl() {
        records = new ArrayList<>();
        for (int i = 0; i < tableSize; i++) {
            records.add(new ArrayList<>());
        }
    }

    public void insert(int pageNum, int frameNum) {
        BufTblRecord newRecord = new BufTblRecord(pageNum, frameNum);
        if (pageNum < this.tableSize)
            this.records.get(pageNum).add(newRecord);
    }

    public int lookup(int pageNum) {
        // your code goes here
        for (int i = 0; i < this.tableSize; i++) {
            for (int j = 0; j < this.records.get(i).size(); j++) {
                if (this.records.get(i).get(j).pageNum == pageNum) {
                    return this.records.get(i).get(j).frameNum;
                }
            }
        }
        return -1;  // you need to change the returned value
    }

    public boolean remove(int pageNum, int frameNum) {
        // your code goes here
        for (int i = 0; i < this.tableSize; i++) {
            for (int j = 0; j < this.records.get(i).size(); j++) {
                if (this.records.get(i).get(j).pageNum == pageNum && this.records.get(i).get(j).frameNum == frameNum) {
                    this.records.get(i).remove(j);
                    return true; // you need to change the returned value
                }
            }
        }
        return false;
    }

    private static class BufTblRecord {
        public int pageNum;
        public int frameNum;
        public BufTblRecord (int pageNum, int frameNum) {
            this.pageNum = pageNum;
            this.frameNum = frameNum;
        }
    }
}
