package de.hilling.junit.cdi.jee.jpa.eclipselink;

import java.sql.Connection;
import java.sql.SQLException;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import de.hilling.junit.cdi.jee.TestEntityResources;
import de.hilling.junit.cdi.jee.jpa.ConnectionWrapper;
import de.hilling.junit.cdi.jee.jpa.DatabaseCleaner;

@RequestScoped
public class EclipselinkConnectionWrapper implements ConnectionWrapper {

    @Inject
    private Instance<DatabaseCleaner> cleaner;

    @Inject
    private TestEntityResources entityManagers;

    @Override
    public void callDatabaseCleaner(EntityManager entityManager) {
        cleanEntityManager(entityManager);
    }

    private void cleanEntityManager(EntityManager entityManager) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Connection connection = entityManager.unwrap(Connection.class);
            if (connection == null) {
                transaction.rollback();
            } else {
                if (!cleaner.isUnsatisfied()) {
                    try {
                        cleaner.get().run(connection);
                    } catch (SQLException e) {
                        throw new RuntimeException("cleaning database failed", e);
                    }
                }
                transaction.commit();
            }
        } catch (RuntimeException re) {
            transaction.rollback();
        }
    }
}
