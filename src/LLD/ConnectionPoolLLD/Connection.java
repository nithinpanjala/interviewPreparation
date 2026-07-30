package LLD.ConnectionPoolLLD;

public interface Connection extends AutoCloseable {
    boolean isValid();
    void executeQuery(String query);
    @Override
    void close();
}
