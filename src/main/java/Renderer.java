// 콘솔 시각화: 위층이 위, 엘리베이터는 열(column)로 표시
public class Renderer {
    
    public static void render(Building building) {
        int floorCount = building.getFloors().size();
        
        // 헤더
        StringBuilder header = new StringBuilder("           ");
        for (int i = 1; i <= building.getElevators().size(); i++) {
            header.append(String.format(" EV%-3d", i));
        }
        System.out.println(header);
        
        // 층별 (맨 위층부터)
        for (int level = floorCount; level >= 1; level--) {
            Floor floor = building.getFloor(level);
            String button = floor.getButton() == Floor.ButtonStatus.ACTIVE ? "●" : " ";
            
            StringBuilder row = new StringBuilder(String.format("%2dF %s %-4s|", level, button,
                    floor.getWaiting().isEmpty() ? "" : "(" + floor.getWaiting().size() + ")"));
            for (Elevator e : building.getElevators()) {
                row.append(e.getCurrentLevel() == level ? " " + symbol(e) + "   " : "     ").append("|");
            }
            System.out.println(row);
        }
        
        // 엘리베이터 상태
        System.out.println();
        int n = 1;
        for (Elevator e : building.getElevators()) {
            System.out.printf("EV%d\t층: %d\t방향: %s\t\t상태: %s\t문: %s\t인원: %d/%d명\t목적지: %s%n",
                    n++, e.getCurrentLevel(), e.getDirection(), e.getStatus(), e.getDoorStatus(),
                    e.getPassengerCount(), e.getMaxCapacity(), e.getDestination());
        }
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
