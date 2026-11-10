// 기본: 가장 가까운 가용 엘리베이터, 한가할 땐 제자리 대기
public class StandardPolicy implements DispatchPolicy {
    
    @Override
    public boolean canServe(Building b, Elevator e, int level, Direction direction) {
        if (e.isFull()) return false;
        Direction current = e.getDirection();
        if (current == Direction.NONE) return true;
        if (current != direction) return false;
        return direction == Direction.UP
                ? e.getCurrentLevel() <= level
                : e.getCurrentLevel() >= level;
    }
}
