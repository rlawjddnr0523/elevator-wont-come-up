// 출근 시간: 한가한 엘리베이터는 로비(1층)로 복귀해 대기
public class RushHourPolicy extends StandardPolicy {
    
    @Override
    public void onIdle(Building b, Elevator e) {
        if (e.getCurrentLevel() != 1) {
            e.addDestination(1);
        }
    }
}
