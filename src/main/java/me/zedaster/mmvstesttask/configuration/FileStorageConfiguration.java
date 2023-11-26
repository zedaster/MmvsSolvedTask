package me.zedaster.mmvstesttask.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class FileStorageConfiguration {
    @Value("${storage.ff-mpeg-path}")
    private String ffMpegPath;

    @Value("${storage.ff-probe-path")
    private String ffProbePath;

    @Value("${storage.folder-path}")
    private String folderPath;

    public Path getStorageFolderPath() {
        return Path.of(folderPath);
    }

    public Path getFFmpegPath() {
        return Path.of(ffMpegPath);
    }

    public Path getFFprobePath() {
        return Path.of(ffProbePath);
    }
}
