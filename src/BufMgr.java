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
        } else {
            frameNum = getFreeFrame();

            if (frameNum == -1) { //implement replacement policy
                if (lruQueue.isEmpty()) {
                    System.out.println("No free frame available and LRU queue is empty.");
                    return; // No free frame and LRU queue is empty, can't continue
                }

                frameNum = lruQueue.remove(0);
                if (this.pool[frameNum].isDirty()) {
                    writePage(frameNum);
                }
            }

            readPage(pageNum);
            this.pool[frameNum].incPin();
        }
        updateQueue(frameNum);

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
        if (this.pool[frameNum].getPin() > 0)
            this.pool[pageNum].decPin();

        if (this.pool[frameNum].getPin() == 0 && this.pool[frameNum].isDirty()) {
                writePage(pageNum);
                bufTbl.remove(pageNum, frameNum);
            }

        this.updateQueue(frameNum);
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

    public void readPage(int pageNum) {
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
        int frameNum = -1;
        for (int i = 0; i < poolSize; i++) {
            if (pool[i].getPin() == 0) {
                pool[i] = newFrame;
                frameNum = i;
                break;
            }
        }

        if (frameNum != -1) {
            this.bufTbl.insert(pageNum, frameNum);
        }
        else
            System.out.println("no free frame available in the buffer pool");
    }

    public void writePage(int pageNum) {
        Integer frameNum = bufTbl.lookup(pageNum);
        String path = getPageFileName(pageNum);
        String frameContent = pool[frameNum].getContent();

        try {
            FileOutputStream stream = new FileOutputStream(path);
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
