package erp;

public class ERP {
    private int idProducto;
    private String nombre;
    private String tipoMadera;
    private double precio;
    private int stock;

    public ERP() {}

    public ERP(int idProducto, String nombre, String tipoMadera, double precio, int stock) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.tipoMadera = tipoMadera;
        this.precio = precio;
        this.stock = stock;
    }

    // Getters y Setters
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipoMadera() { return tipoMadera; }
    public void setTipoMadera(String tipoMadera) { this.tipoMadera = tipoMadera; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}