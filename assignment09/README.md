# 과제 9

## Custom Item Reader & Writer
특이 케이스 및 특정 비즈니스 로직 처리를 위해 Custom Item Reader & Writer를 구현 가능
### Reader
ItemReader 혹은 AbstractPagingItemReader를 상속받아 구현

### Writer
ItemWriter를 상속받아 구현

## Flow Control
Step을 건너뛰거나 제어하기 위해 사용

- next: 현재 step이 성공할 경우 다음 step으로 이동
- to: 특정 step으로 이동
- end: Flow 종료
- from: 특정 step에서 특정 step으로 이동
- on: 특정 ExitStatus이 발생할 경우 이동할 step 지정