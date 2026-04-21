import java.sql.*;
import java.util.Scanner;

public class Ejercicio3 {

    public static void main(String[] args) {

        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String usuario = "RIBERA";
        String contraseña = "ribera";

        Scanner sc = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {

            int opcion;

            do {
                System.out.println("--- MENU ---");
                System.out.println("1. Insertar incidencia");
                System.out.println("2. Mostrar todas");
                System.out.println("3. Mostrar incidencias de un ciclista");
                System.out.println("0. Salir");
                System.out.print("Opcion: ");
                opcion = sc.nextInt();

                switch (opcion) {
                    case 1:
                        insertarIncidencia(conn, sc);
                        break;
                    case 2:
                        mostrarTodas(conn);
                        break;
                    case 3:
                        mostrarPorCiclista(conn, sc);
                        break;
                }

            } while (opcion != 0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertarIncidencia(Connection conn, Scanner sc) {

        try {
            // Desactivar autocommit
            conn.setAutoCommit(false);

            System.out.print("ID ciclista: ");
            int idCiclista = sc.nextInt();

            System.out.print("Numero de etapa: ");
            int etapa = sc.nextInt();

            sc.nextLine(); // limpiar buffer
            System.out.print("Tipo incidencia: ");
            String tipo = sc.nextLine();



            // Verificar ciclista
            String checkCiclista = "SELECT COUNT(*) FROM ciclista WHERE id_ciclista = ?";
            PreparedStatement ps1 = conn.prepareStatement(checkCiclista);
            ps1.setInt(1, idCiclista);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();

            if (rs1.getInt(1) == 0) {
                System.out.println("El ciclista no existe");
                conn.rollback();
                return;
            }

            // Verificar etapa
            String checkEtapa = "SELECT COUNT(*) FROM etapa WHERE numero = ?";
            PreparedStatement ps2 = conn.prepareStatement(checkEtapa);
            ps2.setInt(1, etapa);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();

            if (rs2.getInt(1) == 0) {
                System.out.println("La etapa no existe");
                conn.rollback();
                return;
            }

            // Insertar incidencia
            String insert = "INSERT INTO incidencia (id_ciclista, numero_etapa, tipo) VALUES (?, ?, ?)";
            PreparedStatement ps3 = conn.prepareStatement(insert);
            ps3.setInt(1, idCiclista);
            ps3.setInt(2, etapa);
            ps3.setString(3, tipo);

            ps3.executeUpdate();

            conn.commit();
            System.out.println("Incidencia insertada correctamente");

        } catch (Exception e) {
            try {
                conn.rollback();
                System.out.println("Error -> rollback");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void mostrarTodas(Connection conn) {

        String sql = "SELECT i.id_incidencia, c.nombre, i.numero_etapa, i.tipo " +
                "FROM incidencia i JOIN ciclista c ON i.id_ciclista = c.id_ciclista";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                        rs.getInt(1) + " | " +
                                rs.getString(2) + " | Etapa " +
                                rs.getInt(3) + " | " +
                                rs.getString(4)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void mostrarPorCiclista(Connection conn, Scanner sc) {

        System.out.print("ID ciclista: ");
        int id = sc.nextInt();

        String sql = "SELECT i.id_incidencia, c.nombre, i.numero_etapa, i.tipo " +
                "FROM incidencia i JOIN ciclista c ON i.id_ciclista = c.id_ciclista " +
                "WHERE i.id_ciclista = ? " +
                "ORDER BY i.numero_etapa";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                        rs.getInt(1) + " | " +
                                rs.getString(2) + " | Etapa " +
                                rs.getInt(3) + " | " +
                                rs.getString(4)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}