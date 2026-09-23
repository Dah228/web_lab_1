

public class PointResult {
    public double x;
    public double y;
    public double r;
    public boolean isHit;
    public String timestamp;      // Время сервера
    public double executionTime;  // Время работы скрипта

    // Конструктор для быстрого создания записи
    public PointResult(double x, double y, double r, boolean isHit, String timestamp, double executionTime) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.isHit = isHit;
        this.timestamp = timestamp;
        this.executionTime = executionTime;
    }
}