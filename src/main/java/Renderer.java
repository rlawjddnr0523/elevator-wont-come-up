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
            String button = floor.getButton() == Floor.buttonStatus.ACTIVE ? "●" : " ";
            
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
}
