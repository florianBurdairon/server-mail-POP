package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class MailRepository {
    private static final String dataRootDirectory = "data\\";

    private final User user;

    private final Map<Integer, Mail> mails;
    private Map<Integer, Mail> deletedMails;

    public MailRepository(User user) {
        this.user = user;
        mails = retrieveMails();
        deletedMails = new HashMap<>();
    }

    private Map<Integer, Mail> retrieveMails() {
        Map<Integer, Mail> mails = new HashMap<>();
        File folder = new File(dataRootDirectory + user.getUsername());
        File[] files = folder.listFiles();
        if (files != null) {
            for(int i = 0; i < files.length; i++) {
                try {
                    Scanner scanner = new Scanner(files[i]);
                    StringBuilder builder = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        builder.append(scanner.nextLine());
                    }
                    mails.put(i, new Mail(files[i].getAbsolutePath(), builder.toString()));
                } catch (FileNotFoundException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
        return mails;
    }

    public long[] getMailsSize() {
        long[] mailsSize = new long[mails.size()];
        for(int i = 0; i < mails.size(); i++) {
            mailsSize[i] = mails.get(i).size;
        }
        return mailsSize;
    }

    public String getMailAtIndex(int index) {
        Mail mail = mails.get(index);
        return mail.content;
    }

    public void deleteMail(int index) {
        deletedMails.put(index, mails.get(index));
        mails.remove(index);
    }

    public void resetDeletedMails() {
        mails.putAll(deletedMails);
        deletedMails = new HashMap<>();
    }

    public void updateMails() {
        deletedMails.forEach((index, deleteMail) -> {
            File toDeleteFile = new File(deleteMail.filepath);
            toDeleteFile.delete();
        });
        deletedMails = new HashMap<>();
    }
}
