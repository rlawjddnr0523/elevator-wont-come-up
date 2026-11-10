import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BuildingTest {
    
    private static Building building(int floors, int evs, int cap) {
        return new Building(floors, evs, cap);
    }
    
    private static void ticks(Building b, int n) {
        for (int i = 0; i < n; i++) b.tick();
    }
    
    // ── 호출 검증 ──
    
    @Test
    void 최상층_위쪽_호출과_최하층_아래쪽_호출은_거부된다() {
        Building b = building(10, 1, 8);
        assertThrows(IllegalArgumentException.class, () -> b.call(10, Direction.UP));
        assertThrows(IllegalArgumentException.class, () -> b.call(1, Direction.DOWN));
    }
    
    @Test
    void 잘못된_인수는_거부된다() {
        Building b = building(10, 1, 8);
        assertThrows(IllegalArgumentException.class, () -> b.call(0, Direction.UP));
        assertThrows(IllegalArgumentException.class, () -> b.call(11, Direction.DOWN));
        assertThrows(IllegalArgumentException.class, () -> b.call(3, Direction.UP, 0, null));
        assertThrows(IllegalArgumentException.class, () -> b.call(3, Direction.UP, 1, 2));   // 방향 불일치
        assertThrows(IllegalArgumentException.class, () -> b.call(3, Direction.DOWN, 1, 5)); // 방향 불일치
        assertThrows(IllegalArgumentException.class, () -> b.call(3, Direction.UP, 1, 11));  // 범위 밖
    }
    
    @Test
    void 거부된_호출은_상태를_바꾸지_않는다() {
        Building b = building(10, 1, 8);
        assertThrows(IllegalArgumentException.class, () -> b.call(3, Direction.UP, 1, 2));
        assertTrue(b.getFloor(3).getWaiting().isEmpty());
        assertEquals(Floor.ButtonStatus.IDLE, b.getFloor(3).getButton());
    }
    
    // ── 배정 ──
    
    @Test
    void 가장_가까운_엘리베이터가_배정된다() {
        Building b = building(10, 2, 8);
        b.getElevators().get(0).setCurrentLevel(1);
        b.getElevators().get(1).setCurrentLevel(8);
        assertSame(b.getElevators().get(1), b.call(9, Direction.DOWN));
    }
    
    @Test
    void 거리가_같으면_먼저_찾은_엘리베이터가_배정된다() {
        Building b = building(10, 2, 8);
        assertSame(b.getElevators().get(0), b.call(5, Direction.UP));
    }
    
    @Test
    void 반대_방향으로_움직이는_엘리베이터는_배정되지_않는다() {
        Building b = building(10, 2, 8);
        Elevator ev1 = b.getElevators().get(0);
        ev1.setCurrentLevel(8);
        ev1.addDestination(2);           // 아래로 이동 중
        b.getElevators().get(1).setCurrentLevel(1);
        // 6층 up 호출: 아래로 가는 EV1은 제외, EV2가 맡아야 한다
        assertSame(b.getElevators().get(1), b.call(6, Direction.UP));
    }
    
    @Test
    void 이미_지나친_층은_같은_방향이어도_배정되지_않는다() {
        Building b = building(10, 2, 8);
        Elevator ev1 = b.getElevators().get(0);
        ev1.setCurrentLevel(5);
        ev1.addDestination(9);           // 위로 이동 중, 3층은 이미 지나침
        b.getElevators().get(1).setCurrentLevel(10);
        assertSame(b.getElevators().get(1), b.call(3, Direction.UP));
    }
    
    @Test
    void 만원_엘리베이터는_배정되지_않고_가용_엘리베이터가_없으면_null이다() {
        Building b = building(10, 1, 2);
        Elevator ev = b.getElevators().get(0);
        ev.getRiders().add(new Person(1, 5, 0));
        ev.getRiders().add(new Person(1, 6, 0));
        assertTrue(ev.isFull());
        assertNull(b.call(3, Direction.UP));
        assertEquals(1, b.getFloor(3).getWaiting().size());   // 호출은 보존된다
    }
    
    @Test
    void 배정_못_받은_호출은_엘리베이터가_비면_이후_틱에서_처리된다() {
        Building b = building(10, 1, 1);
        Elevator ev = b.getElevators().get(0);
        ev.getRiders().add(new Person(1, 2, 0));   // 만원, 2층에서 내림
        ev.addDestination(2);
        assertNull(b.call(3, Direction.UP));
        ticks(b, 12);
        assertTrue(b.getFloor(3).getWaiting().isEmpty());
    }
    
    // ── 정차 / 탑승 / 하차 ──
    
    @Test
    void 호출한_사람을_태워_목적지에서_내려준다() {
        Building b = building(10, 1, 8);
        b.call(3, Direction.UP, 3, 8);
        ticks(b, 2);                       // 1→3층 도착
        Elevator ev = b.getElevators().get(0);
        assertEquals(3, ev.getCurrentLevel());
        assertEquals(3, ev.getPassengerCount());
        assertTrue(b.getFloor(3).getWaiting().isEmpty());
        assertEquals(Floor.ButtonStatus.IDLE, b.getFloor(3).getButton());
        assertEquals(Elevator.DoorStatus.OPEN, ev.getDoorStatus());
        
        ticks(b, 6);                       // 문 닫기 1틱 + 3→8층 5틱
        assertEquals(8, ev.getCurrentLevel());
        assertEquals(0, ev.getPassengerCount());
        assertEquals(3, b.getStats().getArrived());
    }
    
    @Test
    void 이동_중_같은_방향_호출_층에서_정차한다() {
        Building b = building(10, 1, 8);
        Elevator ev1 = b.getElevators().get(0);
        b.call(9, Direction.UP, 1, 10);                // EV1: 1→9
        ticks(b, 2);                                   // 3층까지 이동
        b.call(5, Direction.UP, 1, 7);                 // 이동 중 같은 방향 호출
        ticks(b, 2);                                   // 5층 도착
        assertEquals(5, ev1.getCurrentLevel());
        assertEquals(Elevator.DoorStatus.OPEN, ev1.getDoorStatus());
        assertEquals(1, ev1.getPassengerCount());
    }
    
    @Test
    void 만원이면_외부_호출_층을_무정차로_통과한다() {
        Building b = building(10, 2, 1);
        b.getElevators().get(1).setCurrentLevel(10);
        Elevator ev1 = b.getElevators().get(0);
        b.call(2, Direction.UP, 1, 9);                 // EV1이 1명 태우고 만원
        ticks(b, 1);
        assertTrue(ev1.isFull());
        b.getFloor(5).getWaiting().add(new Person(5, 8, 0));   // 5층 외부 호출 (up)
        b.getFloor(5).setUp(true);
        ticks(b, 4);                                   // 문 닫기 1틱 + 2→5층 3틱 (5층 통과)
        assertEquals(5, ev1.getCurrentLevel());
        assertNotEquals(Elevator.DoorStatus.OPEN, ev1.getDoorStatus());
        assertEquals(1, b.getFloor(5).getWaiting().size());   // 태우지 않았다
    }
    
    @Test
    void 만원이어도_목적지에서는_정차해서_내려준다() {
        Building b = building(10, 1, 1);
        b.call(2, Direction.UP, 1, 4);
        ticks(b, 1);
        Elevator ev = b.getElevators().get(0);
        assertTrue(ev.isFull());
        ticks(b, 3);                       // 문 닫기 1틱 + 2→4층 2틱
        assertEquals(4, ev.getCurrentLevel());
        assertEquals(0, ev.getPassengerCount());
        assertEquals(Elevator.DoorStatus.OPEN, ev.getDoorStatus());
    }
    
    @Test
    void 정원을_넘는_인원은_남겨두고_호출이_유지된다() {
        Building b = building(10, 1, 2);
        b.call(2, Direction.UP, 5, 8);
        ticks(b, 1);
        Elevator ev = b.getElevators().get(0);
        assertEquals(2, ev.getPassengerCount());
        assertEquals(3, b.getFloor(2).getWaiting().size());
        assertTrue(b.getFloor(2).isUp());
        assertEquals(Floor.ButtonStatus.ACTIVE, b.getFloor(2).getButton());
    }
    
    @Test
    void 반대_방향_대기자는_같이_타지_않는다() {
        Building b = building(10, 1, 8);
        b.getElevators().get(0).setCurrentLevel(3);
        b.call(5, Direction.UP, 1, 9);
        b.getFloor(5).getWaiting().add(new Person(5, 1, 0));   // 5층에서 down 대기자
        b.getFloor(5).setDown(true);
        ticks(b, 2);                                           // 5층 도착
        Elevator ev = b.getElevators().get(0);
        assertEquals(1, ev.getPassengerCount());
        assertEquals(9, ev.getRiders().get(0).getDestinationLevel());
        assertEquals(1, b.getFloor(5).getWaiting().size());
        assertFalse(b.getFloor(5).isUp());
        assertTrue(b.getFloor(5).isDown());
    }
    
    // ── 모드 ──
    
    @Test
    void ECO는_절반의_엘리베이터만_호출을_받는다() {
        Building b = building(10, 4, 8);
        b.setMode(Mode.ECO);
        b.getElevators().get(3).setCurrentLevel(5);   // 가장 가깝지만 비가동 대상
        Elevator chosen = b.call(5, Direction.UP);
        assertTrue(b.getElevators().indexOf(chosen) < 2);
    }
    
    @Test
    void FAST는_한가한_엘리베이터를_층_전체에_분산시킨다() {
        Building b = building(10, 2, 8);
        b.setMode(Mode.FAST);
        ticks(b, 10);
        assertEquals(3, b.getElevators().get(0).getCurrentLevel());
        assertEquals(8, b.getElevators().get(1).getCurrentLevel());
    }
    
    @Test
    void FAST_대기_위치로_이동_중에도_호출을_받으면_대기_이동을_취소한다() {
        Building b = building(10, 1, 8);
        b.setMode(Mode.FAST);
        ticks(b, 2);                                    // 홈(6층)으로 이동 중
        Elevator ev = b.getElevators().get(0);
        assertTrue(ev.isParking());
        assertSame(ev, b.call(2, Direction.UP, 1, 9));
        assertFalse(ev.isParking());
        assertEquals(java.util.List.of(2), ev.getDestination());
    }
    
    @Test
    void FAST는_정차_예정이_많은_엘리베이터보다_ETA가_짧은_엘리베이터를_고른다() {
        Building b = building(10, 2, 8);
        b.setMode(Mode.FAST);
        Elevator ev1 = b.getElevators().get(0);
        Elevator ev2 = b.getElevators().get(1);
        ev1.setCurrentLevel(3);
        for (int l = 4; l <= 7; l++) ev1.addDestination(l);   // 4개 층에 정차 예정
        ev2.setCurrentLevel(1);
        // 거리만 보면 EV1(3→8: 5층, EV2: 7층)이 가깝지만 ETA는 EV1 13틱, EV2 7틱
        assertSame(ev2, b.call(8, Direction.UP));
    }
    
    @Test
    void 수동으로_모드를_고르면_자동_전환이_꺼진다() {
        Building b = building(10, 1, 8);
        b.setAutoMode(true);
        b.setMode(Mode.ECO);
        assertFalse(b.isAutoMode());
    }
    
    @Test
    void 자동_모드는_대기가_몰리면_FAST로_바뀐다() {
        Building b = building(10, 1, 8);
        b.setAutoMode(true);
        b.call(5, Direction.UP, 7, 9);   // 대기 7명
        b.tick();
        assertEquals(Mode.FAST, b.getMode());
    }
    
    @Test
    void 자동_모드는_한동안_한가하면_ECO로_바뀐다() {
        Building b = building(10, 2, 8);
        b.setAutoMode(true);
        ticks(b, 10);
        assertEquals(Mode.ECO, b.getMode());
    }
    
    // ── 경로 정렬 ──
    
    @Test
    void 목적지는_가는_방향으로_먼저_훑고_반대편은_돌아오며_처리한다() {
        Building b = building(10, 1, 8);
        Elevator ev = b.getElevators().get(0);
        ev.setCurrentLevel(3);
        ev.addDestination(8);
        ev.addDestination(5);
        assertEquals(java.util.List.of(5, 8), ev.getDestination());
        ev.addDestination(2);
        ev.addDestination(1);
        assertEquals(java.util.List.of(5, 8, 2, 1), ev.getDestination());
    }
    
    // ── 문 ──
    
    @Test
    void 정차하면_문이_1틱_열리고_다음_틱에_닫힌_뒤_이동한다() {
        Building b = building(10, 1, 8);
        Elevator ev = b.getElevators().get(0);
        b.call(2, Direction.UP, 1, 5);
        b.tick();                                          // 2층 도착, 문 열림
        assertEquals(Elevator.DoorStatus.OPEN, ev.getDoorStatus());
        b.tick();                                          // 문 닫힘 (이동 없음)
        assertEquals(Elevator.DoorStatus.CLOSED, ev.getDoorStatus());
        assertEquals(2, ev.getCurrentLevel());
        b.tick();                                          // 이동 재개
        assertEquals(3, ev.getCurrentLevel());
    }
    
    // ── 통계 ──
    
    @Test
    void 대기_시간과_탑승_시간이_통계에_기록된다() {
        Building b = building(10, 1, 8);
        b.call(3, Direction.UP, 1, 5);   // time 0에 호출
        ticks(b, 5);                     // 2틱 탑승, 3틱 문 닫기, 5틱 도착
        Stats st = b.getStats();
        assertEquals(1, st.getCalled());
        assertEquals(1, st.getArrived());
        assertEquals(2.0, st.avgWait(), 0.001);
        assertEquals(3.0, st.avgRide(), 0.001);
    }
}
