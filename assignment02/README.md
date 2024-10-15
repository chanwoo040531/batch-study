# 과제 02

## 스프링 배치 아키텍처

### 스프링 배치 모델
- Tasklet
  - 단순한 처리 모델
  - 유연한 사용 가능
- Chunk
  - 데이터량이 큰 작업에 적합
  - Reader, Processor, Writer로 구성


## 스프링 배치의 기본 아키텍처
 ```mermaid
classDiagram
  class JobRepository

  class JobLauncher
  class Job
  class Step

  class ItemReader
  class ItemProcessor
  class ItemWriter

  JobLauncher -- Job
  Job -- Step: 1 N
  JobLauncher <--> JobRepository
  Job <--> JobRepository
  Step <--> JobRepository
  
  Step -- ItemReader: 1
  Step -- ItemProcessor: 1
  Step -- ItemWriter: 1
```
- Job: 배치 작업의 실행 단위
- Step: 
  - Job을 구성하는 단위 (job : step = 1 : N)
  - Tasklet, Chunk 모델의 구현체 탑재되어 실행
- JobLauncher: 
  - Job을 실행하는 인터페이스
  - 사용자에 의해 직접 실행됨
- ItemReader: 데이터를 읽어오는 역할
- ItemProcessor: 데이터를 가공하는 역할
- ItemWriter: 데이터를 저장 및 수정하는 역할
- Tasklet: 단순한 처리 모델
- JobRepository: JobLauncher, Job, Step의 상태정보를 저장하는 저장소

### 스프링 배치 흐름
```mermaid
stateDiagram-v2
  [*] --> JobLauncher: run()
  state JavaProcess {
    JobLauncher --> Job: execute()
    Job --> Step: execute()
    state StepExecutionContext {
      Step --> ItemReader: read()
      Step --> ItemProcessor: process()
      Step --> ItemWriter: write()
    }
    JobLauncher --> JobRepository
    Job --> JobRepository
    Step --> JobRepository
  }

  JobRepository --> Database
  Database --> JobRepository
```
