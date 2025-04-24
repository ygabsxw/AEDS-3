package file;

import java.util.List;

public interface FileManager<T> {
    int create(T obj) throws Exception;
    int createAfterOrder(T obj) throws Exception;
    T read(int id) throws Exception;
    boolean update(T obj) throws Exception;
    boolean delete(int id) throws Exception;
    List<T> readAll() throws Exception;
    int getNextId() throws Exception;
    void clear() throws Exception;
    void close() throws Exception;
}