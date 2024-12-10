package me.study.assignment09;

import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "me.study.assignment09")
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder
                .create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.standby")
    public DataSource standbyDataSource() {
        return DataSourceBuilder
                .create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @DependsOn({ "primaryDataSource", "standbyDataSource" })
    public DataSourceRouter datasourceRouter() {
        DataSource primaryDataSource = primaryDataSource();
        DataSource standbyDataSource = standbyDataSource();

        ConcurrentHashMap<Object, Object> targetDataSources = new ConcurrentHashMap<>();
        targetDataSources.put(DataSourceType.PRIMARY, primaryDataSource);
        targetDataSources.put(DataSourceType.STANDBY, standbyDataSource);

        DataSourceRouter dataSourceRouter = new DataSourceRouter();
        dataSourceRouter.setTargetDataSources(targetDataSources);
        dataSourceRouter.setDefaultTargetDataSource(standbyDataSource);

        return dataSourceRouter;
    }

    @Bean
    @Primary
    @DependsOn("datasourceRouter")
    public DataSource dataSource(DataSourceRouter dataSourceRouter) {
        return new LazyConnectionDataSourceProxy(dataSourceRouter);
    }

    public static class DataSourceRouter extends AbstractRoutingDataSource {

        @Override
        protected Object determineCurrentLookupKey() {
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            if (readOnly) {
                return DataSourceType.STANDBY;
            }
            return DataSourceType.PRIMARY;
        }
    }

    private enum DataSourceType {
        PRIMARY, STANDBY
    }
}
