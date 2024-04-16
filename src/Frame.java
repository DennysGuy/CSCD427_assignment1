public class Frame {
    private int pinCount = 0;
    private boolean dirty = false;
    private String content;
    private int pageNum;

    public Frame(String content) {
        this.content = content;
    }


    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }

    public int getPin() {
        // your code goes here
        return this.pinCount;
    }

    public int getPageNum() {
        return this.pageNum;
    }

    public void incPin() {
        this.pinCount++;
    }

    public void decPin() {
        if (pinCount > 0)
            this.pinCount--;
    }

    public boolean isDirty() {
        // your code goes here
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void displayPage() {
        System.out.println(content);
    }

    public void updatePage(String toAppend) {
        content += "\n" + toAppend;
        dirty = true;
    }

    public String getContent() {
        return content;
    }

    public void setContent (String content) {
        this.content = content;
    }
}
