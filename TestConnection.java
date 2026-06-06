import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/student_ms?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);

            if (conn != null) {
                System.out.println("SUCCESS: Database Connected!");
            }

            conn.close();

        } catch (Exception e) {
            System.out.println("FAILED: Connection error");
            e.printStackTrace();
        }
    }
}