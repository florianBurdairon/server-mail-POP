package pop;

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

    /**
     * Retrieve mails from the user's directory
     * @return a map of mails with their index as key
     */
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
                    scanner.close();
                } catch (FileNotFoundException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
        return mails;
    }

    /**
     * Get the size of each mail
     * @return a map of mails with their index as key and their size as value
     */
    public Map<Integer, Long> getMailsSize() {
        Map<Integer, Long> mailsSize = new HashMap<>();//new long[mails.size()];
        mails.forEach((index, mail) -> mailsSize.put(index, mail.size));
        return mailsSize;
    }

    /**
     * Get the content of a mail at a given index
     * @param index the index of the mail
     * @return the content of the mail
     */
    public String getMailAtIndex(int index) {
        Mail mail = mails.get(index);
        return mail.content;
    }

    /**
     * Delete a mail at a given index
     * @param index the index of the mail
     */
    public void deleteMail(int index) {
        deletedMails.put(index, mails.get(index));
        mails.remove(index);
    }

    /**
     * Reset the deleted mails
     */
    public void resetDeletedMails() {
        mails.putAll(deletedMails);
        deletedMails = new HashMap<>();
    }

    /**
     * Update the mails
     */
    public void updateMails() {
        deletedMails.forEach((index, deleteMail) -> {
            File toDeleteFile = new File(deleteMail.filepath);
            System.out.println(deleteMail.filepath);
            System.out.println(toDeleteFile.delete());
        });
        deletedMails = new HashMap<>();
    }
}
