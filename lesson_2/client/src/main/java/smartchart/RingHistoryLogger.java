package smartchart;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

public class RingHistoryLogger {
    private File file;
    private int limit;
    private LinkedList<Message>   messages;

    public RingHistoryLogger(String login) {
        limit = 0; //No limitation
        file = new File("history_" + login + ".txt");
        messages = new LinkedList<>();
    }

    public RingHistoryLogger(String login, int limit) {
        this(login);
        this.limit = limit; //No limitation
    }

    public synchronized void addMessage(String message) {
        messages.add(new Message(message));
        if ( limit > 0 && messages.size() > limit ) {
            messages.removeFirst();
        }
    }

    public synchronized void addMessages(String messages) {
        String[] tokens = messages.split("\\r\\n|\\n|\\r" );
        for (int i = 0; i < tokens.length; i++) {
            addMessage(tokens[i]);
        }
    }

    public synchronized String[] getMessages() {
        int i = 0;
        String[] msg = new String[messages.size()];
        Iterator<Message> iter = messages.iterator();

        while( iter.hasNext() ) {
            msg[i++] = iter.next().message;
        }
        return msg;
    }

    public synchronized void clear() {
        messages.clear();
    }

    public synchronized boolean flushToFile() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file,true));
            Iterator<Message> iterator = messages.iterator();
            while (iterator.hasNext()) {
                Message msg = iterator.next();
                if ( !msg.writed ) {
                    writer.write(msg.message + "\n");
                    msg.writed = true;
                }
            }
            writer.flush();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if ( writer != null ) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public synchronized boolean restoreFromFile() {
        return restoreFromFileLastRecords(0);
    }

    public synchronized boolean restoreFromFileLastRecords(int limit) {
        clear();
        if ( !file.exists() ) {
            return false;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {

                addMessage(line);
                messages.peekLast().writed = true;
                if ( limit > 0 && messages.size() > limit ) {
                    messages.removeFirst();
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

    class Message {
        public String message;
        public boolean writed;

        public Message(String message) {
            this.message = message;
            writed = false;
        }
    }

}
