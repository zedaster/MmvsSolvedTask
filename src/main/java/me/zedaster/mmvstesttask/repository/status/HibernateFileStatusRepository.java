package me.zedaster.mmvstesttask.repository.status;

import jakarta.persistence.EntityManager;
import me.zedaster.mmvstesttask.model.FileStatus;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий, работающий со статусами обработки файлов. Реализован с помощью Hibernate.
 */
@Repository("hibernateStatusRepository")
public class HibernateFileStatusRepository implements FileStatusRepository {
    @Autowired
    protected EntityManager entityManager;

    @Override
    @Transactional
    public UUID add(String filename) {
        try (Session session = entityManager.unwrap(Session.class)) {
            FileStatus fileStatus = new FileStatus(filename);
            session.persist(fileStatus);
            return fileStatus.getUuid();
        }
    }

    @Override
    @Transactional
    public void markProcessing(UUID uuid) {
        try (Session session = entityManager.unwrap(Session.class)) {
            FileStatus fileStatus = session.get(FileStatus.class, uuid);
            if (fileStatus == null) return;
            fileStatus.startProcessing();
        }
    }

    @Override
    @Transactional
    public void setLastOperationStatus(UUID uuid, boolean isSuccess) {
        try (Session session = entityManager.unwrap(Session.class)) {
            FileStatus fileStatus = session.get(FileStatus.class, uuid);
            if (fileStatus == null) return;
            fileStatus.finishProcessing(isSuccess);
        }
    }

    @Override
    public Optional<FileStatus> getById(UUID uuid) {
        try (Session session = entityManager.unwrap(Session.class)) {
            FileStatus status = session.get(FileStatus.class, uuid);
            return Optional.ofNullable(status);
        }
    }

    @Override
    public boolean has(UUID uuid) {
        return getById(uuid).isPresent();
    }

    @Override
    @Transactional
    public void remove(UUID uuid) {
        try (Session session = entityManager.unwrap(Session.class)) {
            FileStatus fileStatus = session.get(FileStatus.class, uuid);
            if (fileStatus == null) return;
            session.remove(fileStatus);
        }
    }
}
