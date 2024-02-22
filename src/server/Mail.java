package server;

public class Mail {
    public final long size;
    public final String content;
    public final String filepath;
    
    public Mail(String filepath, String content) {
        this.filepath = filepath;
        this.content = content;
        this.size = content.getBytes().length;
    }
}
