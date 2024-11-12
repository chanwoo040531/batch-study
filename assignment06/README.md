# 과제 6

## JpaPagingItemReader
JPA를 사용하여 DB에서 Page 단위로 데이터를 읽어오는 Reader

### 주요 구성요소
- pageSize: 페이지 크기
- SkippableItemReader: 오류 발생 시 해당 Item을 건너뛸 수 있도록 함
- ReadListener: 읽기 시작, 종료, 오류 발생 등의 이벤트 처리

## JpaItemWriter

### 장점
- JPA를 사용하여 DB에 데이터를 저장할 수 있음
- 객체 매핑 가능
- 높은 유연성

### 단점
- 설정 복잡성
- 오류 가능성