package smartchart;

import java.io.*;
import java.util.LinkedList;

public class HistoryLogger {
    private File file;
    private int limit;
    private LinkedList<String> messages;

    public HistoryLogger(String login) {
        limit = 0; //No limitation
        file = new File("history_" + login + ".txt");
        messages = new LinkedList<>();
    }

    public void addMessage(String message) {
        
    }

    public void saveLastMessages( String messages, int limit ) {
        if ( !file.exists() ) {
            return;
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            String[] tokens = messages.split("\\r\\n|\\n|\\r" );

            int offset = 0;
            if ( tokens.length > limit ) {
                offset = tokens.length - limit;
            }

            for (int i = offset; i < tokens.length; i++) {
                writer.write(tokens[i]);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
        } finally {
            if ( writer != null ) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String[] restoreLastMessages(int limit) {
        if ( !file.exists() ) {
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            LinkedList<String> msglist = new LinkedList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                msglist.add(line);
                if ( msglist.size() > limit) {
                    msglist.removeFirst();
                }
            }
            String[] result = new String[msglist.size()];
            msglist.toArray(result);
            return result;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return null;
    }
}
