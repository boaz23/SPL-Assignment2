package bgu.spl.mics.loggers;

import java.io.*;

public class FileLogger implements Logger, Closeable {
    private Writer fileWriter;
    private final String filePath;

    public FileLogger(String filePath) throws IOException {
        fileWriter = new BufferedWriter(new FileWriter(filePath));
        this.filePath = filePath;
    }

    @Override
    public synchronized Logger appendLine(String s) {
        try {
            fileWriter.append(s)
                .append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public synchronized Logger append(String s) {
        try {
            fileWriter.append(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public synchronized Logger appendLine(Object o) {
        try {
            fileWriter.append(o.toString())
                .append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public synchronized Logger append(Object o) {
        try {
            fileWriter.append(o.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public void flush() throws IOException {
        fileWriter.flush();
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
    }

    @Override
    public String toString() {
        return "FileLogger: '" + filePath + "'";
    }
}
