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
    
    enum Status {
        RUNNING,
        WAITING,
        MAX_CAPACITY,
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
    
    public void addDestination(int level) {
        if (!destination.contains(level)) {
            destination.add(level);
        }
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
            case UP -> currentLevel++;
            case DOWN -> currentLevel--;
            case NONE -> { }
        }
    }
}
