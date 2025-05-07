package persistence.DAO;

import java.util.List;

public interface DAO <T> {

    void add(T t) throws Exception;
    List<T> getAll() throws Exception;
    void delete(T t) throws Exception;
    T getOne(String id) throws Exception;
    void update(T t) throws Exception;

}
