import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Elevator {
    int maxCapacity;      // 최대 정원 (명)
    List<Person> riders = new ArrayList<>();   // 현재 탑승자
    Status status = Status.IDLE;
    DoorStatus doorStatus = DoorStatus.CLOSED;
    List<Integer> destination = new ArrayList<>();
    int currentLevel;
    boolean parking;      // 한가해서 대기 위치로 이동 중 (호출이 오면 언제든 취소)
    int movedFloors;      // 누적 이동 층수
    
    enum Status {
        RUNNING,
        WAITING,
        IDLE,
    }
    
    enum DoorStatus {
        OPEN,
        CLOSED,
    }
    
    public Direction getDirection() {
        if (destination == null || destination.isEmpty()) {
            return Direction.NONE;
        }
        if (currentLevel < destination.getFirst()) {
            return Direction.UP;
        } else if (currentLevel > destination.getFirst()) {
            return Direction.DOWN;
        }
        return Direction.NONE;
    }
    
    // 목적지를 추가하고 SCAN 순서(가는 방향으로 먼저 훑고, 반대편은 돌아오며)로 정렬한다
    public void addDestination(int level) {
        if (destination.contains(level)) return;
        List<Integer> route = routeWith(level);
        destination.clear();
        destination.addAll(route);
    }
    
    // level을 추가했을 때의 SCAN 경로 (이 엘리베이터는 바꾸지 않는다)
    public List<Integer> routeWith(int level) {
        List<Integer> all = new ArrayList<>(destination);
        if (!all.contains(level)) all.add(level);
        
        Direction heading = getDirection();
        if (heading == Direction.NONE) {
            heading = level < currentLevel ? Direction.DOWN : Direction.UP;
        }
        
        List<Integer> ahead = new ArrayList<>();
        List<Integer> behind = new ArrayList<>();
        for (int l : all) {
            boolean isAhead = heading == Direction.UP ? l >= currentLevel : l <= currentLevel;
            (isAhead ? ahead : behind).add(l);
        }
        if (heading == Direction.UP) {
            ahead.sort(null);
            behind.sort(java.util.Comparator.reverseOrder());
        } else {
            ahead.sort(java.util.Comparator.reverseOrder());
            behind.sort(null);
        }
        ahead.addAll(behind);
        return ahead;
    }
    
    // level 층에 도착하기까지 걸리는 예상 틱 (이동 + 중간 정차당 문 여닫기 2틱)
    public int etaTo(int level) {
        int pos = currentLevel;
        int t = doorStatus == DoorStatus.OPEN ? 1 : 0;
        for (int stop : routeWith(level)) {
            t += Math.abs(stop - pos);
            pos = stop;
            if (stop == level) return t;
            t += 2;
        }
        return t;
    }
    
    public int getPassengerCount() {
        return riders.size();
    }
    
    public boolean isFull() {
        return getPassengerCount() >= maxCapacity;
    }
    
    // level 층에서 내릴 탑승자가 있는지
    public boolean hasRiderFor(int level) {
        return riders.stream().anyMatch(p -> p.getDestinationLevel() == level);
    }
    
    // 목적지 방향으로 한 칸 이동 (목적지가 없거나 도착했으면 제자리)
    public void step() {
        switch (getDirection()) {
            case UP -> { currentLevel++; movedFloors++; }
            case DOWN -> { currentLevel--; movedFloors++; }
            case NONE -> { }
        }
    }
}
