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
    }

    public void pin(int pageNum) {
        int frameNum = bufTbl.lookup(pageNum);
        if (frameNum != -1) {
            pool[frameNum].incPin();
            lruQueue.add(frameNum);
        } else {
            frameNum = getFreeFrame();
            if (frameNum == -1) {
                frameNum = lruQueue.get(0);
            }

            if (this.pool[frameNum].isDirty()) {
                writePage(pageNum);
            }

            readPage(frameNum);
            this.pool[frameNum].incPin();
            this.pool[frameNum].setDirty(false);

        }
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
        this.pool[pageNum].decPin();

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

        Integer frameNumber = bufTbl.lookup(pageNum);
        pool[frameNumber].setContent(contentBuilder.toString());

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
