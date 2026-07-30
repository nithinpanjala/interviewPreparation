package LLD.ConnectionPoolLLD;

public class ConnectionPoolDemo {
    public static void main(String[] args) throws Exception {
        PoolConfig config = new PoolConfig(2, 5, 3000, 30000);
        ConnectionPool pool = new ConnectionPool(new DatabaseConnectionFactory(), config);

        try (Connection conn = pool.getConnection()) {
            conn.executeQuery("SELECT * FROM orders");
        } // close() returns it to pool, doesn't destroy it

        pool.shutdown();
    }
}
