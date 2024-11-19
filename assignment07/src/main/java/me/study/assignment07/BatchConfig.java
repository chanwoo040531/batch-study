package me.study.assignment07;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class BatchConfig {
    private final static int CHUNK_SIZE = 10;

    @Bean
    public MyBatisPagingItemReader<Customer> pagingItemReader(SqlSessionFactory sqlSessionFactory) {

        return new MyBatisPagingItemReaderBuilder<Customer>()
                .sqlSessionFactory(sqlSessionFactory)
                .pageSize(CHUNK_SIZE)
                .queryId("me.study.assignment07.selectCustomers")
                .build();
    }

    @Bean
    public ItemProcessor<Customer, CustomerUpdate> processor() {
        return (customer) -> {
            int updatedAge = customer.age() + 1;
            return new CustomerUpdate(customer.id(), updatedAge);
        };
    }

    @Bean
    public ItemWriter<CustomerUpdate> writer(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisBatchItemWriterBuilder<CustomerUpdate>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("me.study.assignment07.updateCustomers")
                .itemToParameterConverter(item -> item)
                .build();
    }

    @Bean
    public Step myStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Customer> reader,
            ItemProcessor<Customer, CustomerUpdate> processor,
            ItemWriter<CustomerUpdate> writer
    ) {
        return new StepBuilder("myBatisStep", jobRepository)
                .<Customer, CustomerUpdate>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job myJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("myBatisJob", jobRepository)
                .start(step)
                .build();
    }
}
