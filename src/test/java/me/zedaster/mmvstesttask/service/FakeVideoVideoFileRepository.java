package me.zedaster.mmvstesttask.service;

import me.zedaster.mmvstesttask.configuration.FileStorageConfiguration;
import me.zedaster.mmvstesttask.repository.video.LocalVideoFileRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Тестовое хранилище файлов, которое можно полностью очищает
 */
public class FakeVideoVideoFileRepository extends LocalVideoFileRepository {
    @Autowired
    private FileStorageConfiguration fileStorageConfiguration;

    /**
     * Удаляет все файлы
     */
    public void removeAllFiles() {
        try {
            removeFolderRecursively(fileStorageConfiguration.getStorageFolderPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeFolderRecursively(Path path) throws IOException {
        Files.walk(path, 1)
                .filter((p) -> !p.equals(path))
                .forEach((child) -> {
                    try {
                        if (Files.isDirectory(child)) {
                            removeFolderRecursively(child);
                        }
                        Files.delete(child);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

    }
}
