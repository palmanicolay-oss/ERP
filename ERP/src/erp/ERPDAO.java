package erp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class ERPDAO {

    // 1. Listar todo el inventario (para la vista de Productos y el Combo de Ventas)
    public ArrayList<ERP> listarInventario() {
        ArrayList<ERP> listaAdornos = new ArrayList<>();
        String sql = "SELECT * FROM productos_madera";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ERP producto = new ERP(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getString("tipo_madera"),
                    rs.getDouble("precio"),
                    rs.getInt("stock")
                );
                listaAdornos.add(producto);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar inventario: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
        return listaAdornos;
    }

    // 2. Registrar un nuevo adorno de madera
    public boolean agregarAdorno(ERP adorno) {
        String sql = "INSERT INTO productos_madera (nombre, tipo_madera, precio, stock) VALUES (?, ?, ?, ?)";
        
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, adorno.getNombre());
            ps.setString(2, adorno.getTipoMadera());
            ps.setDouble(3, adorno.getPrecio());
            ps.setInt(4, adorno.getStock());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar el producto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // 3. REGISTRAR VENTA REAL EN LA BASE DE DATOS (Con Transacción y Descuento de Stock)
    public boolean registrarVentaReal(String rut, String nombreCliente, int idProducto, int cantidad, double totalConIva) {
        Connection con = null;
        try {
            con = Conexion.conectar();
            con.setAutoCommit(false); // Iniciar transacción segura

            // Paso A: Verificar si el cliente ya existe o registrarlo
            int idCliente = -1;
            String sqlClienteSel = "SELECT id_cliente FROM clientes WHERE rut = ?";
            try (PreparedStatement ps1 = con.prepareStatement(sqlClienteSel)) {
                ps1.setString(1, rut);
                try (ResultSet rs = ps1.executeQuery()) {
                    if (rs.next()) {
                        idCliente = rs.getInt("id_cliente");
                    } else {
                        String sqlClienteIns = "INSERT INTO clientes (rut, nombre_cliente) VALUES (?, ?)";
                        try (PreparedStatement psIns = con.prepareStatement(sqlClienteIns, PreparedStatement.RETURN_GENERATED_KEYS)) {
                            psIns.setString(1, rut);
                            psIns.setString(2, nombreCliente);
                            psIns.executeUpdate();
                            try (ResultSet gk = psIns.getGeneratedKeys()) {
                                if (gk.next()) idCliente = gk.getInt(1);
                            }
                        }
                    }
                }
            }

            // Paso B: Insertar la Venta principal
            int idVenta = -1;
            String sqlVenta = "INSERT INTO ventas (id_cliente, total_con_iva) VALUES (?, ?)";
            try (PreparedStatement psVenta = con.prepareStatement(sqlVenta, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psVenta.setInt(1, idCliente);
                psVenta.setDouble(2, totalConIva);
                psVenta.executeUpdate();
                try (ResultSet gk = psVenta.getGeneratedKeys()) {
                    if (gk.next()) idVenta = gk.getInt(1);
                }
            }

            // Paso C: Insertar el Detalle de la Venta
            String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, subtotal) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psDet = con.prepareStatement(sqlDetalle)) {
                psDet.setInt(1, idVenta);
                psDet.setInt(2, idProducto);
                psDet.setInt(3, cantidad);
                psDet.setDouble(4, totalConIva);
                psDet.executeUpdate();
            }

            // Paso D: Descontar el stock del producto de madera
            String sqlStock = "UPDATE productos_madera SET stock = stock - ? WHERE id_producto = ?";
            try (PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                psStock.setInt(1, cantidad);
                psStock.setInt(2, idProducto);
                psStock.executeUpdate();
            }

            con.commit(); // Confirmar cambios en la BD
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            JOptionPane.showMessageDialog(null, "Error al procesar la venta en la BD: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // 4. Listar Historial de Ventas real usando JOIN (para la tabla de la vista de Ventas)
    public ArrayList<Object[]> listarHistorialVentas() {
        ArrayList<Object[]> listaVentas = new ArrayList<>();
        String sql = "SELECT v.id_venta, v.fecha, c.rut, c.nombre_cliente, p.nombre, d.cantidad, v.total_con_iva " +
                     "FROM ventas v " +
                     "JOIN clientes c ON v.id_cliente = c.id_cliente " +
                     "JOIN detalle_venta d ON v.id_venta = d.id_venta " +
                     "JOIN productos_madera p ON d.id_producto = p.id_producto";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_venta"),
                    rs.getString("fecha"),
                    rs.getString("rut"),
                    rs.getString("nombre_cliente"),
                    rs.getString("nombre"),
                    rs.getInt("cantidad"),
                    "$" + rs.getDouble("total_con_iva")
                };
                listaVentas.add(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar historial: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
        return listaVentas;
    }

    // 5. Consulta SQL Completa con GROUP BY y SUM (para el Reporte Financiero)
    public ArrayList<Object[]> obtenerResumenTablaInventario() {
        ArrayList<Object[]> listaResumen = new ArrayList<>();
        String sql = "SELECT tipo_madera, COUNT(*) as total_items, SUM(stock * precio) as valor_total FROM productos_madera GROUP BY tipo_madera";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getString("tipo_madera"),
                    rs.getInt("total_items"),
                    rs.getDouble("valor_total")
                };
                listaResumen.add(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error en consulta compleja: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
        return listaResumen;
    }
}