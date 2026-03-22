package ru.msu.cmc.cipher.astrolib.dao;

import ru.msu.cmc.cipher.astrolib.models.CommonEntity;

import java.util.Collection;

public interface CommonDAO<T extends CommonEntity<ID>, ID> {
    //чтение
    T getById(ID id);
    Collection<T> getAll();

    //запись
    void insert(T entity);
    void update(T entity);

    //удаление
    void delete(T entity);
    void deleteById(ID id);
}
