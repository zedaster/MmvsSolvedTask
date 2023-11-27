package me.zedaster.mmvstesttask.repository.status;

import me.zedaster.mmvstesttask.model.FileStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

/**
 * Интеграционные тесты для Hibernate репозитория статусов
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class HibernateFileStatusRepositoryTest {
    /**
     * Hibernate репозиторий, который содержит метод для удаления всего содержимого
     */
    @Autowired
    private HibernateFileStatusRepository hibernateStatusRepository;

    /**
     * Тестирование операций создания, завершения процесса, проверки на существование и удаление.
     */
    @Test
    public void addAndProcessAndRemove() {
        final String fileName = "someFile.mp4";
        UUID uuid = hibernateStatusRepository.add(fileName);
        Assertions.assertTrue(hibernateStatusRepository.has(uuid));

        FileStatus processingStatus = hibernateStatusRepository.getById(uuid).get();
        assertFileStatus(uuid, fileName, null, processingStatus);

        hibernateStatusRepository.setLastOperationStatus(uuid, true);

        FileStatus finishedStatus = hibernateStatusRepository.getById(uuid).get();
        assertFileStatus(uuid, fileName, true, finishedStatus);

        hibernateStatusRepository.remove(uuid);
        Assertions.assertFalse(hibernateStatusRepository.has(uuid));
    }

    /**
     * Тестирует, отметиться ли файл как находящийся в обработке
     */
    @Test
    public void markProcessing() {
        final String fileName = "someFile.mp4";
        UUID uuid = hibernateStatusRepository.add(fileName);
        hibernateStatusRepository.setLastOperationStatus(uuid, true);

        hibernateStatusRepository.markProcessing(uuid);
        FileStatus processingStatus = hibernateStatusRepository.getById(uuid).get();
        assertFileStatus(uuid, fileName, null, processingStatus);
    }

    /**
     * Тестирует, что операции над несуществующим статусом происходят корректно и бесшумно
     */
    @Test
    public void quietWorkWithNonExistentStatus() {
        UUID uuid = UUID.randomUUID();

        hibernateStatusRepository.setLastOperationStatus(uuid, true);
        Assertions.assertTrue(hibernateStatusRepository.getById(uuid).isEmpty());

        hibernateStatusRepository.markProcessing(uuid);
        Assertions.assertTrue(hibernateStatusRepository.getById(uuid).isEmpty());

        hibernateStatusRepository.remove(uuid);
    }

    /**
     * Тестирует, соответствует ли переданный {@link FileStatus} переданным параметрам
     *
     * @param uuid                ожидаемый UUID
     * @param fileName            ожидаемое имя файла
     * @param isProcessingSuccess ожидаемый результат обработки
     * @param fileStatus          проверяемый {@link FileStatus}
     */
    private void assertFileStatus(UUID uuid, String fileName, Boolean isProcessingSuccess,
                                  FileStatus fileStatus) {
        Assertions.assertEquals(uuid, fileStatus.getUuid());
        Assertions.assertEquals(fileName, fileStatus.getFileName());
        Assertions.assertEquals(isProcessingSuccess, fileStatus.isLastProcessingSuccess());
    }
}
