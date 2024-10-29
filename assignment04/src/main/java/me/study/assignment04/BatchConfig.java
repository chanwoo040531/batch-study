package me.study.assignment04;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final static int CHUNK_SIZE = 1000;
    private final static String UTF_8 = "UTF-8";

    @Bean
    public static BeanDefinitionRegistryPostProcessor jobRegistryBeanPostProcessorRemover() {
        return registry -> registry.removeBeanDefinition("jobRegistryBeanPostProcessor");
    }

    @Bean
    public FlatFileItemReader<CompositeIndex> ass04Reader() {
        return new FlatFileItemReaderBuilder<CompositeIndex>()
                .name("ass04Reader")
                .resource(new ClassPathResource("./flatfile.csv"))
                .encoding(UTF_8)
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("indicator", "m202403", "m202404", "m202405", "m202406", "m202407", "m202408")
                .targetType(CompositeIndex.class)
                .build()
                ;
    }

    public ItemProcessor<CompositeIndex, CompositeIndexRate> ass04Processor() {
        return item -> {
            log.info("Processing {}", item);
            return new CompositeIndexRate(
                    item.indicator(),
                    formatValue(item.m202403(), 0.0),
                    formatValue(item.m202404(), item.m202403()),
                    formatValue(item.m202405(), item.m202404()),
                    formatValue(item.m202406(), item.m202405()),
                    formatValue(item.m202407(), item.m202406()),
                    formatValue(item.m202408(), item.m202407())
            );
        };
    }

    @Bean
    public FlatFileItemWriter<CompositeIndexRate> ass04Writer() {
        FlatFileItemWriter<CompositeIndexRate> itemWriter = new FlatFileItemWriterBuilder<CompositeIndexRate>()
                .name("ass04Writer")
                .resource(new FileSystemResource("./output.csv"))
                .encoding(UTF_8)
                .delimited()
                    .delimiter(",")
                .names("indicator", "m202403", "m202404", "m202405", "m202406", "m202407", "m202408")
                .headerCallback((writer) -> writer.write(
                        "\"지수별\",2024.03,2024.04,2024.05,2024.06 p),2024.07 p),2024.08 p)"))
                .build();
        return itemWriter;
    }

    @Bean
    public Step ass04Step(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("ass04Step", jobRepository)
                .<CompositeIndex, CompositeIndexRate>chunk(CHUNK_SIZE, transactionManager)
                .reader(ass04Reader())
                .processor(ass04Processor())
                .writer(ass04Writer())
                .build();
    }

    @Bean
    public Job ass04Job(JobRepository jobRepository, Step ass04Step) {
        return new JobBuilder("ass04Job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(ass04Step)
                .build();
    }

    private String formatValue(Double value, Double previousValue) {
        double rate = (value - previousValue) * 0.01;
        return String.format("%f(%.2f)", value, rate);
    }
}

