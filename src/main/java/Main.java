import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException {
        
        int floorCount = 10;
        int evAmount = 2;
        int evCapacity = 8; // 최대 정원 (명)
        
        Building building = new Building(floorCount, evAmount, evCapacity);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        Renderer.render(building);
        
        while (true) {
            System.out.print("\n호출(예: 3 up 3 8 = 3층/위/3명/8층행) / mode <standard|rush_hour|eco> / 엔터=틱 / exit: ");
            String line = br.readLine();
            if (line == null) break;
            line = line.trim().toLowerCase();
            
            if (line.equals("exit")) {
                break;
            }
            
            if (line.startsWith("mode")) {
                String[] m = line.split("\\s+");
                try {
                    building.setMode(Mode.valueOf(m[1].toUpperCase()));
                    System.out.println("모드: " + building.getMode());
                } catch (RuntimeException ex) {
                    System.out.println("형식: mode <standard|rush_hour|eco>");
                }
                continue;
            }
            
            if (!line.isEmpty()) {
                String[] parts = line.split("\\s+");
                if (parts.length < 2 || parts.length > 4) {
                    System.out.println("형식: <층> <up|down> [인원] [목적지]  예) 3 up 3 8");
                    continue;
                }
                
                int level;
                int count = 1;
                Integer destination = null;
                try {
                    level = Integer.parseInt(parts[0]);
                    if (parts.length >= 3) count = Integer.parseInt(parts[2]);
                    if (parts.length == 4) destination = Integer.parseInt(parts[3]);
                } catch (NumberFormatException ex) {
                    System.out.println("알 수 없는 명령입니다. 형식: <층> <up|down> [인원] [목적지] / mode <...> / exit");
                    continue;
                }
                if (!parts[1].equals("up") && !parts[1].equals("down")) {
                    System.out.println("방향은 up 또는 down 입니다.");
                    continue;
                }
                Direction direction = parts[1].equals("up") ? Direction.UP : Direction.DOWN;
                
                Elevator chosen;
                try {
                    chosen = building.call(level, direction, count, destination);
                } catch (IllegalArgumentException ex) {
                    System.out.println(ex.getMessage());
                    continue;
                }
                if (chosen == null) {
                    System.out.println("가용 엘리베이터가 없어 대기열에 등록했습니다.");
                }
            } else {
                building.tick();
            }
            
            System.out.println();
            Renderer.render(building);
        }
    }
}
