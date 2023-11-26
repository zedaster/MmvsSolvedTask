package me.zedaster.mmvstesttask.repository.video;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.zedaster.mmvstesttask.configuration.FileStorageConfiguration;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Repository
public class LocalVideoFileRepository implements VideoFileRepository {
    @Autowired
    private FileStorageConfiguration fileStorageConfiguration;

    private final Map<UUID, CompletableFuture<Boolean>> processingFiles = new HashMap<>();

    @Override
    public CompletableFuture<Boolean> runUploading(UUID uuid, InputStreamSource inputStreamSource) {
        Path targetPath = getMp4FilePath(uuid);
        CompletableFuture<Boolean> operation = completeAsync(() -> {
            {
                try (InputStream inputStream = inputStreamSource.getInputStream()) {
                    Files.copy(inputStream, targetPath);
                    processingFiles.remove(uuid);
                    return true;
                } catch (IOException e) {
                    processingFiles.remove(uuid);
                    // TODO: Log here
                    e.printStackTrace();
                    return false;
                }
            }
        });
        processingFiles.put(uuid, operation);
        return operation;
    }

    @Override
    public CompletableFuture<Boolean> runAdjustingSize(UUID uuid, int width, int height) throws NonExistentIdException,
            ProcessingException {
        Path targetPath = getMp4FilePath(uuid);
        Path tempPath = getTempMp4FilePath(uuid);

        if (Files.notExists(targetPath)) {
            throw new NonExistentIdException();
        }

        if (isProcessing(uuid)) {
            throw new ProcessingException();
        }

        CompletableFuture<Boolean> operation = completeAsync(() -> {
            try {
                FFmpeg ffmpeg = new FFmpeg(fileStorageConfiguration.getFFmpegPath().toAbsolutePath().toString());
                FFprobe ffprobe = new FFprobe(fileStorageConfiguration.getFFprobePath().toAbsolutePath().toString());

                FFmpegBuilder builder = new FFmpegBuilder()
                        .setInput(targetPath.toAbsolutePath().toString())
                        .overrideOutputFiles(true)
                        .addOutput(tempPath.toAbsolutePath().toString())
                        .setFormat("mp4")
                        .setVideoResolution(width, height)
                        .setStrict(FFmpegBuilder.Strict.STRICT)
                        .done();

                createTempFolderIfNotExists();
                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
                executor.createJob(builder).run();

                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                processingFiles.remove(uuid);
                deleteFileIfExistsQuietly(tempPath);
                return true;
            } catch (IOException e) {
                processingFiles.remove(uuid);
                deleteFileIfExistsQuietly(tempPath);
                // TODO: Log
                e.printStackTrace();
                return false;
            }
        });

        processingFiles.put(uuid, operation);
        return operation;
    }

    @Override
    public CompletableFuture<Void> runRemoving(UUID uuid) throws NonExistentIdException {
        Path targetPath = getMp4FilePath(uuid);
        if (Files.notExists(targetPath)) {
            throw new NonExistentIdException();
        }
        Runnable removeAction = () -> {
            try {
                Files.delete(targetPath);
            } catch (IOException e) {
                // TODO: Log
                throw new RuntimeException(e);
            }
        };

        if (isProcessing(uuid)) {
            return processingFiles.get(uuid).thenRun(removeAction);
        }

        return runAsync(removeAction);
    }

    @Override
    public boolean isProcessing(UUID uuid) {
        return processingFiles.containsKey(uuid);
    }

    private Path getMp4FilePath(UUID uuid) {
        return fileStorageConfiguration.getStorageFolderPath().resolve(uuid.toString() + ".mp4");
    }

    private Path getTempMp4FilePath(UUID uuid) {
        return fileStorageConfiguration.getStorageFolderPath()
                .resolve("temp/")
                .resolve(uuid.toString() + ".mp4");
    }

    private void createTempFolderIfNotExists() {
        Path folder = fileStorageConfiguration.getStorageFolderPath()
                .resolve("temp/");
        try {
            if (Files.notExists(folder)) {
                Files.createDirectory(folder);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteFileIfExistsQuietly(Path tempPath) {
        try {
            Files.deleteIfExists(tempPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Boolean> completeAsync(Supplier<Boolean> supplier) {
        return Single.fromCallable(supplier::get)
                .subscribeOn(Schedulers.io())
                .toCompletionStage()
                .toCompletableFuture();
    }

    private CompletableFuture<Void> runAsync(Runnable runnable) {
        return Completable.fromRunnable(runnable)
                .subscribeOn(Schedulers.io())
                .toCompletionStage(null)
                .toCompletableFuture()
                .thenApply(o -> null);
    }
}
