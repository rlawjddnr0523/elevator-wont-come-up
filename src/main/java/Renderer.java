// 콘솔 시각화: 위층이 위, 엘리베이터는 열(column)로 표시
//   [▲ 3]  문 닫힘 / ]▲ 3[  문 열림  (▲▼■ = 진행 방향, 숫자 = 탑승 인원)
//   층 왼쪽: ▲▼ = 해당 방향 호출 중, (n) = 대기 인원
public class Renderer {
    
    private static final int PREFIX = 11;   // 층 라벨 영역 폭
    private static final int CELL = 7;      // 엘리베이터 한 칸 폭
    
    public static void render(Building building) {
        int floorCount = building.getFloors().size();
        int evCount = building.getElevators().size();
        
        // 헤더
        StringBuilder header = new StringBuilder(" ".repeat(PREFIX + 1));
        for (int i = 1; i <= evCount; i++) {
            header.append(center("EV" + i, CELL)).append(" ");
        }
        System.out.println(header);
        System.out.println(border('┌', '┬', '┐', evCount));
        
        // 층별 (맨 위층부터)
        for (int level = floorCount; level >= 1; level--) {
            Floor floor = building.getFloor(level);
            String up = floor.isUp() ? "▲" : "·";
            String down = floor.isDown() ? "▼" : "·";
            String waiting = floor.getWaiting().isEmpty() ? "" : "(" + floor.getWaiting().size() + ")";
            
            StringBuilder row = new StringBuilder(
                    String.format("%2dF %s%s %-4s", level, up, down, waiting));
            row.append("│");
            for (Elevator e : building.getElevators()) {
                row.append(e.getCurrentLevel() == level ? cell(e) : " ".repeat(CELL)).append("│");
            }
            System.out.println(row);
        }
        System.out.println(border('└', '┴', '┘', evCount));
        
        // 엘리베이터 상태
        System.out.println();
        int n = 1;
        for (Elevator e : building.getElevators()) {
            System.out.printf("EV%d  %2dF  %-4s %-8s 문 %-6s %d/%d명  목적지 %s%n",
                    n++, e.getCurrentLevel(), e.getDirection(), e.getStatus(),
                    e.getDoorStatus() == Elevator.DoorStatus.OPEN ? "OPEN" : "CLOSED",
                    e.getPassengerCount(), e.getMaxCapacity(), e.getDestination());
        }
    }
    
    // 엘리베이터 한 칸: 문 닫힘 "[▲ 3]", 열림 "]▲ 3["
    private static String cell(Elevator e) {
        boolean open = e.getDoorStatus() == Elevator.DoorStatus.OPEN;
        String body = String.format("%s %d", symbol(e), e.getPassengerCount());
        return " " + (open ? "]" : "[") + body + (open ? "[" : "]") + " ";
    }
    
    private static String border(char left, char mid, char right, int evCount) {
        StringBuilder sb = new StringBuilder(" ".repeat(PREFIX)).append(left);
        for (int i = 0; i < evCount; i++) {
            sb.append("─".repeat(CELL)).append(i == evCount - 1 ? right : mid);
        }
        return sb.toString();
    }
    
    private static String center(String s, int width) {
        int left = (width - s.length()) / 2;
        return " ".repeat(left) + s + " ".repeat(width - s.length() - left);
    }
    
    private static String symbol(Elevator e) {
        return switch (e.getDirection()) {
            case UP -> "▲";
            case DOWN -> "▼";
            case NONE -> "■";
        };
    }
    
    public static void renderStats(Building b) {
        Stats st = b.getStats();
        int waiting = b.getFloors().stream().mapToInt(f -> f.getWaiting().size()).sum();
        int riding = b.getElevators().stream().mapToInt(Elevator::getPassengerCount).sum();
        int moved = b.getElevators().stream().mapToInt(Elevator::getMovedFloors).sum();
        System.out.println("── 통계 (" + b.getMode() + ", " + b.getTime() + "틱) ──");
        System.out.printf("호출 %d명 | 도착 %d명 | 탑승 중 %d명 | 대기 중 %d명%n",
                st.getCalled(), st.getArrived(), riding, waiting);
        System.out.printf("평균 대기 %.1f틱 | 최대 대기 %d틱 | 평균 탑승 %.1f틱 | 총 이동 %d층%n",
                st.avgWait(), st.getMaxWait(), st.avgRide(), moved);
    }
}
