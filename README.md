# PointService 의 동시성 제어 방식
PointService 클래스는 포인트 충전, 사용, 조회 및 트랜잭션 기록과 관련된 비즈니스 로직을 처리하는 서비스입니다. 이 클래스는 동시성 문제를 방지하기 위해 ReentrantLock 과 CompletableFuture 를 함께 사용하고 있습니다. 

# ReentrantLock 의 역할
- lock.lock(): 메소드가 호출될 때, 첫 번째 스레드가 lock 을 획득하여 해당 코드 블록을 실행하는 동안 다른 스레드는 이 블록에 접근할 수 없습니다.
- lock.unlock(): 작업이 완료되면 lock 을 해제하여 대기 중인 스레드가 해당 블록에 접근할 수 있습니다.

# CompletableFuture 의 역할
- 비동기 처리: CompletableFuture.supplyAsync() 를 사용하여 포인트 충전 및 사용 작업을 비동기적으로 처리합니다. 이를 통해 메소드 호출자는 즉각적으로 작업을 반환받고, 작업이 완료되면 결과를 얻을 수 있습니다.
- 스레드 풀 활용: ExecutorService 를 통해 별도의 스레드에서 작업을 처리함으로써 메인 스레드의 부담을 줄이고, 시스템 자원을 효율적으로 활용합니다.
