package LLD.ConnectionPoolLLD;

import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnectionFactory implements ConnectionFactory {
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    @Override
    public Connection createConnection() {
        String connectionId = "conn-" + idGenerator.incrementAndGet();
        return new RealDatabaseConnection(connectionId);
    }
}
