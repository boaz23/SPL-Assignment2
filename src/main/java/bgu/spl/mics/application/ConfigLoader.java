package bgu.spl.mics.application;

import bgu.spl.mics.application.config.Config;
import com.google.gson.Gson;

import java.io.*;

public class ConfigLoader implements Closeable {
    private final String filePath;
    private final Gson gson;
    private Reader fileReader;

    public ConfigLoader(String filePath) {
        this(filePath, new Gson());
    }
    public ConfigLoader(String filePath, Gson gson) {
        this.filePath = filePath;
        this.gson = gson;
    }

    public ConfigLoader init() throws FileNotFoundException {
        fileReader = new FileReader(filePath);
        return this;
    }

    public Config load() {
        if (fileReader == null) {
            throw new IllegalStateException("The loader must initialized.");
        }

        return gson.fromJson(fileReader, Config.class);
    }

    @Override
    public void close() throws IOException {
        if (fileReader != null) {
            fileReader.close();
        }
    }
}
