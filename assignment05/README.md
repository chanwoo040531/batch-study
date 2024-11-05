# 과제 05

## JdbcPagingItemReader
### 주요 구성요소
- **SkippableItemReader**: 예외 발생 시 해당 Item을 Skip하고 계속 진행
- **ReadListener**: 읽기 시작, 종료, 오류 발생 등의 이벤트를 처리를 위한 Listener
- **SaveStateCallback**: 상태 저장 및 복원을 위한 Callback

## JdbcBatchItemWriter
### 주요 구성요소
- **SqlStatementCreator**: 쿼리를 생성
- **ItemPreparedStatementSetter**: 쿼리의 파라미터값을 설정
- **ItemSqlParameterSourceProvider**: ItemPreparedStatementSetter에 전달할 파라미터를 생성
