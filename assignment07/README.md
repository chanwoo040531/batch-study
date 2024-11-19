# 과제 7

## MyBatisItemReader

MyBatis를 사용하여 DB에서 Page 단위로 데이터를 읽어오는 Reader

### 주요 구성요소

- **queryId**: MyBatis Mapper의 쿼리 ID
- **SqlSessionFactory**: MyBatis 설정 정보 및 SQL 쿼리 매퍼 정보를 담고 있는 객체
    - Bean, Spring Batch XML, Java Config를 통해 설정 가능
- **pageSize**: 페이지 단위로 읽어올 데이터의 크기

### 장단점

| 장점                                       | 단점                           |
|------------------------------------------|------------------------------|
| - 간편한 설정<br/> - 최적화된 쿼리 <br/> - 동적 쿼리 지원 | - MyBatis 의존성 <br/> - 낮은 유연성 |

### 주의할 점

PagingItemReader를 사용할 때 **정렬 키**를 설정하지 않으면 각각의 Chunk가 각기다른 정렬 조건을 가지고 처리를 수행할 수 있음

```sql
SELECT id, name, age, gender
FROM account.customers
ORDER BY id -- 정렬 키 설정
OFFSET #{_skiprows} LIMIT #{_pagesize}
```

## MyBatisItemWriter

MyBatis를 사용하여 DB에 데이터를 저장하는 Writer

### 주요 구성요소

- **SqlSessionTemplate**: SqlSession을 생성 및 관리
- **SqlSessionFactory**: **SqlSessionTemplate**을 생성하기 위한 객체
- **statementId**: MyBatis Mapper의 쿼리 ID
- **itemToParameter**: Item을 SQL 쿼리의 파라미터로 변환하는 함수

### 장단점

| 장점                    | 단점       |
|-----------------------|----------|
| - MyBatis를 사용 가능<br/> | - 낮은 유연성 |
