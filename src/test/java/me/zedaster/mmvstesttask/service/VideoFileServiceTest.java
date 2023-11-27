package me.zedaster.mmvstesttask.service;

import me.zedaster.mmvstesttask.configuration.FileStorageConfiguration;
import me.zedaster.mmvstesttask.model.FileStatus;
import me.zedaster.mmvstesttask.repository.video.NonExistentIdException;
import me.zedaster.mmvstesttask.repository.video.ProcessingException;
import me.zedaster.mmvstesttask.service.video.FileInfo;
import me.zedaster.mmvstesttask.service.video.FutureVideo;
import me.zedaster.mmvstesttask.service.video.VideoFileService;
import me.zedaster.mmvstesttask.service.video.exception.IncorrectSizeException;
import me.zedaster.mmvstesttask.service.video.exception.UploadFormatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamSource;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = {VideoFileService.class, FakeVideoVideoFileRepository.class, FakeStatusRepository.class,
        FileStorageConfiguration.class})
@ActiveProfiles(profiles = "test")
public class VideoFileServiceTest {
    private final VideoFileService videoFileService;

    private final FakeVideoVideoFileRepository fakeVideoFileRepository;

    private final FakeStatusRepository fakeStatusRepository;

    @Autowired
    public VideoFileServiceTest(FakeVideoVideoFileRepository fakeVideoFileRepository,
                                FakeStatusRepository fakeStatusRepository) {
        this.fakeVideoFileRepository = fakeVideoFileRepository;
        this.fakeStatusRepository = fakeStatusRepository;
        this.videoFileService = new VideoFileService(
                fakeVideoFileRepository,
                fakeStatusRepository
        );
    }

    /**
     * Очищает репозитории после каждого тестоа.
     */
    @AfterEach
    public void reinitialize() {
        this.fakeVideoFileRepository.removeAllFiles();
        this.fakeStatusRepository.removeAll();
    }

    /**
     * Тест на получения статуса после загрузки ролика и его удаление.
     */
    @Test
    public void saveAndGetLoadedStatusAndRemove() throws UploadFormatException {
        final String fileName = "OneSecondVideo.mp4";
        FutureVideo futureVideo = videoFileService.asyncSaveFile(fileName, getResourceAsInputStreamSource(fileName));
        UUID uuid = futureVideo.getUuid();

        futureVideo.getUploadingOperation().thenRun(() -> {
            FileInfo fileInfo;
            try {
                fileInfo = videoFileService.getInfoById(uuid);
                // Синхронизируем удаление для проверки всей корректности операции
                videoFileService.asyncDeleteById(uuid).get();
            } catch (NonExistentIdException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Проверка fileInfo
            assertFileInfo(uuid, fileName, false, true, fileInfo);

            // Файла не должно быть после теста
            Assertions.assertThrows(NonExistentIdException.class, () -> {
                videoFileService.getInfoById(uuid);
            });
        });
    }

    /**
     * Тест на получение статуса о файле во время загрузки.
     */
    @Test
    public void getLoadingStatus() throws UploadFormatException, NonExistentIdException {
        final String fileName = "HeavyVideo.mp4";
        FutureVideo futureVideo = videoFileService.asyncSaveFile(fileName, getResourceAsInputStreamSource(fileName));
        UUID uuid = futureVideo.getUuid();
        FileInfo fileStatus = videoFileService.getInfoById(uuid);

        // Проверка fileInfo
        assertFileInfo(uuid, fileName, true, null, fileStatus);
    }

    /**
     * Сохраняет файл, который не является видео
     */
    @Test
    public void saveNotVideo() {
        final String fileName = "NotVideo.txt";
        Assertions.assertThrows(UploadFormatException.class, () -> {
            videoFileService.asyncSaveFile(fileName, getResourceAsInputStreamSource(fileName));
        });
    }

    /**
     * Получает информацию о файле, которого не существует
     */
    @Test
    public void getFileInfoOfNonExistingVideo() {
        Assertions.assertThrows(NonExistentIdException.class, () -> {
            videoFileService.getInfoById(UUID.randomUUID());
        });
    }

    /**
     * Удаляет файл, которого не существует
     */
    @Test
    public void removeNonExistentVideo() {
        Assertions.assertThrows(NonExistentIdException.class, () -> {
            videoFileService.asyncDeleteById(UUID.randomUUID());
        });
    }

    /**
     * Изменяет размер видео на то же самое разрешение
     */
    @Test
    public void adjustSameSizeCompletely() throws ExecutionException, InterruptedException, NonExistentIdException,
            IncorrectSizeException, UploadFormatException, ProcessingException {
        adjustSmallVideoCompletely(640, 360);
    }

    /**
     * Изменяет размер видео на меньшее разрешение с тем же соотношением сторон
     */
    @Test
    public void adjustSmallerSizeCompletely() throws ExecutionException, InterruptedException, NonExistentIdException,
            IncorrectSizeException, UploadFormatException, ProcessingException {
        adjustSmallVideoCompletely(320, 180);
    }

    /**
     * Изменяет размер видео на большее разрешение с тем же соотношением сторон
     */
    @Test
    public void adjustBiggerSizeCompletely() throws ExecutionException, InterruptedException, NonExistentIdException,
            IncorrectSizeException, UploadFormatException, ProcessingException {
        adjustSmallVideoCompletely(1280, 720);
    }

    /**
     * Изменяет размер видео на разрешение с иным соотношением сторон
     */
    @Test
    public void adjustDisproportionalSizeCompletely() throws ExecutionException, InterruptedException, NonExistentIdException,
            IncorrectSizeException, UploadFormatException, ProcessingException {
        adjustSmallVideoCompletely(360, 360);
    }

    /**
     * Тестирует изменение размера видео
     * @param width ширина
     * @param height высота
     */
    private void adjustSmallVideoCompletely(int width, int height) throws UploadFormatException, ExecutionException,
            InterruptedException, NonExistentIdException, IncorrectSizeException, ProcessingException {
        final String fileName = "OneSecondVideo.mp4";

        // Прошлыми тестами загрузка проверена, поэтому здесь не повторяем их
        FutureVideo futureVideo = videoFileService.asyncSaveFile(fileName, getResourceAsInputStreamSource(fileName));
        UUID uuid = futureVideo.getUuid();
        // Ждем полную загрузку
        futureVideo.getUploadingOperation().get();

        videoFileService.asyncAdjustSize(uuid, width, height).get();
        FileInfo status = videoFileService.getInfoById(uuid);
        assertFileInfo(uuid, fileName, false, true, status);
    }

    /**
     * Тестирует нахождение в статусе обработки при измении размера видео
     */
    @Test
    public void getVideoAdjustProcessingStatus() throws UploadFormatException, ExecutionException, InterruptedException,
            NonExistentIdException, IncorrectSizeException, ProcessingException {
        final String fileName = "HeavyVideo.mp4";
        FutureVideo futureVideo = videoFileService.asyncSaveFile(fileName, getResourceAsInputStreamSource(fileName));
        UUID uuid = futureVideo.getUuid();
        // Ждем полную загрузку
        futureVideo.getUploadingOperation().get();

        videoFileService.asyncAdjustSize(uuid, 640, 360);
        FileInfo status = videoFileService.getInfoById(uuid);
        assertFileInfo(uuid, fileName, true, null, status);
    }

    /**
     * Тестирует некорректные размеры для изменения разрешения видео
     */
    @Test
    public void incorrectAdjustParams() throws UploadFormatException, ExecutionException, InterruptedException,
            NonExistentIdException {
        List<List<Integer>> incorrectParams = List.of(
                List.of(40, 0), List.of(0, 40),
                List.of(40, -1), List.of(-1, 40),
                List.of(40, 33), List.of(33, 40),
                List.of(40, 1), List.of(1, 40)
        );

        List<CompletableFuture<Void>> deleteOperations = new ArrayList<>();
        for (List<Integer> params : incorrectParams) {
            final String fileName = "OneSecondVideo.mp4";

            FutureVideo futureVideo = videoFileService.asyncSaveFile(fileName, getResourceAsInputStreamSource(fileName));
            UUID uuid = futureVideo.getUuid();
            // Ждем полную загрузку
            futureVideo.getUploadingOperation().get();

            Assertions.assertThrows(IncorrectSizeException.class, () -> {
                videoFileService.asyncAdjustSize(uuid, params.get(0), params.get(1));
            });

            deleteOperations.add(videoFileService.asyncDeleteById(uuid));
        }

        CompletableFuture<Void>[] deleteOperationsArray = deleteOperations
                .toArray((CompletableFuture<Void>[]) new CompletableFuture[0]);
        CompletableFuture.allOf(deleteOperationsArray).get();
    }

    /**
     * Тестирует изменение разрешение у видео, которого не существует
     */
    @Test
    public void adjustNonExistingVideo() {
        Assertions.assertThrows(NonExistentIdException.class, () -> {
            videoFileService.asyncAdjustSize(UUID.randomUUID(), 320, 180);
        });
    }

    /**
     * Загружает ресурс в виде интерфейса {@link InputStreamSource}
     */
    private InputStreamSource getResourceAsInputStreamSource(String path) {
        return new InputStreamSource() {
            @Override
            public InputStream getInputStream() {
                return this.getClass().getResourceAsStream("/" + path);
            }
        };
    }

    /**
     * Тестирует, соответствует ли переданный {@link FileStatus} переданным параметрам
     *
     * @param uuid                ожидаемый UUID
     * @param fileName            ожидаемое имя файла
     * @param isProcessing        ожидаемый статус обрабатывается/не обрабатывается
     * @param isProcessingSuccess ожидаемый результат обработки
     * @param fileStatus          проверяемый {@link FileStatus}
     */
    private void assertFileInfo(UUID uuid, String fileName, boolean isProcessing, Boolean isProcessingSuccess,
                                FileInfo fileStatus) {
        Assertions.assertEquals(uuid, fileStatus.getUuid());
        Assertions.assertEquals(fileName, fileStatus.getFileName());
        Assertions.assertEquals(isProcessing, fileStatus.isProcessing());
        Assertions.assertEquals(isProcessingSuccess, fileStatus.isLastProcessingSuccess());
    }
}
