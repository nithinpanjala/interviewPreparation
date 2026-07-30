package LLD.ConnectionPoolLLD;

public class PooledConnection implements Connection {
    private final Connection delegate;
    private final ConnectionPool ownerPool;
    private volatile boolean returned = false;

    public PooledConnection(Connection delegate, ConnectionPool ownerPool) {
        this.delegate = delegate;
        this.ownerPool = ownerPool;
    }

    @Override
    public boolean isValid() {
        return delegate.isValid();
    }

    @Override
    public void executeQuery(String query) {
        if (returned) throw new IllegalStateException("Connection already released to pool");
        delegate.executeQuery(query);
    }

    @Override
    public void close() {
        // Caller thinks they're closing it — really returning to pool.
        if (!returned) {
            returned = true;
            ownerPool.releaseConnection(this);
        }
    }

    Connection getDelegate() {
        return delegate;
    }
}
