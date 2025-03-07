package com.mygym.crm.backstages.persistence.daos.trainingdao;

import com.mygym.crm.backstages.domain.models.Trainer;
import com.mygym.crm.backstages.domain.models.Training;
import com.mygym.crm.backstages.domain.models.TrainingType;
import com.mygym.crm.backstages.repositories.daorepositories.TrainingDao;
import lombok.Getter;
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
public class TrainingDaoImpl implements TrainingDao {

    @Getter
    private SessionFactory sessionFactory;
    private static final Logger logger = LoggerFactory.getLogger(TrainingDaoImpl.class);

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Training> select(Long trainingKey) {

        logger.info("Attempting to select Training with ID: {}", trainingKey);

        try {
            Session session = this.sessionFactory.getCurrentSession();
            Training training = session.get(Training.class, trainingKey);

            if (training != null) {
                logger.info("Successfully selected Training with ID: {}", trainingKey);
                return Optional.of(training);
            }

            logger.warn("No Training found with ID: {}", trainingKey);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error selecting Training with ID: {}", trainingKey, e);
            throw e;
        }
    }

    @Override
    public Optional<Training> add(Training training, Long trainingTypeId) {
        try {
            logger.info("Creating training with id: {}", training.getId());
            Session session = this.sessionFactory.getCurrentSession();
            Serializable generatedID = session.save(training);

            if (generatedID != null) {
                logger.info("Successfully created trainer with id: {}", training.getId());

                TrainingType trainingType = session
                        .getReference(TrainingType.class, trainingTypeId);

                training.setTrainingType(trainingType);

                return Optional.of(training);
            }
        } catch (HibernateError e) {
            logger.error(e.getMessage());
            throw new HibernateException(e);
        }

        return Optional.empty();
    }


}
