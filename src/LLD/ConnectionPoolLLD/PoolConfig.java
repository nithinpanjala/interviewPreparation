package LLD.ConnectionPoolLLD;


public class PoolConfig {
    final int minSize;
    final int maxSize;
    final long borrowTimeoutMillis;
    final long idleValidationIntervalMillis;

    public PoolConfig(int minSize, int maxSize, long borrowTimeoutMillis, long idleValidationIntervalMillis) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.borrowTimeoutMillis = borrowTimeoutMillis;
        this.idleValidationIntervalMillis = idleValidationIntervalMillis;
    }
}

