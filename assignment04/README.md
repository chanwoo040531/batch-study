# 과제 3

## FlatFileItemReader

### 장단점
| 장점                                  | 단점                   |
|-------------------------------------|----------------------|
| - 효율적이고 간단한 구현<br/> - 다양한 텍스트 파일 처리 | - 복잡한 데이터 처리에 맞지 않음  |

### 설정
- `LineMapper`:
  - 텍스트 파일의 각 라인을 Item으로 변환
  - 설정하지 않을 시 DefaultLineMapper 사용
  - `LineTokenizer`:
    - 라인을 필드로 분리
    - 설정하지 않을 시 DelimitedLineTokenizer 사용
    - `LineTokenizer` 구현체:
      - `DelimitedLineTokenizer`: 구분자로 필드 분리
      - `FixedLengthTokenizer`: 고정 길이로 필드 분리
- `FieldSetMapper`: 토큰을 Item 프로퍼티에 매핑
  

<details>
<summary>FlatFileItemReaderBuilder 소스코드</summary>

```java
reader.setResource(this.resource);

if (this.lineMapper != null) {
    reader.setLineMapper(this.lineMapper);
}
else {
    Assert.state(validatorValue == 0 || validatorValue == 1 || validatorValue == 2 || validatorValue == 4,
            "Only one LineTokenizer option may be configured");

    DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();

    if (this.lineTokenizer != null) {
        lineMapper.setLineTokenizer(this.lineTokenizer);
    }
    else if (this.fixedLengthBuilder != null) {
        lineMapper.setLineTokenizer(this.fixedLengthBuilder.build());
    }
    else if (this.delimitedBuilder != null) {
        lineMapper.setLineTokenizer(this.delimitedBuilder.build());
    }
    else {
        throw new IllegalStateException("No LineTokenizer implementation was provided.");
    }
    // ...
```
</details>

## FlatFileItemWriter

### 장단점
| 장점                                            | 단점                                                                              |
|-----------------------------------------------|---------------------------------------------------------------------------------|
| - 간편성<br/> - 높은 유연성<br/> - 대량의 데이터를 빠르게 출력 가능 | - 텍스트 파일만을 지원<br/> - 복잡한 구조의 데이터의 경우 설정이 복잡해질 수 있음<br/> - 설정 오류 시 파일 손상될 가능성 존재 |

### 설정
- `LineAggregator`: Item을 문자열로 변환
  - `DelimitedLineAggregator`: 구분자로 필드 결합
  - `FormatterLineAggregator`: 포맷터로 필드 결합
- `HeaderCallback`: 
  - 출력 파일의 헤더를 지정
  - `AbstractFileItemWriter#doOpen`에서 호출
- `FooterCallback`:
  - 출력 파일의 푸터를 지정
  - `AbstractFileItemWriter#close`에서 호출
- `AppendMode`: 기존 파일에 추가할지(true) 덮어쓸지(false) 설정