package me.study.assignment08;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
public class BatchConfig {
    private final static int CHUNK_SIZE = 10;

    private final ItemProcessor<Customer, CustomerUpdate> lowerCaseItemProcessor =
            (item) -> new CustomerUpdate(
                    item.id(),
                    item.name().toLowerCase(),
                    item.age(),
                    item.gender()
            );

    private final ItemProcessor<CustomerUpdate, CustomerUpdate> after20YearsItemProcessor =
            (item) -> new CustomerUpdate(
                    item.id(),
                    item.name(),
                    item.age() + 20,
                    item.gender()
            );

    @Bean
    public MyBatisPagingItemReader<Customer> pagingItemReader(SqlSessionFactory sqlSessionFactory) {

        return new MyBatisPagingItemReaderBuilder<Customer>()
                .sqlSessionFactory(sqlSessionFactory)
                .pageSize(CHUNK_SIZE)
                .queryId("me.study.assignment08.selectCustomers")
                .build();
    }


    @Bean
    public ItemProcessor<Customer, CustomerUpdate> processor() {
        return new CompositeItemProcessorBuilder<Customer, CustomerUpdate>()
                .delegates(List.of(
                        lowerCaseItemProcessor,
                        after20YearsItemProcessor))
                .build();
    }

    @Bean
    public ItemWriter<CustomerUpdate> writer(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisBatchItemWriterBuilder<CustomerUpdate>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("me.study.assignment08.updateCustomers")
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
