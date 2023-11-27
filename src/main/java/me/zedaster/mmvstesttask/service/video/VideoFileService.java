package me.zedaster.mmvstesttask.service.video;

import me.zedaster.mmvstesttask.model.FileStatus;
import me.zedaster.mmvstesttask.repository.status.FileStatusRepository;
import me.zedaster.mmvstesttask.repository.video.NonExistentIdException;
import me.zedaster.mmvstesttask.repository.video.ProcessingException;
import me.zedaster.mmvstesttask.repository.video.VideoFileRepository;
import me.zedaster.mmvstesttask.service.video.exception.IncorrectSizeException;
import me.zedaster.mmvstesttask.service.video.exception.UploadFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис хранения видеофайлов с возможностью менять разрешение
 */
@Service
public class VideoFileService {
    /**
     * Хранилище видеофайлов
     */
    @Autowired
    private VideoFileRepository videoFileRepository;

    /**
     * Репозиторий со статусом операций над файлами
     */
    @Autowired
    private FileStatusRepository fileStatusRepository;

    @Autowired
    public VideoFileService(VideoFileRepository videoFileRepository, FileStatusRepository fileStatusRepository) {
        this.videoFileRepository = videoFileRepository;
        this.fileStatusRepository = fileStatusRepository;
    }

    /**
     * Асинхронно запускает загрузку видеофайла в репозиторий
     *
     * @param filename          Имя видеофайла
     * @param inputStreamSource Класс, из которого можно получить поток видеофайла
     * @return Объект {@link FutureVideo}, содержащий UUID и CompletableFuture операции загрузки
     * @throws UploadFormatException если формат видеофайла неверный
     */
    public FutureVideo asyncSaveFile(String filename, InputStreamSource inputStreamSource) throws UploadFormatException {
        checkFileIsMp4(filename);
        UUID uuid = fileStatusRepository.add(filename);

        return new FutureVideo(uuid, videoFileRepository.runUploading(uuid, inputStreamSource));
    }

    /**
     * Асинхронно запускает изменение размер загруженного видеофайла.
     *
     * @param uuid   UUID файла
     * @param width  новая ширина в пикселях (четное число больше 20)
     * @param height новая высота в пикселях (четное число больше 20)
     * @return Объект {@link CompletableFuture} асинхронной операции загрузки.
     * @throws NonExistentIdException когда загруженного файла с таким UUID нет
     * @throws IncorrectSizeException когда размеры введены неверно.
     */
    public CompletableFuture<Void> asyncAdjustSize(UUID uuid, int width, int height) throws NonExistentIdException,
            IncorrectSizeException, ProcessingException {

        checkAdjustParameters(width, height);

        return videoFileRepository
                .runAdjustingSize(uuid, width, height)
                .thenAccept((isSuccess) -> fileStatusRepository.setLastOperationStatus(uuid, isSuccess));
    }

    /**
     * Получает информацию о файле и статусе его обработки по UUID
     *
     * @param uuid UUID файла
     * @return объект {@link FileStatus}, в котором содержиться информация
     * @throws NonExistentIdException когда файла с таким UUID нет
     */
    public FileInfo getInfoById(UUID uuid) throws NonExistentIdException {
        Optional<FileStatus> optionalFileStatus = fileStatusRepository.getById(uuid);
        if (optionalFileStatus.isEmpty()) {
            throw new NonExistentIdException();
        }
        boolean isProcessing = videoFileRepository.isProcessing(uuid);
        return new FileInfo(optionalFileStatus.get(), isProcessing);
    }

    /**
     * Удаляет видеофайл по его UUID. Синхронно
     *
     * @throws NonExistentIdException когда файла с таким UUID нет
     */
    public CompletableFuture<Void> asyncDeleteById(UUID uuid) throws NonExistentIdException {
        checkFileExistence(uuid);

        fileStatusRepository.remove(uuid);

        return videoFileRepository.runRemoving(uuid);
    }

    /**
     * Тестирует корректность названия у видеофайла mp4
     *
     * @param filename          Имя файла
     * @throws UploadFormatException Если файл или название некорректно
     */
    private void checkFileIsMp4(String filename) throws UploadFormatException {
        if (!filename.substring(filename.length() - 4).toLowerCase().endsWith(".mp4")) {
            throw new UploadFormatException();
        }
    }

    /**
     * Тестирует корректность параметров для изменения размера.
     * Каждое измерение должно быть положительным числом, кратным двум.
     *
     * @param width  ширина
     * @param height высота
     * @throws IncorrectSizeException Если размеры некорректные
     */
    private void checkAdjustParameters(int width, int height) throws IncorrectSizeException {
        if (width <= 0 || height <= 0 || width % 2 != 0 || height % 2 != 0) {
            throw new IncorrectSizeException();
        }
    }

    /**
     * Проверяет, существует ли файл
     *
     * @param uuid UUID файла
     * @throws NonExistentIdException Если файл с таким UUID не существует
     */
    private void checkFileExistence(UUID uuid) throws NonExistentIdException {
        if (!fileStatusRepository.has(uuid)) {
            throw new NonExistentIdException();
        }
    }
}