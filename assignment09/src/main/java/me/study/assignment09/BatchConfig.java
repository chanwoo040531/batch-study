package me.study.assignment09;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final static int CHUNK_SIZE = 10;
    private final DataSourceConfig.DataSourceRouter dataSourceRouter;

    @PersistenceContext
    public EntityManager entityManager;

    @Bean
    public ItemReader<Customer> pagingItemReader(EntityManagerFactory entityManagerFactory) {
        return new QuerydslPagingItemReader<>(
                "pagingItemReader",
                entityManager,
                dataSourceRouter,
                jpaQueryFactory -> jpaQueryFactory
                        .selectFrom(QCustomer.customer)
                        .orderBy(QCustomer.customer.id.asc()),
                CHUNK_SIZE,
                false
        );
    }

    @Bean
    public ItemProcessor<Customer, Customer> processor() {

        return (customer) -> {
            log.info(customer.getName());
            return customer;
        };
    }

    @Bean
    public ItemWriter<Customer> writer(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Customer>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step myStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Customer> reader,
            ItemProcessor<Customer, Customer> processor,
            ItemWriter<Customer> writer
    ) {
        return new StepBuilder("myJpaStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Step myStep2(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Customer> reader,
            ItemWriter<Customer> writer
    ) {
        return new StepBuilder("myJpaStep2", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor((item) -> {
                    log.info(item.getName());
                    return item;
                })
                .writer(writer)
                .build();
    }

    @Bean
    public Job myJob(JobRepository jobRepository, Step myStep, Step myStep2) {
        return new JobBuilder("myJpaJob", jobRepository)
                .start(myStep)
                .next(myStep2)
                .build();
    }
}
