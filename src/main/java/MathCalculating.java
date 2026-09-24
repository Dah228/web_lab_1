
import java.util.HashMap;

public class MathCalculating {

    private final double x;
    private final double y;
    private final double r;


    public MathCalculating(HashMap <String, Double> mapWithData){
        this.x = mapWithData.getOrDefault("coord_x", (double) 0);
        this.y = mapWithData.getOrDefault("coord_y", (double) 0);
        this.r = mapWithData.getOrDefault("radius_r", (double) 0);
    }


    private boolean dataIsCorrect(){
        return y >= -5 && y <= 3;
    }




    // Четверть круга (первый квадрант: X >= 0, Y >= 0)
    private boolean isInsideCircle() {
        // x^2 + y^2 <= (r/2)^2  <=>  x^2 + y^2 <= r^2 / 4
        return x >= 0 && y >= 0 && (x * x + y * y) <= (r * r / 4.0);
    }

    // Треугольник (третий квадрант: X <= 0, Y <= 0)
    private boolean isInsideTriangle() {
        // Уравнение гипотенузы: x + y = -r  =>  -y - x <= r
        return x <= 0 && y <= 0 && (-y - x <= r);
    }

    // Прямоугольник (четвертый квадрант: X > 0, Y <= 0)
    private boolean isInsideRectangle() {
        // Ширина от 0 до R, высота от 0 до -R/2
        return x > 0 && y <= 0 && Math.abs(y) <= r / 2.0 && x <= r;
    }

    // Общая проверка попадания в любую из зон
    public boolean isInsideZone() {
        return dataIsCorrect() && (isInsideCircle() ||
                isInsideTriangle() ||
                isInsideRectangle());
    }

    public double getX() {
        return x;
    }

    public double getR() {
        return r;
    }

    public double getY() {
        return y;
    }
}
