package erp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class VentanaPrincipal extends javax.swing.JPanel {

    private JPanel panelContenidoCentral;
    private CardLayout cardLayout;
    
    private ERPDAO dao;
    
    private JTable tablaProductos;
    private DefaultTableModel modeloTablaProductos;
    
    private JTable tablaReporte;
    private DefaultTableModel modeloTablaReporte;
    
    private JTable tablaHistorialVentas;
    private DefaultTableModel modeloTablaHistorialVentas;
    
    // Campos del formulario de productos
    private JTextField txtNombre, txtMadera, txtPrecio, txtStock;
    
    // Componentes del módulo de ventas
    private JTextField txtRutVenta, txtNombreVenta, txtCantVenta;
    private JComboBox<String> comboProductosVenta;
    private ArrayList<ERP> listaProductosMemoria;

    public VentanaPrincipal() {
        initComponents();
        dao = new ERPDAO();
        inicializarInterfazDashboard();
    }

    private void inicializarInterfazDashboard() {
        setLayout(new BorderLayout());

        // 1. PANEL LATERAL IZQUIERDO (MENÚ)
        JPanel panelMenu = new JPanel();
        panelMenu.setPreferredSize(new Dimension(200, getHeight()));
        panelMenu.setBackground(new Color(44, 62, 80));
        panelMenu.setLayout(new GridLayout(8, 1, 5, 5));

        JLabel lblTituloMenu = new JLabel("  Susurros Madera", JLabel.CENTER);
        lblTituloMenu.setForeground(Color.WHITE);
        lblTituloMenu.setFont(new Font("Arial", Font.BOLD, 14));
        panelMenu.add(lblTituloMenu);

        JButton btnInicio = crearBotonMenu("Inicio");
        JButton btnProductos = crearBotonMenu("Inventario");
        JButton btnVentas = crearBotonMenu("Registro de Ventas");
        JButton btnReporte = crearBotonMenu("Reporte Financiero");

        panelMenu.add(btnInicio);
        panelMenu.add(btnProductos);
        panelMenu.add(btnVentas);
        panelMenu.add(btnReporte);

        add(panelMenu, BorderLayout.WEST);

        // 2. PANEL CENTRAL CON CARDLAYOUT
        cardLayout = new CardLayout();
        panelContenidoCentral = new JPanel(cardLayout);

        panelContenidoCentral.add(crearPanelInicio(), "INICIO");
        panelContenidoCentral.add(crearPanelProductos(), "PRODUCTOS");
        panelContenidoCentral.add(crearPanelVentas(), "VENTAS");
        panelContenidoCentral.add(crearPanelReporteFinanciero(), "REPORTE");

        add(panelContenidoCentral, BorderLayout.CENTER);

        // Eventos del menú
        btnInicio.addActionListener(e -> cardLayout.show(panelContenidoCentral, "INICIO"));
        
        btnProductos.addActionListener(e -> {
            cardLayout.show(panelContenidoCentral, "PRODUCTOS");
            cargarDatosTablaProductos();
        });
        
        btnVentas.addActionListener(e -> {
            cardLayout.show(panelContenidoCentral, "VENTAS");
            cargarComboProductos();
            cargarTablaHistorialVentas();
        });
        
        btnReporte.addActionListener(e -> {
            cardLayout.show(panelContenidoCentral, "REPORTE");
            actualizarReporteFinanciero();
        });
    }

    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(new Color(52, 73, 94));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        return boton;
    }

    private JPanel crearPanelInicio() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(236, 240, 241));
        JLabel lblBienvenida = new JLabel("<html><div style='text-align: center;'><h2>Bienvenido al Sistema ERP</h2><p><b>Susurros de la Madera</b> - Control de Inventario y Ventas</p></div></html>");
        lblBienvenida.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(lblBienvenida);
        return panel;
    }

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelForm = new JPanel(new GridLayout(5, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Gestión de Adornos de Madera"));

        panelForm.add(new JLabel("Nombre del Adorno:"));
        txtNombre = new JTextField();
        panelForm.add(txtNombre);

        panelForm.add(new JLabel("Tipo de Madera (ej: Roble):"));
        txtMadera = new JTextField();
        panelForm.add(txtMadera);

        panelForm.add(new JLabel("Precio ($):"));
        txtPrecio = new JTextField();
        panelForm.add(txtPrecio);

        panelForm.add(new JLabel("Stock Inicial:"));
        txtStock = new JTextField();
        panelForm.add(txtStock);

        JButton btnGuardar = new JButton("Guardar Producto");
        btnGuardar.setBackground(new Color(39, 174, 96));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> registrarProducto());
        panelForm.add(btnGuardar);

        panel.add(panelForm, BorderLayout.NORTH);

        modeloTablaProductos = new DefaultTableModel();
        modeloTablaProductos.addColumn("ID");
        modeloTablaProductos.addColumn("Nombre");
        modeloTablaProductos.addColumn("Madera");
        modeloTablaProductos.addColumn("Precio ($)");
        modeloTablaProductos.addColumn("Stock");

        tablaProductos = new JTable(modeloTablaProductos);
        panel.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Actualizar Tabla");
        btnRefrescar.addActionListener(e -> cargarDatosTablaProductos());
        JPanel panelSur = new JPanel();
        panelSur.add(btnRefrescar);
        panel.add(panelSur, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // MÓDULO DE VENTAS (CONECTADO A LA BASE DE DATOS)
    // ==========================================
    private JPanel crearPanelVentas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelVentaForm = new JPanel(new GridLayout(6, 2, 5, 5));
        panelVentaForm.setBorder(BorderFactory.createTitledBorder("Módulo de Facturación y Caja (Guardado Real)"));

        panelVentaForm.add(new JLabel("RUT del Cliente:"));
        txtRutVenta = new JTextField();
        panelVentaForm.add(txtRutVenta);

        panelVentaForm.add(new JLabel("Nombre del Cliente:"));
        txtNombreVenta = new JTextField();
        panelVentaForm.add(txtNombreVenta);

        panelVentaForm.add(new JLabel("Seleccionar Adorno:"));
        comboProductosVenta = new JComboBox<>();
        panelVentaForm.add(comboProductosVenta);

        panelVentaForm.add(new JLabel("Cantidad a Vender:"));
        txtCantVenta = new JTextField("1");
        panelVentaForm.add(txtCantVenta);

        JButton btnProcesarVenta = new JButton("Procesar Venta y Guardar en BD");
        btnProcesarVenta.setBackground(new Color(41, 128, 185));
        btnProcesarVenta.setForeground(Color.WHITE);
        btnProcesarVenta.addActionListener(e -> procesarYGuardarVenta());
        panelVentaForm.add(btnProcesarVenta);

        panel.add(panelVentaForm, BorderLayout.NORTH);

        // Tabla de Historial Real de Ventas
        modeloTablaHistorialVentas = new DefaultTableModel();
        modeloTablaHistorialVentas.addColumn("N° Venta");
        modeloTablaHistorialVentas.addColumn("Fecha");
        modeloTablaHistorialVentas.addColumn("RUT Cliente");
        modeloTablaHistorialVentas.addColumn("Nombre Cliente");
        modeloTablaHistorialVentas.addColumn("Producto");
        modeloTablaHistorialVentas.addColumn("Cantidad");
        modeloTablaHistorialVentas.addColumn("Total con IVA ($)");

        tablaHistorialVentas = new JTable(modeloTablaHistorialVentas);
        panel.add(new JScrollPane(tablaHistorialVentas), BorderLayout.CENTER);

        return panel;
    }

    private void cargarComboProductos() {
        comboProductosVenta.removeAllItems();
        listaProductosMemoria = dao.listarInventario();
        for (ERP p : listaProductosMemoria) {
            comboProductosVenta.addItem(p.getNombre() + " (Stock: " + p.getStock() + " | $" + p.getPrecio() + ")");
        }
    }

    private void procesarYGuardarVenta() {
        try {
            if (txtRutVenta.getText().trim().isEmpty() || txtNombreVenta.getText().trim().isEmpty() || txtCantVenta.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos del cliente y la cantidad.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int indexSeleccionado = comboProductosVenta.getSelectedIndex();
            if (indexSeleccionado < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ERP productoElegido = listaProductosMemoria.get(indexSeleccionado);
            int cantidad = Integer.parseInt(txtCantVenta.getText().trim());

            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (cantidad > productoElegido.getStock()) {
                JOptionPane.showMessageDialog(this, "Stock insuficiente. Stock actual: " + productoElegido.getStock(), "Error de Stock", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Cálculo con IVA (19%)
            double subtotalNeto = productoElegido.getPrecio() * cantidad;
            double totalConIva = subtotalNeto * 1.19;

            // Guardar en la Base de Datos real mediante el DAO
            boolean exito = dao.registrarVentaReal(
                txtRutVenta.getText().trim(),
                txtNombreVenta.getText().trim(),
                productoElegido.getIdProducto(),
                cantidad,
                totalConIva
            );

            if (exito) {
                JOptionPane.showMessageDialog(this, "¡Venta procesada y guardada en MySQL con éxito!\nTotal con IVA (19%): $" + totalConIva, "Caja ERP", JOptionPane.INFORMATION_MESSAGE);
                txtRutVenta.setText("");
                txtNombreVenta.setText("");
                txtCantVenta.setText("1");
                cargarComboProductos();
                cargarTablaHistorialVentas();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: La cantidad debe ser un valor numérico.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTablaHistorialVentas() {
        modeloTablaHistorialVentas.setRowCount(0);
        ArrayList<Object[]> ventas = dao.listarHistorialVentas();
        for (Object[] fila : ventas) {
            modeloTablaHistorialVentas.addRow(fila);
        }
    }

    private JPanel crearPanelReporteFinanciero() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("Reporte Financiero - Valorización de Inventario (Consulta SQL con GROUP BY)");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblTitulo, BorderLayout.NORTH);

        modeloTablaReporte = new DefaultTableModel();
        modeloTablaReporte.addColumn("Tipo de Madera");
        modeloTablaReporte.addColumn("Cantidad de Modelos");
        modeloTablaReporte.addColumn("Valor Total Inventario ($)");

        tablaReporte = new JTable(modeloTablaReporte);
        panel.add(new JScrollPane(tablaReporte), BorderLayout.CENTER);

        JButton btnActualizar = new JButton("Actualizar Datos del Reporte");
        btnActualizar.setBackground(new Color(41, 128, 185));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.addActionListener(e -> actualizarReporteFinanciero());

        JPanel panelSur = new JPanel();
        panelSur.add(btnActualizar);
        panel.add(panelSur, BorderLayout.SOUTH);

        return panel;
    }

    private void actualizarReporteFinanciero() {
        modeloTablaReporte.setRowCount(0);
        ArrayList<Object[]> listaResumen = dao.obtenerResumenTablaInventario();
        for (Object[] fila : listaResumen) {
            modeloTablaReporte.addRow(fila);
        }
    }

    private void registrarProducto() {
        try {
            if (txtNombre.getText().trim().isEmpty() || txtPrecio.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete los campos obligatorios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());

            ERP nuevo = new ERP(0, txtNombre.getText().trim(), txtMadera.getText().trim(), precio, stock);
            if (dao.agregarAdorno(nuevo)) {
                JOptionPane.showMessageDialog(this, "¡Adorno registrado correctamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                txtNombre.setText(""); txtMadera.setText(""); txtPrecio.setText(""); txtStock.setText("");
                cargarDatosTablaProductos();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: Precio y Stock deben ser numéricos.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosTablaProductos() {
        modeloTablaProductos.setRowCount(0);
        ArrayList<ERP> lista = dao.listarInventario();
        for (ERP p : lista) {
            modeloTablaProductos.addRow(new Object[]{p.getIdProducto(), p.getNombre(), p.getTipoMadera(), p.getPrecio(), p.getStock()});
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
