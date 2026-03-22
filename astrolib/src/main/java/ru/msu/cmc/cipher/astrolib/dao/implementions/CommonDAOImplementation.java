package ru.msu.cmc.cipher.astrolib.dao.implementions;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.dao.CommonDAO;
import ru.msu.cmc.cipher.astrolib.models.CommonEntity;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

@Repository
@Transactional
public abstract class CommonDAOImplementation<T extends CommonEntity<ID>, ID extends Serializable>
        implements CommonDAO<T, ID> {

    @PersistenceContext
    protected EntityManager entityManager;

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public CommonDAOImplementation() {
        this.entityClass = (Class<T>) ((ParameterizedType)
                getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public T getById(ID id) {
        return entityManager.find(entityClass, id);
    }

    @Override
    public Collection<T> getAll() {
        TypedQuery<T> query = entityManager.createQuery(
                "FROM " + entityClass.getSimpleName(),
                entityClass
        );
        return query.getResultList();
    }

    @Override
    public void insert(T entity) {
        entityManager.persist(entity);
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
    }

    @Override
    public void delete(T entity) {
        T managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    @Override
    public void deleteById(ID id) {
        T entity = getById(id);
        if (entity != null) {
            delete(entity);
        }
    }
}