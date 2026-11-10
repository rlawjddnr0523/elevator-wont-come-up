import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Person {
    int currentLevel;
    int destinationLevel;
    int callTime;           // 호출한 틱
    int boardTime = -1;     // 탑승한 틱
    int arriveTime = -1;    // 도착한 틱
    
    public Person(int currentLevel, int destinationLevel, int callTime) {
        this.callTime = callTime;
        this.currentLevel = currentLevel;
        this.destinationLevel = destinationLevel;
    }
    
    public Direction direction() {
        return destinationLevel > currentLevel ? Direction.UP : Direction.DOWN;
    }
}
