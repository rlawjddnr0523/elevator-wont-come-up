// 시뮬레이션 통계 (시간 단위: 틱)
public class Stats {
    private int called;          // 호출한 총 인원
    private int arrived;         // 목적지에 도착한 인원
    private long totalWait;      // 호출~탑승 합계
    private long totalRide;      // 탑승~도착 합계
    private int maxWait;
    
    public void recordCall(int count) {
        called += count;
    }
    
    public void recordArrival(Person p) {
        int wait = p.getBoardTime() - p.getCallTime();
        arrived++;
        totalWait += wait;
        totalRide += p.getArriveTime() - p.getBoardTime();
        maxWait = Math.max(maxWait, wait);
    }
    
    public int getCalled() { return called; }
    
    public int getArrived() { return arrived; }
    
    public int getMaxWait() { return maxWait; }
    
    public double avgWait() { return arrived == 0 ? 0 : (double) totalWait / arrived; }
    
    public double avgRide() { return arrived == 0 ? 0 : (double) totalRide / arrived; }
}
