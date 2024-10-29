package me.study.assignment03;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final static int CHUNK_SIZE = 1000;

    @Bean
    public static BeanDefinitionRegistryPostProcessor jobRegistryBeanPostProcessorRemover() {
        return registry -> registry.removeBeanDefinition("jobRegistryBeanPostProcessor");
    }

    @Bean
    @Profile("cursor")
    public JdbcCursorItemReader<Coupon> cursorItemReader(DataSource dataSource) {
        String query =
                "SELECT id, code, discount, state, expired_at " +
                "FROM coupon.coupons " +
                "WHERE state = ? AND expired_at < ?";

        LocalDateTime todaysMidnight = LocalDate.now().atStartOfDay();

        JdbcCursorItemReader<Coupon> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(query);
        reader.setPreparedStatementSetter(ps -> {
            ps.setString(1, "AVAILABLE");
            ps.setTimestamp(2, Timestamp.valueOf(todaysMidnight));
        });

        reader.setRowMapper((rs, rowNum) -> {
            Long id = rs.getLong("id");
            String code = rs.getString("code");
            Double discount = rs.getDouble("discount");
            String state = rs.getString("state");
            LocalDateTime expiredAt = rs.getTimestamp("expired_at").toLocalDateTime();

            return new Coupon(id, code, discount, state, expiredAt);
        });

        return reader;
    }

    @Bean
    @Profile("paging")
    public JdbcPagingItemReader<Coupon> pagingItemReader(DataSource dataSource) {
        JdbcPagingItemReader<Coupon> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(CHUNK_SIZE);
        reader.setPageSize(CHUNK_SIZE);

        reader.setRowMapper((rs, rowNum) -> {
            Long id = rs.getLong("id");
            String code = rs.getString("code");
            Double discount = rs.getDouble("discount");
            String state = rs.getString("state");
            LocalDateTime expiredAt = rs.getTimestamp("expired_at").toLocalDateTime();

            return new Coupon(id, code, discount, state, expiredAt);
        });

        // Database 마다 다른 Paging 처리 방식을 사용함에 따라 각 DB에 맞는 QueryProvider를 사용해야 함
        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("id, code, discount, state, expired_at");
        queryProvider.setFromClause("coupon.coupons");
        queryProvider.setWhereClause("state = :state AND expired_at < :expiredAt");

        queryProvider.setSortKeys(Map.of("id", Order.ASCENDING));

        LocalDateTime todaysMidnight = LocalDate.now().atStartOfDay();

        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("state", "AVAILABLE");
        parameters.put("expiredAt", todaysMidnight);

        reader.setParameterValues(parameters);

        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public ItemProcessor<Coupon, Coupon> processor() {
        return (coupon) -> {
            coupon.setState("EXPIRED");
            return coupon;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Coupon> writer(DataSource dataSource) {
        JdbcBatchItemWriter<Coupon> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("UPDATE coupon.coupons SET state = :state WHERE id = :id");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        return writer;
    }

    @Bean
    public Step myStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Coupon> reader,
            ItemProcessor<Coupon, Coupon> processor,
            ItemWriter<Coupon> writer
    ) {
        log.debug("Read data from {}", reader.getClass().getSimpleName());

        return new StepBuilder("myStep", jobRepository)
                .<Coupon, Coupon>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job myJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("myJob", jobRepository)
                .start(step)
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coupon {
        private Long id;
        private String code;
        private Double discount;
        private String state;
        private LocalDateTime expiredAt;
    }
}
