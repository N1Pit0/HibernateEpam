package com.mygym.crm.backstages.persistence.daos.trainerdao;

import com.mygym.crm.backstages.domain.models.Trainer;
import com.mygym.crm.backstages.repositories.daorepositories.TrainerDao;
import org.hibernate.HibernateError;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Optional;


@Repository
public class TrainerDaoImpl implements TrainerDao {

    private SessionFactory sessionFactory;
    private static final Logger logger = LoggerFactory.getLogger(TrainerDaoImpl.class);

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Trainer> create(Trainer trainer) {
        checkTrainer(trainer, Trainer.class);

        try {
            logger.info("Creating trainer with UserName: {}", trainer.getUserName());
            Session session = this.sessionFactory.getCurrentSession();
            Serializable generatedID = session.save(trainer);

            if (generatedID != null) {
                logger.info("Successfully created trainer with UserName: {}", trainer.getUserName());
                return Optional.of(trainer);
            }
        } catch (HibernateError e) {
            logger.error(e.getMessage());
            throw new HibernateException(e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Trainer> update(Trainer trainer) {
        checkTrainer(trainer, Trainer.class);

        try {
            logger.info("Updating trainer: {}", trainer.getUserId());

            Session session = this.sessionFactory.getCurrentSession();
            Trainer newTrainer = (Trainer) session.merge(trainer);
            session.flush();

            return Optional.ofNullable(newTrainer);
        } catch (HibernateException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Trainer> select(Long trainerId) {
        checkTrainer(trainerId, Long.class);

        logger.info("Attempting to select trainer with ID: {}", trainerId);

        try {
            Session session = this.sessionFactory.getCurrentSession();
            Trainer trainer = session.get(Trainer.class, trainerId);

            if (trainer != null) {
                logger.info("Successfully selected trainer with ID: {}", trainerId);
                return Optional.of(trainer);
            }

            logger.warn("No trainer found with ID: {}", trainerId);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error selecting trainer with ID: {}", trainerId, e);
            throw e;
        }
    }

    @Override
    public Optional<Trainer> selectWithUserName(String username) {
        checkTrainer(username, String.class);

        logger.info("Attempting to select trainer with username: {}", username);

        try {
            Session session = this.sessionFactory.getCurrentSession();

            String sql = """
             SELECT t FROM Trainer t\s
             LEFT JOIN FETCH t.trainings tr\s
             WHERE t.userName = :username
           """;

            Trainer trainer = (Trainer) session.createQuery(sql.strip(), Trainer.class)
                    .setParameter("username", username)
                    .uniqueResult();

            if (trainer != null) {
                logger.info("Successfully selected trainer with username: {}", username);
                return Optional.of(trainer);
            }

            logger.warn("No trainer found with username: {}", username);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error selecting trainer with username: {}", username, e);
            throw e;
        }
    }

    @Override
    public boolean changePassword(String username, String newPassword) {
        checkTrainer(username, String.class);

        logger.info("Attempting to change password for trainer with username: {}", username);

        try {
            Session session = this.sessionFactory.getCurrentSession();

            String sql = """
                UPDATE Trainer t\s
                SET t.password = :newPassword\s
                WHERE t.userName = :username
               """;

            int affectedRows = session.createQuery(sql.strip())
                    .setParameter("newPassword", newPassword)
                    .setParameter("username", username)
                    .executeUpdate();

            if (affectedRows == 1) {
                logger.info("Successfully changed password for trainer with username: {}", username);
                return true;
            }

            logger.warn("No trainer found to change password with username: {}", username);
            return false;
        } catch (Exception e) {
            logger.error("Error changing password for trainer with username: {}", username, e);
            throw e;
        }
    }

    @Override
    public boolean toggleIsActive(String username) {
        checkTrainer(username, String.class);

        logger.info("Attempting to toggle isActive for trainer with username: {}", username);

        try {
            Session session = this.sessionFactory.getCurrentSession();

            String sql = """
                    SELECT t.isActive\s
                    From Trainer t\s
                    WHERE t.userName = :username
                    """;

            Boolean isActive = (Boolean) session.createQuery(sql.strip(), Boolean.class)
                    .setParameter("username", username)
                    .uniqueResult();

            if (isActive == null) {
                logger.warn("No trainer found to toggle isActive with username: {}", username);
                return false;
            }

            boolean newIsActive = !isActive;

            sql = """
                    UPDATE Trainer t\s
                    SET t.isActive = :isActive\s
                    WHERE t.userName = :username
                    """;

            int affectedRows = session.createQuery(sql.strip())
                    .setParameter("isActive", newIsActive)
                    .setParameter("username", username)
                    .executeUpdate();

            if (affectedRows == 1) {
                logger.info("Successfully toggled isActive for trainer with username: {} from: {} to: {}",
                        username,
                        isActive,
                        newIsActive);
                return true;
            }

            logger.warn("Failed to toggle isActive for trainer with username: {}", username);
            return false;

        }catch (Exception e) {
            logger.error("Error toggling isActive for trainer with username: {}", username, e);
            throw e;
        }
    }

    private <T> void checkTrainer(T trainer, Class<T> tClass) {
        if (trainer == null) {
            String className = tClass.getName();
            switch (className) {
                case "com/mygym/crm/backstages/domain/models/Trainer.java":
                    logger.error("Trainer is null");
                    throw new IllegalArgumentException("Trainer must not be null");
                case "java.lang.Long":
                    logger.error("Trainer ID is null");
                    throw new IllegalArgumentException("Trainer ID must not be null");
                case "java.lang.String":
                    logger.error("Trainer username is null");
                    throw new IllegalArgumentException("Trainer username must not be null");
                default:
                    throw new IllegalArgumentException("Unexpected type: " + className);
            }
        }
    }
}
