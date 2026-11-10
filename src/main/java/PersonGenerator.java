import java.util.concurrent.ThreadLocalRandom;

// 틱마다 확률적으로 임의의 호출(1~3명)을 만든다
public class PersonGenerator {
    private double rate;   // 틱당 호출 발생 확률 (0.0 ~ 1.0)
    private boolean enabled = false;
    
    public PersonGenerator(double rate) {
        this.rate = rate;
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
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if (r.nextDouble() >= rate) return;
        
        int floors = building.getFloors().size();
        int level = r.nextInt(1, floors + 1);
        Direction dir;
        if (level == 1) dir = Direction.UP;
        else if (level == floors) dir = Direction.DOWN;
        else dir = r.nextBoolean() ? Direction.UP : Direction.DOWN;
        
        building.call(level, dir, r.nextInt(1, 4), null);
    }
}
