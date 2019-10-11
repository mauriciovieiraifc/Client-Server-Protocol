package settings;

public class Function {
    
    private final double x, y;
    
    public Function(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double sum() {
        return x + y;
    }
 
    public double sub() {
        return x - y;
    }
 
    public double mul() {
        return x * y;
    }
 
    public double div() {
        return x / y;
    }
}
