package smoma.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbChecker {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/smoma_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "TruthOfVick26!";

        try (Connection c = DriverManager.getConnection(url, user, pass);
             Statement s = c.createStatement()) {

            String q = "SELECT id, username, email, matricule, role FROM users ORDER BY id DESC LIMIT 20";
            ResultSet rs = s.executeQuery(q);
            System.out.println("id\tusername\temail\tmatricule\trole");
            while (rs.next()) {
                System.out.printf("%d\t%s\t%s\t%s\t%s\n",
                        rs.getLong("id"), rs.getString("username"), rs.getString("email"), rs.getString("matricule"), rs.getString("role") );
            }
        } catch (Exception e) {
            System.err.println("DB check failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}
