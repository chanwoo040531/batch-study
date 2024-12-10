package me.study.assignment09;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

@Slf4j
@Transactional(readOnly = true)
public class QuerydslPagingItemReader<T> extends AbstractPagingItemReader<T> {
    private final Function<JPAQueryFactory, JPAQuery<T>> querySupplier;
    private final Boolean alwaysReadFromZero;
    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;
    private final DataSourceConfig.DataSourceRouter dataSourceRouter;

    public QuerydslPagingItemReader(
            String name,
            EntityManager em,
            DataSourceConfig.DataSourceRouter dataSourceRouter,
            Function<JPAQueryFactory, JPAQuery<T>> querySupplier,
            int chunkSize,
            Boolean alwaysReadFromZero
    ) {
        super.setPageSize(chunkSize);
        setName(name);
        this.querySupplier = querySupplier;
        this.jpaQueryFactory = new JPAQueryFactory(em);
        this.em = em;
        this.dataSourceRouter = dataSourceRouter;
        this.alwaysReadFromZero = alwaysReadFromZero;

    }

    @Override
    protected void doReadPage() {
        initQueryResult();

        em.flush();
        em.clear();

        long offset = (!alwaysReadFromZero)
                ? (long) getPage() * getPageSize()
                : 0;

        JPAQuery<T> query = querySupplier
                .apply(jpaQueryFactory)
                .offset(offset)
                .limit(getPageSize());


        List<T> queryResult = query.fetch();

        for (T entity : queryResult) {
            em.detach(entity);
            results.add(entity);
        }
    }

    @Override
    protected void doClose() throws Exception {
        if (em != null) {
            em.close();
        }
        super.doClose();
    }

    private void initQueryResult() {
        if (CollectionUtils.isEmpty(results)) {
            results = new CopyOnWriteArrayList<>();
            return;
        }
        results.clear();
    }
}
