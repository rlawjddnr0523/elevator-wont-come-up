// 모드별 배차 규칙. Building은 이 인터페이스만 알고, 모드 전환은 policy 교체로 한다.
public interface DispatchPolicy {
    
    // e가 level 층의 direction 호출을 맡을 수 있는지
    boolean canServe(Building b, Elevator e, int level, Direction direction);
    
    // 배정 비용: 작을수록 우선 (기본: 현재 층과의 거리)
    default int cost(Building b, Elevator e, int level, Direction direction) {
        return Math.abs(e.getCurrentLevel() - level);
    }
    
    // 이동 직후, 현재 층에서 정차할지 (prevDir: 이동 전 진행 방향)
    default boolean shouldStop(Elevator e, Floor here, Direction prevDir, boolean arrived) {
        boolean sameDirection =
                (prevDir == Direction.UP && here.isUp()) ||
                (prevDir == Direction.DOWN && here.isDown());
        // 목적지 도착/하차는 만원이어도 정차, 외부 호출 정차는 만원이면 무시
        return arrived || e.hasRiderFor(here.getLevel()) || (!e.isFull() && sameDirection);
    }
    
    // 이번 틱에 할 일이 없는 엘리베이터에 대한 행동
    default void onIdle(Building b, Elevator e) { }
}
