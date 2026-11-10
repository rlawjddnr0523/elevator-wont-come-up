import java.util.Random;

// 틱마다 확률적으로 임의의 호출(1~3명)을 만든다
public class PersonGenerator {
    private double rate;   // 틱당 호출 발생 확률 (0.0 ~ 1.0)
    private boolean enabled = false;
    private final Random random;
    
    public PersonGenerator(double rate) {
        this(rate, new Random());
    }
    
    // 시드를 고정하면 같은 호출 패턴을 재현할 수 있다 (모드 비교용)
    public PersonGenerator(double rate, Random random) {
        this.rate = rate;
        this.random = random;
    }
    
    public boolean isEnabled() { return enabled; }
    
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public double getRate() { return rate; }
    
    public void setRate(double rate) {
        if (rate < 0 || rate > 1) {
            throw new IllegalArgumentException("확률은 0~1 사이여야 합니다.");
        }
        this.rate = rate;
    }
    
    public void generate(Building building) {
        if (!enabled) return;
        Random r = random;
        if (r.nextDouble() >= rate) return;
        
        int floors = building.getFloors().size();
        int level = 1 + r.nextInt(floors);
        Direction dir;
        if (level == 1) dir = Direction.UP;
        else if (level == floors) dir = Direction.DOWN;
        else dir = r.nextBoolean() ? Direction.UP : Direction.DOWN;
        
        building.call(level, dir, 1 + r.nextInt(3), null);
    }
}
