import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BufMgr {
    private BufHashTbl bufTbl;
    private Frame[] pool;
    private int poolSize;
    private List<Integer> lruQueue;
    private int used = 0;

    public BufMgr(int poolSize) {
        bufTbl = new BufHashTbl();
        this.poolSize = poolSize;
        this.pool = new Frame[poolSize];
        lruQueue = new ArrayList<>();

        for (int i = 0; i < poolSize; i++) {
            pool[i] = new Frame(null);
        }

    }
    public void pin(int pageNum) {
        int frameNum = bufTbl.lookup(pageNum);
        if (frameNum != -1) {
            pool[frameNum].incPin();
            System.out.println("page " + pageNum + " which is stored in frame " + frameNum + " pin count: " + this.pool[frameNum].getPin() + " which is "  + ((this.pool[frameNum].isDirty()) ?  "dirty" :  "is not dirty"));
        } else {
            frameNum = this.getEmptyFrame();

            if (frameNum == -1) { //implement replacement policy

                frameNum = lruQueue.remove(0);
                if (this.pool[frameNum].isDirty()) {
                    writePage(this.pool[frameNum].getPageNum());
                    this.pool[frameNum].setDirty(false);
                }

                this.bufTbl.remove(this.pool[frameNum].getPageNum(),frameNum);

            }

            //this will take in an empty frame, otherwise it should take the Least recently used fram in the LRU queue
            readPage(pageNum,frameNum);
            System.out.println("page " + pageNum + " which is stored in frame " + frameNum + " pin count: " + this.pool[frameNum].getPin() + " which is "  + ((this.pool[frameNum].isDirty()) ?  "dirty" :  "is not dirty"));

        }
        this.updateQueue(frameNum);
        System.out.println(this.lruQueue);

    }

    public int getEmptyFrame() {
        for (int i = 0; i < this.poolSize; i++) {
            if (this.pool[i].getContent() == null) {
                return i;
            }
        }

        return -1;
    }

    public int getFreeFrame() {
        for (int i = 0; i < this.poolSize; i++) {
            if (this.pool[i].getPin() == 0) {
                return i;
            }
        }

        return -1;
    }

    public void unpin(int pageNum) {
        int frameNum = bufTbl.lookup(pageNum);
        if (frameNum != -1) {
            this.pool[frameNum].decPin();
            this.updateQueue(frameNum);
        }
        System.out.println(this.lruQueue);
        System.out.println("page " + pageNum + " which is stored in frame " + frameNum + " pin count: " + this.pool[frameNum].getPin() + " which is "  + ((this.pool[frameNum].isDirty()) ?  "dirty" :  "is not dirty"));


    }

    public void updateQueue(int frameNum) {
        if (this.lruQueue.contains(frameNum))
            this.lruQueue.remove(Integer.valueOf(frameNum));
        this.lruQueue.add(frameNum);
    }

    public void createPage(int pageNum) {
        String name = getPageFileName(pageNum);
        String contents = "This is page " + pageNum + ".";
        FileWriter writer = null;

        try {
            writer = new FileWriter(name, false);
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something went wrong while creating the page");
        }
    }

    public void readPage(int pageNum, int frameNum) {
        String path = getPageFileName(pageNum);
        StringBuilder contentBuilder = new StringBuilder();

        try {

            BufferedReader br = new BufferedReader(new FileReader(path));

            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line);
            }
            br.close();
        } catch (IOException e ) {
            e.printStackTrace();
        }

        Frame newFrame = new Frame(contentBuilder.toString());
        newFrame.setPageNum(pageNum);

        this.pool[frameNum] = newFrame;
        this.pool[frameNum].incPin();
        this.bufTbl.insert(pageNum, frameNum);


    }

    public void writePage(int pageNum) {
        Integer frameNum = bufTbl.lookup(pageNum);
        String path = getPageFileName(pageNum);
        String frameContent = pool[frameNum].getContent();

        try {
            FileOutputStream stream = new FileOutputStream(path);
            System.out.println("I have written to disk");
            stream.write(frameContent.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void displayPage(int pageNum) {
        Integer frameNum = bufTbl.lookup(pageNum);
        if (frameNum == null) throw new IllegalArgumentException("Cannot display page that is not in memory");

        pool[frameNum].displayPage();
    }

    public void updatePage(int pageNum, String toAppend) {
        Integer frameNum = bufTbl.lookup(pageNum);
        if (frameNum == null) throw new IllegalArgumentException("Cannot update page that is not in memory");
        pool[frameNum].updatePage(toAppend);
    }

    private String getPageFileName(int pageNum) {
        return pageNum + ".txt";
    }
}
