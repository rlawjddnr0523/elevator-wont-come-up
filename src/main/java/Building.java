import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Building {
    private final List<Floor> floors = new ArrayList<>();
    private final List<Elevator> elevators = new ArrayList<>();
    private final Stats stats = new Stats();
    private int time = 0;   // 현재 틱
    private boolean autoMode = false;   // true면 부하에 따라 모드를 자동 전환
    private int quietTicks = 0;           // 연속으로 한가했던 틱 수
    private Mode mode = Mode.STANDARD;
    private DispatchPolicy policy = mode.policy();
    
    public void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }
    
    public void setMode(Mode mode) {
        this.autoMode = false;   // 수동으로 고르면 자동 전환 해제
        this.mode = mode;
        this.policy = mode.policy();
    }
    
    public Building(int floorCount, int elevatorCount, int maxCapacity) {
        for (int i = 1; i <= floorCount; i++) {
            Floor f = new Floor();
            f.setLevel(i);
            floors.add(f);
        }
        for (int i = 0; i < elevatorCount; i++) {
            Elevator e = new Elevator();
            e.setMaxCapacity(maxCapacity);
            e.setCurrentLevel(1);
            elevators.add(e);
        }
    }
    
    public Floor getFloor(int level) {
        return floors.get(level - 1);
    }
    
    // 부하 기반 자동 전환: 평소엔 FAST, 한동안 한가하면 ECO(절전), 대기가 생기면 다시 FAST
    private void updateMode() {
        int waiting = floors.stream().mapToInt(f -> f.getWaiting().size()).sum();
        int riding = elevators.stream().mapToInt(Elevator::getPassengerCount).sum();
        quietTicks = (waiting == 0 && riding == 0) ? quietTicks + 1 : 0;
        
        Mode next;
        if (mode == Mode.ECO) {
            next = waiting >= 3 ? Mode.FAST : Mode.ECO;   // 잦은 전환 방지: 대기가 꽤 쌓여야 복귀
        } else {
            next = quietTicks >= 10 ? Mode.ECO : Mode.FAST;
        }
        if (next != mode) {
            this.mode = next;
            this.policy = next.policy();
        }
    }
    
    private record Call(int level, Direction direction) { }
    
    // 배정 못 받은 호출 (다음 틱에 재시도)
    private final List<Call> pending = new ArrayList<>();
    
    // 시간을 한 단위 진행: 대기 호출 재배정 → (문 닫기 | 한 칸 이동 + 정차 판단)
    public void tick() {
        time++;
        if (autoMode) updateMode();
        for (Call c : new ArrayList<>(pending)) {
            if (assign(c.level(), c.direction()) != null) {
                pending.remove(c);
            }
        }
        
        for (Elevator e : elevators) {
            // 문이 열려 있었다면 이번 틱은 문을 닫는 데 쓴다 (이동 없음): 열림 1틱 → 닫힘 1틱
            if (e.getDoorStatus() == Elevator.DoorStatus.OPEN) {
                e.setDoorStatus(Elevator.DoorStatus.CLOSED);
                if (e.getDestination().isEmpty()) {
                    policy.onIdle(this, e);
                }
                e.setStatus(e.getDirection() == Direction.NONE
                        ? Elevator.Status.IDLE : Elevator.Status.RUNNING);
                continue;
            }
            Direction dir = e.getDirection();      // step() 하기 전에 저장
            e.step();
            int level = e.getCurrentLevel();
            Floor here = getFloor(level);
            
            // 대기 위치에 도착하면 문을 열지 않고 그냥 대기
            if (e.isParking() && e.getDestination().size() == 1 && e.getDestination().getFirst() == level) {
                e.getDestination().clear();
                e.setParking(false);
                e.setStatus(Elevator.Status.IDLE);
                continue;
            }
            
            boolean arrived = !e.getDestination().isEmpty()
                    && e.getDestination().getFirst() == level;
            
            if (policy.shouldStop(e, here, dir, arrived)) {
                // 정차: 목적지에서 제거 → 하차 → 탑승 → 호출 상태 갱신 → 문 열기
                e.getDestination().remove(Integer.valueOf(level));
                e.getRiders().removeIf(p -> {
                    if (p.getDestinationLevel() != level) return false;
                    p.setArriveTime(time);
                    stats.recordArrival(p);
                    return true;
                });
                board(e, here);
                refreshFloor(here);
                e.setDoorStatus(Elevator.DoorStatus.OPEN);
                e.setStatus(Elevator.Status.WAITING);
            } else {
                e.setDoorStatus(Elevator.DoorStatus.CLOSED);
                if (e.getDestination().isEmpty()) {
                    policy.onIdle(this, e);
                }
                e.setStatus(e.getDirection() == Direction.NONE
                        ? Elevator.Status.IDLE : Elevator.Status.RUNNING);
            }
        }
    }
    
    // 정차한 층에서 대기자를 태운다 (진행 방향이 같은 사람만, 정원까지)
    private void board(Elevator e, Floor here) {
        Direction go = e.getDirection();   // 하차 후 남은 목적지 기준 (NONE이면 자유)
        for (Person p : new ArrayList<>(here.getWaiting())) {
            if (e.isFull()) break;
            if (go == Direction.NONE) go = p.direction();
            if (p.direction() != go) continue;
            here.getWaiting().remove(p);
            p.setBoardTime(time);
            e.getRiders().add(p);
            e.addDestination(p.getDestinationLevel());
        }
    }
    
    // 대기자 기준으로 호출 버튼 갱신, 못 태운 사람이 있으면 재호출(대기열 등록)
    private void refreshFloor(Floor f) {
        f.setUp(f.getWaiting().stream().anyMatch(p -> p.direction() == Direction.UP));
        f.setDown(f.getWaiting().stream().anyMatch(p -> p.direction() == Direction.DOWN));
        f.setButton(f.getWaiting().isEmpty() ? Floor.ButtonStatus.IDLE : Floor.ButtonStatus.ACTIVE);
        if (f.isUp()) addPending(new Call(f.getLevel(), Direction.UP));
        if (f.isDown()) addPending(new Call(f.getLevel(), Direction.DOWN));
    }
    
    private void addPending(Call c) {
        if (!pending.contains(c)) pending.add(c);
    }
    
    // 호출 (1명, 목적지 랜덤)
    public Elevator call(int level, Direction direction) {
        return call(level, direction, 1, null);
    }
    
    // 호출 → 엘리베이터 배정. 없으면 null (호출은 대기열에 보존)
    // count: 호출한 인원, destination: 목적지 (null이면 진행 방향 안에서 랜덤)
    public Elevator call(int level, Direction direction, int count, Integer destination) {
        if (level < 1 || level > floors.size()) {
            throw new IllegalArgumentException("층은 1~" + floors.size() + " 사이여야 합니다.");
        }
        if (level == floors.size() && direction == Direction.UP) {
            throw new IllegalArgumentException("최상층(" + level + "층)에서는 위로 갈 수 없습니다.");
        }
        if (level == 1 && direction == Direction.DOWN) {
            throw new IllegalArgumentException("최하층(1층)에서는 아래로 갈 수 없습니다.");
        }
        if (count < 1) {
            throw new IllegalArgumentException("인원은 1명 이상이어야 합니다.");
        }
        if (destination != null) {
            if (destination < 1 || destination > floors.size()) {
                throw new IllegalArgumentException("목적지는 1~" + floors.size() + "층 사이여야 합니다.");
            }
            if (direction == Direction.UP ? destination <= level : destination >= level) {
                throw new IllegalArgumentException("목적지(" + destination + "층)가 호출 방향("
                        + (direction == Direction.UP ? "up" : "down") + ")과 맞지 않습니다.");
            }
        }
        
        Floor f = getFloor(level);
        for (int i = 0; i < count; i++) {
            int dest = destination != null ? destination : randomDestination(level, direction);
            f.getWaiting().add(new Person(level, dest, time));
        }
        stats.recordCall(count);
        f.setButton(Floor.ButtonStatus.ACTIVE);
        if (direction == Direction.UP) f.setUp(true);
        if (direction == Direction.DOWN) f.setDown(true);
        
        Elevator chosen = assign(level, direction);
        if (chosen == null) {
            addPending(new Call(level, direction));
        }
        return chosen;
    }
    
    private int randomDestination(int level, Direction direction) {
        return direction == Direction.UP
                ? ThreadLocalRandom.current().nextInt(level + 1, floors.size() + 1)
                : ThreadLocalRandom.current().nextInt(1, level);
    }
    
    // 가장 가까운 엘리베이터를 골라 목적지에 추가
    private Elevator assign(int level, Direction direction) {
        Elevator nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        
        for (Elevator e : elevators) {
            if (!policy.canServe(this, e, level, direction)) {
                continue;
            }
            int cost = policy.cost(this, e, level, direction);
            if (cost < nearestDistance) {
                nearest = e;
                nearestDistance = cost;
            }
        }
        if (nearest != null) {
            if (nearest.isParking()) {            // 대기 위치 이동은 취소
                nearest.getDestination().clear();
                nearest.setParking(false);
            }
            nearest.addDestination(level);
            // 호출 직후에도 상태가 바로 보이도록 갱신
            nearest.setStatus(nearest.getDirection() == Direction.NONE
                    ? Elevator.Status.IDLE : Elevator.Status.RUNNING);
        }
        return nearest;
    }
}
