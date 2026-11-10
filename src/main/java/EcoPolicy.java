// 절전: 전체의 절반(최소 1대)만 가동하고, 나머지는 대기시킨다
public class EcoPolicy extends StandardPolicy {
    
    @Override
    public boolean canServe(Building b, Elevator e, int level, Direction direction) {
        int total = b.getElevators().size();
        int active = Math.max(1, (total + 1) / 2);
        if (b.getElevators().indexOf(e) >= active) return false;
        return super.canServe(b, e, level, direction);
    }
}
