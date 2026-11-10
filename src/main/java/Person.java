import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Person {
    int currentLevel;
    int destinationLevel;
    
    public Person(int currentLevel, int destinationLevel) {
        this.currentLevel = currentLevel;
        this.destinationLevel = destinationLevel;
    }
    
    public Direction direction() {
        return destinationLevel > currentLevel ? Direction.UP : Direction.DOWN;
    }
}
