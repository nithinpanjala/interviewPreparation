package LLD.ConnectionPoolLLD;

public class RealDatabaseConnection implements Connection {
    private final String connectionId;
    private volatile boolean closed = false;

    public RealDatabaseConnection(String connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public boolean isValid() {
        return !closed;
    }

    @Override
    public void executeQuery(String query) {
        if (closed) throw new IllegalStateException("Connection " + connectionId + " is closed");
        System.out.println("[" + connectionId + "] executing: " + query);
    }

    @Override
    public void close() {
        closed = true;
        System.out.println("[" + connectionId + "] physically closed");
    }

    public String getConnectionId() {
        return connectionId;
    }
}

