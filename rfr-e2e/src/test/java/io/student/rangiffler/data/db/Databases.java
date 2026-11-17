package io.student.rangiffler.data.db;

import com.p6spy.engine.event.CompoundJdbcEventListener;
import com.p6spy.engine.event.DefaultEventListener;
import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.student.rangiffler.config.TestConfig;

import java.util.List;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public enum Databases {
    AUTH("rangiffler-auth"),
    API("rangiffler-api");

    private final String schema;
    private volatile DataSource dataSource;
    private volatile DataSourceTransactionManager transactionManager;

    Databases(String schema) {
        this.schema = schema;
    }

    public String schema() {
        return schema;
    }

    public synchronized DataSource dataSource() {
        if (dataSource == null) {
            HikariDataSource created = new HikariDataSource();
            created.setJdbcUrl(TestConfig.dbUrl(schema));
            created.setUsername(TestConfig.dbUser());
            created.setPassword(TestConfig.dbPassword());
            created.setMaximumPoolSize(4);
            created.setPoolName("rfr-e2e-" + schema);
            P6DataSource spied = new P6DataSource(created);
            spied.setJdbcEventListenerFactory(
                    () -> new CompoundJdbcEventListener(List.of(DefaultEventListener.INSTANCE, new AllureSql())));
            dataSource = spied;
        }
        return dataSource;
    }

    public synchronized PlatformTransactionManager transactionManager() {
        if (transactionManager == null) {
            transactionManager = new DataSourceTransactionManager(dataSource());
        }
        return transactionManager;
    }
}
