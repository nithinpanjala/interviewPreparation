package LLD.ConnectionPoolLLD;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPool {
    private final ConnectionFactory factory;
    private final PoolConfig config;
    private final BlockingQueue<Connection> freeConnections;
    private final Set<Connection> activeConnections;
    private final Semaphore permits;                 // bounds total connections at maxSize
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean shutdown = false;

    public ConnectionPool(ConnectionFactory factory, PoolConfig config) {
        this.factory = factory;
        this.config = config;
        this.freeConnections = new LinkedBlockingQueue<>();
        this.activeConnections = ConcurrentHashMap.newKeySet();
        this.permits = new Semaphore(config.maxSize, true);  // fair queuing
        initializeMinConnections();
    }

    private void initializeMinConnections() {
        for (int i = 0; i < config.minSize; i++) {
            permits.acquireUninterruptibly();
            Connection conn = factory.createConnection();
            freeConnections.offer(conn);
        }
    }

    public Connection getConnection() throws InterruptedException, TimeoutException {
        if (shutdown) throw new IllegalStateException("Pool is shut down");

        boolean acquired = permits.tryAcquire(config.borrowTimeoutMillis, TimeUnit.MILLISECONDS);
        if (!acquired) {
            throw new TimeoutException("Timed out waiting for a connection");
        }

        Connection raw = freeConnections.poll();
        if (raw == null) {
            // permit says we're allowed to grow the pool
            raw = factory.createConnection();
        } else if (!raw.isValid()) {
            // stale connection — replace it
            raw = factory.createConnection();
        }

        PooledConnection pooled = new PooledConnection(raw, this);
        activeConnections.add(pooled);
        return pooled;
    }

    void releaseConnection(PooledConnection pooledConnection) {
        lock.lock();
        try {
            activeConnections.remove(pooledConnection);
            Connection raw = pooledConnection.getDelegate();
            if (raw.isValid() && !shutdown) {
                freeConnections.offer(raw);
            } else {
                raw.close();
            }
        } finally {
            lock.unlock();
            permits.release();
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            shutdown = true;
            freeConnections.forEach(Connection::close);
            freeConnections.clear();
            activeConnections.forEach(Connection::close);
            activeConnections.clear();
        } finally {
            lock.unlock();
        }
    }
}
