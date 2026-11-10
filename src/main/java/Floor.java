import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Floor {
    int level;
    java.util.List<Person> waiting = new java.util.ArrayList<>();
    boolean isUp = false;
    boolean isDown = false;
    ButtonStatus button = ButtonStatus.IDLE;
    
    public static enum ButtonStatus {
        ACTIVE,
        IDLE
    }
    
    public Direction getDirection(int destination) {
        if (destination > level) {
            return Direction.UP;
        } else if (destination < level) {
            return Direction.DOWN;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
