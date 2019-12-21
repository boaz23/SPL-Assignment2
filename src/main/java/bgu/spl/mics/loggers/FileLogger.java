package bgu.spl.mics.loggers;

import java.io.*;

public class FileLogger implements Logger, Closeable {
    private Writer fileWriter;

    public FileLogger(String filePath) throws IOException {
        fileWriter = new BufferedWriter(new FileWriter(filePath));
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
    public void close() throws IOException {
        fileWriter.close();
    }
}
