// 최대한 빨리: ETA(이동 + 중간 정차 시간)가 가장 짧은 엘리베이터에 배정하고,
// 한가한 엘리베이터는 층 전체에 고르게 분산해 대기시킨다
public class FastPolicy extends StandardPolicy {
    
    @Override
    public boolean canServe(Building b, Elevator e, int level, Direction direction) {
        if (e.isParking()) return !e.isFull();   // 대기 위치로 이동 중이면 한가한 것으로 본다
        return super.canServe(b, e, level, direction);
    }
    
    @Override
    public int cost(Building b, Elevator e, int level, Direction direction) {
        if (e.isParking() || e.getDestination().isEmpty()) {
            return Math.abs(e.getCurrentLevel() - level);
        }
        return e.etaTo(level);
    }
    
    @Override
    public void onIdle(Building b, Elevator e) {
        int home = homeFloor(b, e);
        if (e.getCurrentLevel() != home) {
            e.addDestination(home);
            e.setParking(true);
        }
    }
    
    // n대를 층 범위에 균등 분산: 예) 10층 2대 → 3층, 8층
    static int homeFloor(Building b, Elevator e) {
        int n = b.getElevators().size();
        int i = b.getElevators().indexOf(e);
        int floors = b.getFloors().size();
        return 1 + (int) Math.round((floors - 1) * (2.0 * i + 1) / (2.0 * n));
    }
}
