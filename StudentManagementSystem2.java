import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.time.Year;

public class StudentManagementSystem2 {

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CONSTANTS — COLOURS & FONTS
    // ══════════════════════════════════════════════════════════════════════════
    static final Color NAVY      = new Color(15,  30,  60);
    static final Color NAVY2     = new Color(22,  36,  71);
    static final Color ACCENT    = new Color(79, 142, 247);
    static final Color TEAL      = new Color(56, 217, 169);
    static final Color WHITE     = Color.WHITE;
    static final Color BG        = new Color(244, 246, 251);
    static final Color MUTED     = new Color(107, 122, 153);
    static final Color BORDER    = new Color(226, 232, 244);
    static final Color RED       = new Color(226,  75,  74);
    static final Color AMBER     = new Color(250, 199, 117);
    static final Color GREEN_BG  = new Color(234, 243, 222);
    static final Color GREEN_FG  = new Color( 59, 109,  17);
    static final Color AMBER_BG  = new Color(250, 238, 218);
    static final Color AMBER_FG  = new Color(186, 117,  23);
    static final Color RED_BG    = new Color(252, 235, 235);
    static final Color RED_FG    = new Color(162,  45,  45);
    static final Color BLUE_BG   = new Color(230, 241, 251);
    static final Color BLUE_FG   = new Color( 24,  95, 165);
    static final Font  FONT_BOLD = new Font("SansSerif", Font.BOLD,  13);
    static final Font  FONT_REG  = new Font("SansSerif", Font.PLAIN, 13);
    static final Font  FONT_SM   = new Font("SansSerif", Font.PLAIN, 11);
    static final Font  FONT_H1   = new Font("SansSerif", Font.BOLD,  20);
    static final Font  FONT_H2   = new Font("SansSerif", Font.BOLD,  14);

    static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(8, 12, 8, 12));
    }
    static Border focusBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT),
            new EmptyBorder(8, 12, 8, 12));
    }
    static void focusEffect(JTextField f) {
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBorder(focusBorder()); }
            public void focusLost (FocusEvent e) { f.setBorder(inputBorder()); }
        });
    }
    static JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD); b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(9, 22, 9, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    static JTextField makeField() {
        JTextField f = new JTextField();
        f.setFont(FONT_REG); f.setBorder(inputBorder());
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        focusEffect(f);
        return f;
    }
    static JComboBox<String> makeCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_REG); cb.setBackground(WHITE);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return cb;
    }
    static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text); l.setFont(font); l.setForeground(color);
        return l;
    }
    static JLabel sectionLabel(String text) {
        JLabel l = makeLabel(text, FONT_H2, ACCENT);
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));
        return l;
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  DATABASE CONNECTION
    // ══════════════════════════════════════════════════════════════════════════
    static final String DB_URL      = "jdbc:mysql://localhost:3306/student_ms";
    static final String DB_USER     = "root";
    static final String DB_PASSWORD = "";

    static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                "MySQL JDBC Driver not found!\nAdd mysql-connector-j.jar to classpath.",
                "Driver Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to database!\n" + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  MODEL — Student
    // ══════════════════════════════════════════════════════════════════════════
    static class Student {
        String studentId, firstName, lastName, gender, dob, className, address, phone, status;

        Student() {}
        Student(String id, String fn, String ln, String gen, String dob,
                String cls, String addr, String ph, String st) {
            studentId = id; firstName = fn; lastName = ln; gender = gen;
            this.dob = dob; className = cls; address = addr; phone = ph; status = st;
        }
        String fullName() { return firstName + " " + lastName; }

        static Student fromResultSet(ResultSet rs) throws SQLException {
            return new Student(
                rs.getString("student_id"), rs.getString("first_name"),
                rs.getString("last_name"),  rs.getString("gender"),
                rs.getString("date_of_birth"), rs.getString("class_name"),
                rs.getString("address"),    rs.getString("phone"),
                rs.getString("status")
            );
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  MODEL — Marks
    // ══════════════════════════════════════════════════════════════════════════
    static class Marks {
        int markId; String studentId, subject, term, year; double score;
        Marks(String sid, String subj, double sc, String term, String yr) {
            studentId = sid; subject = subj; score = sc; this.term = term; year = yr;
        }
        static String grade(double avg) {
            if (avg >= 80) return "A"; if (avg >= 70) return "B";
            if (avg >= 60) return "C"; if (avg >= 50) return "D"; return "F";
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  DAO — Student
    // ══════════════════════════════════════════════════════════════════════════
    static class StudentDAO {

        String generateId() {
           /* String id = "STD-2025-001"; */
           String year = String.valueOf(Year.now().getValue());
String id = "STD-" + year + "-001";
            try (Connection c = getConnection()) {
                if (c == null) return id;
                ResultSet rs = c.createStatement()
                    .executeQuery("SELECT student_id FROM students ORDER BY student_id DESC LIMIT 1");
                if (rs.next()) {
                    String last = rs.getString(1);
                    int num = Integer.parseInt(last.substring(9)) + 1;
                   /* id = String.format("STD-2025-%03d", num);*/
                   id = String.format("STD-" + year + "-%03d", num);
                }
            } catch (Exception e) { e.printStackTrace(); }
            return id;
        }

        boolean add(Student s) {
            String sql = "INSERT INTO students VALUES (?,?,?,?,?,?,?,?,?)";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, s.studentId);  ps.setString(2, s.firstName);
                ps.setString(3, s.lastName);   ps.setString(4, s.gender);
                ps.setString(5, s.dob);        ps.setString(6, s.className);
                ps.setString(7, s.address);    ps.setString(8, s.phone);
                ps.setString(9, s.status);
                return ps.executeUpdate() > 0;
            } catch (Exception e) { e.printStackTrace(); return false; }
        }

        boolean update(Student s) {
            String sql = "UPDATE students SET first_name=?,last_name=?,gender=?,date_of_birth=?," +
                         "class_name=?,address=?,phone=?,status=? WHERE student_id=?";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, s.firstName); ps.setString(2, s.lastName);
                ps.setString(3, s.gender);    ps.setString(4, s.dob);
                ps.setString(5, s.className); ps.setString(6, s.address);
                ps.setString(7, s.phone);     ps.setString(8, s.status);
                ps.setString(9, s.studentId);
                return ps.executeUpdate() > 0;
            } catch (Exception e) { e.printStackTrace(); return false; }
        }

        boolean delete(String id) {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE student_id=?")) {
                ps.setString(1, id); return ps.executeUpdate() > 0;
            } catch (Exception e) { e.printStackTrace(); return false; }
        }

        List<Student> getAll() { return query("SELECT * FROM students ORDER BY first_name", null); }

        List<Student> byClass(String cls) {
            return query("SELECT * FROM students WHERE class_name=? ORDER BY first_name", cls);
        }

        List<Student> search(String kw) {
            String k = "%" + kw + "%";
            List<Student> list = new ArrayList<>();
            String sql = "SELECT * FROM students WHERE student_id LIKE ? OR first_name LIKE ? OR last_name LIKE ?";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(Student.fromResultSet(rs));
            } catch (Exception e) { e.printStackTrace(); }
            return list;
        }

        Student byId(String id) {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT * FROM students WHERE student_id=?")) {
                ps.setString(1, id); ResultSet rs = ps.executeQuery();
                if (rs.next()) return Student.fromResultSet(rs);
            } catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        int count() {
            try (Connection c = getConnection();
                 ResultSet rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next()) return rs.getInt(1);
            } catch (Exception e) { e.printStackTrace(); }
            return 0;
        }

        private List<Student> query(String sql, String param) {
            List<Student> list = new ArrayList<>();
            try (Connection c = getConnection()) {
                PreparedStatement ps = c.prepareStatement(sql);
                if (param != null) ps.setString(1, param);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(Student.fromResultSet(rs));
            } catch (Exception e) { e.printStackTrace(); }
            return list;
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  DAO — Marks
    // ══════════════════════════════════════════════════════════════════════════
    static class MarksDAO {

        boolean save(Marks m) {
            String chk = "SELECT mark_id FROM marks WHERE student_id=? AND subject=? AND term=? AND year=?";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(chk)) {
                ps.setString(1, m.studentId); ps.setString(2, m.subject);
                ps.setString(3, m.term);      ps.setString(4, m.year);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PreparedStatement up = c.prepareStatement("UPDATE marks SET score=? WHERE mark_id=?");
                    up.setDouble(1, m.score); up.setInt(2, rs.getInt(1));
                    return up.executeUpdate() > 0;
                } else {
                    PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO marks (student_id,subject,score,term,year) VALUES (?,?,?,?,?)");
                    ins.setString(1, m.studentId); ins.setString(2, m.subject);
                    ins.setDouble(3, m.score);     ins.setString(4, m.term);
                    ins.setString(5, m.year);
                    return ins.executeUpdate() > 0;
                }
            } catch (Exception e) { e.printStackTrace(); return false; }
        }

        List<Marks> getByStudent(String sid, String term, String year) {
            List<Marks> list = new ArrayList<>();
            String sql = "SELECT * FROM marks WHERE student_id=? AND term=? AND year=?";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, sid); ps.setString(2, term); ps.setString(3, year);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Marks mk = new Marks(rs.getString("student_id"), rs.getString("subject"),
                        rs.getDouble("score"), rs.getString("term"), rs.getString("year"));
                    mk.markId = rs.getInt("mark_id");
                    list.add(mk);
                }
            } catch (Exception e) { e.printStackTrace(); }
            return list;
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  UI — LOGIN FORM
    // ══════════════════════════════════════════════════════════════════════════
    static class LoginForm extends JFrame {
        JTextField     txtUser;
        JPasswordField txtPass;

        LoginForm() {
            setTitle("ScholarMS – Login");
            setSize(420, 500);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(false);
            getContentPane().setBackground(NAVY);
            setLayout(new GridBagLayout());
            add(buildCard());
        }

        JPanel buildCard() {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(WHITE);
            card.setBorder(new EmptyBorder(40, 40, 40, 40));
            card.setPreferredSize(new Dimension(340, 410));

            // Icon
            JLabel icon = new JLabel("S");
            icon.setFont(new Font("SansSerif", Font.BOLD, 24));
            icon.setForeground(WHITE); icon.setOpaque(true); icon.setBackground(ACCENT);
            icon.setHorizontalAlignment(SwingConstants.CENTER);
            icon.setPreferredSize(new Dimension(56, 56)); icon.setMaximumSize(new Dimension(56, 56));
            icon.setAlignmentX(CENTER_ALIGNMENT);

            JLabel title = makeLabel("ScholarMS", new Font("SansSerif", Font.BOLD, 22), NAVY);
            title.setAlignmentX(CENTER_ALIGNMENT);
            JLabel sub = makeLabel("Student Management System", FONT_SM, MUTED);
            sub.setAlignmentX(CENTER_ALIGNMENT);

            card.add(icon);
            card.add(Box.createVerticalStrut(10));
            card.add(title);
            card.add(Box.createVerticalStrut(4));
            card.add(sub);
            card.add(Box.createVerticalStrut(28));

            JLabel lUser = makeLabel("Username", FONT_BOLD, MUTED);
            lUser.setAlignmentX(LEFT_ALIGNMENT);
            card.add(lUser);
            card.add(Box.createVerticalStrut(5));
            txtUser = makeField(); txtUser.setText(""); txtUser.setAlignmentX(LEFT_ALIGNMENT);
            card.add(txtUser);
            card.add(Box.createVerticalStrut(14));

            JLabel lPass = makeLabel("Password", FONT_BOLD, MUTED);
            lPass.setAlignmentX(LEFT_ALIGNMENT);
            card.add(lPass);
            card.add(Box.createVerticalStrut(5));
            txtPass = new JPasswordField("");
            txtPass.setFont(FONT_REG); txtPass.setBorder(inputBorder());
            txtPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            txtPass.setAlignmentX(LEFT_ALIGNMENT);
            focusEffect(txtPass);
            card.add(txtPass);
            card.add(Box.createVerticalStrut(22));

            JButton btnLogin = new JButton("Sign In");
            btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
            btnLogin.setForeground(WHITE); btnLogin.setBackground(ACCENT);
            btnLogin.setFocusPainted(false); btnLogin.setBorderPainted(false);
            btnLogin.setBorder(new EmptyBorder(12, 0, 12, 0));
            btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnLogin.addActionListener(e -> doLogin());
            txtPass.addActionListener(e -> doLogin());
            card.add(btnLogin);

           /*  card.add(Box.createVerticalStrut(14));
            JLabel hint = makeLabel("Default: admin / admin123", FONT_SM, MUTED);
            hint.setAlignmentX(CENTER_ALIGNMENT);
            card.add(hint);*/
            JLabel hint = makeLabel("Enter your login credentials", FONT_SM, MUTED);
            return card;
        }

      void doLogin() {

    String u = txtUser.getText().trim();
    String p = new String(txtPass.getPassword()).trim();

    String sql =
        "SELECT * FROM users WHERE username=? AND password=?";

    try (
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)
    ) {

        ps.setString(1, u);
        ps.setString(2, p);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            dispose();
            new MainDashboard().setVisible(true);

        } else {

            JOptionPane.showMessageDialog(
                this,
                "Invalid username or password.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
            );

            txtPass.setText("");
            txtPass.requestFocus();
        }

    } catch (Exception e) {

        JOptionPane.showMessageDialog(
            this,
            e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
  }
    }

// ══════════════════════════════════════════════════════════════════════════
//  UI — MAIN DASHBOARD (FIXED)
// ══════════════════════════════════════════════════════════════════════════
static class MainDashboard extends JFrame {

    JPanel content;
    CardLayout cards;

    MainDashboard() {
        setTitle("ScholarMS – Student Management System");
        setSize(1120, 700);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ✅ MUST INITIALIZE FIRST
        cards = new CardLayout();
        content = new JPanel(cards);
        content.setBackground(BG);

        // ✅ ADD CARDS FIRST
        buildCards();

        // ✅ NOW SIDEBAR IS SAFE
        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────
    // SIDEBAR
    // ─────────────────────────────────────────────
    JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setBackground(NAVY);
        sb.setPreferredSize(new Dimension(220, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 18));
        logo.setBackground(NAVY);
        logo.setMaximumSize(new Dimension(220, 68));

        JLabel lIcon = new JLabel("S");
        lIcon.setFont(new Font("SansSerif", Font.BOLD, 15));
        lIcon.setForeground(WHITE);
        lIcon.setOpaque(true);
        lIcon.setBackground(ACCENT);
        lIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lIcon.setPreferredSize(new Dimension(34, 34));

        JPanel lText = new JPanel();
        lText.setLayout(new BoxLayout(lText, BoxLayout.Y_AXIS));
        lText.setBackground(NAVY);

        JLabel ln = new JLabel("ScholarMS");
        ln.setFont(new Font("SansSerif", Font.BOLD, 14));
        ln.setForeground(WHITE);

        JLabel ls = new JLabel("Student Management");
        ls.setFont(FONT_SM);
        ls.setForeground(new Color(150,160,190));

        lText.add(ln);
        lText.add(ls);
        logo.add(lIcon);
        logo.add(lText);

        sb.add(logo);
        sb.add(divider());

        String[][] nav = {
            {"Dashboard","DASHBOARD"},
            {"All Students","STUDENTS"},
            {"Register Student","REGISTER"},
            {"Marks Entry","MARKS"},
            {"Reports","REPORT"},
            {"Search","SEARCH"}
        };

        ButtonGroup bg = new ButtonGroup();
        JToggleButton first = null;

        for (String[] item : nav) {
            JToggleButton btn = navBtn(item[0], item[1]);
            bg.add(btn);
            sb.add(btn);
            if (first == null) {
                first = btn;
                btn.setSelected(true); // SAFE NOW
            }
        }

        sb.add(Box.createVerticalGlue());
        sb.add(divider());

        JButton logout = new JButton("  Logout");
        logout.setFont(FONT_REG);
        logout.setForeground(new Color(150,160,190));
        logout.setBackground(NAVY);
        logout.setBorderPainted(false);
        logout.setFocusPainted(false);
        logout.setMaximumSize(new Dimension(220,42));
        logout.setAlignmentX(LEFT_ALIGNMENT);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setBorder(new EmptyBorder(10,20,10,20));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        sb.add(logout);
        sb.add(Box.createVerticalStrut(10));
        return sb;
    }

    // ─────────────────────────────────────────────
    // NAV BUTTON
    // ─────────────────────────────────────────────
    JToggleButton navBtn(String label, String card) {
        JToggleButton btn = new JToggleButton("  " + label);
        btn.setFont(FONT_REG);
        btn.setForeground(new Color(160,170,200));
        btn.setBackground(NAVY);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(220, 40));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                btn.setForeground(WHITE);
                btn.setBackground(new Color(30,50,100));
                cards.show(content, card); // ✅ NEVER NULL NOW
            } else {
                btn.setForeground(new Color(160,170,200));
                btn.setBackground(NAVY);
            }
        });

        return btn;
    }

    JSeparator divider() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(40,55,90));
        s.setMaximumSize(new Dimension(220,1));
        return s;
    }

    // ─────────────────────────────────────────────
    // CARDS
    // ─────────────────────────────────────────────
    void buildCards() {
      content.add(new DashboardPanel(), "DASHBOARD"); 
    /*content.add(simplePage("Dashboard"), "DASHBOARD");*/

    content.add(new StudentListPanel(), "STUDENTS");
    content.add(new RegisterForm(null), "REGISTER");
    content.add(new MarksEntryForm(), "MARKS");
    content.add(new ReportForm(), "REPORT");
    content.add(new SearchForm(), "SEARCH");
}

    JPanel simplePage(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        JLabel l = new JLabel(title);
        l.setFont(FONT_H1);
        p.add(l);
        return p;
    }
}
    // ══════════════════════════════════════════════════════════════════════════
    //  UI — DASHBOARD
    // ══════════════════════════════════════════════════════════════════════════
    static class DashboardPanel extends JPanel {
        StudentDAO dao = new StudentDAO();

        DashboardPanel() {
            setBackground(BG); setLayout(new BorderLayout());
            setBorder(new EmptyBorder(28,28,28,28));
            JPanel wrap = new JPanel(); wrap.setLayout(new BoxLayout(wrap,BoxLayout.Y_AXIS));
            wrap.setBackground(BG);

            JLabel h = makeLabel("Dashboard", FONT_H1, NAVY); wrap.add(h);
            wrap.add(Box.createVerticalStrut(4));
            wrap.add(makeLabel("Welcome back, Administrator", FONT_REG, MUTED));
            wrap.add(Box.createVerticalStrut(22));

            // Stat cards
            JPanel stats = new JPanel(new GridLayout(1,4,14,0));
            stats.setBackground(BG); stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
            stats.add(statCard("Total Students", String.valueOf(dao.count()), "Registered", ACCENT));
            stats.add(statCard("Active Classes",  "18",    "All running",     TEAL));
            stats.add(statCard("Avg Score",       "74.2%", "↑ from last term",AMBER));
            stats.add(statCard("Pending Reports", "6",     "Needs attention", RED));
            wrap.add(stats);
            wrap.add(Box.createVerticalStrut(24));

            // Quick actions
            wrap.add(makeLabel("Quick Actions", FONT_H2, NAVY));
            wrap.add(Box.createVerticalStrut(12));
            JPanel qa = new JPanel(new GridLayout(1,3,14,0));
            qa.setBackground(BG); qa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            qa.add(quickCard("Register New Student", ACCENT));
            qa.add(quickCard("Enter Marks",          TEAL));
            qa.add(quickCard("View Reports",         AMBER));
            wrap.add(qa);

            add(wrap, BorderLayout.NORTH);
        }

        JPanel statCard(String title, String val, String sub, Color accent) {
            JPanel c = new JPanel(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));
            c.setBackground(WHITE);
            c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(16,20,16,20)));
            JLabel v = makeLabel(val,   new Font("SansSerif",Font.BOLD,26), accent);
            JLabel t = makeLabel(title, new Font("SansSerif",Font.BOLD,12), MUTED);
            JLabel s = makeLabel(sub,   FONT_SM, MUTED);
            c.add(t); c.add(Box.createVerticalStrut(6)); c.add(v);
            c.add(Box.createVerticalStrut(4)); c.add(s);
            return c;
        }

        JPanel quickCard(String text, Color color) {
            JPanel p = new JPanel(new BorderLayout()); p.setBackground(WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(16,14,16,14)));
            JPanel bar = new JPanel(); bar.setBackground(color); bar.setPreferredSize(new Dimension(5,0));
            JLabel lbl = makeLabel(text, FONT_BOLD, NAVY);
            p.add(bar, BorderLayout.WEST); p.add(Box.createHorizontalStrut(12), BorderLayout.CENTER);
            p.add(lbl, BorderLayout.EAST);
            return p;
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  UI — STUDENT LIST
    // ══════════════════════════════════════════════════════════════════════════
    static class StudentListPanel extends JPanel {
        DefaultTableModel model;
        JTable table;
        JTextField txtSearch;
        JComboBox<String> cmbClass;
        StudentDAO dao = new StudentDAO();

        StudentListPanel() {
            setBackground(BG); setLayout(new BorderLayout());
            setBorder(new EmptyBorder(28,28,28,28));
            add(topBar(),   BorderLayout.NORTH);
            add(tableArea(),BorderLayout.CENTER);
        }

        JPanel topBar() {
            JPanel wrap = new JPanel(); wrap.setLayout(new BoxLayout(wrap,BoxLayout.Y_AXIS));
            wrap.setBackground(BG);
            wrap.add(makeLabel("All Students", FONT_H1, NAVY));
            wrap.add(Box.createVerticalStrut(16));
            JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            bar.setBackground(BG);
            txtSearch = new JTextField(20); txtSearch.setFont(FONT_REG);
            txtSearch.setBorder(inputBorder()); focusEffect(txtSearch);
            txtSearch.setToolTipText("Search by name or ID");
            cmbClass = makeCombo("All Classes",
                     "Senior 1A","Senior 1B",
                     "Senior 2A","Senior 2B",
                     "Senior 3A","Senior 3B",
                     "Senior 4A","Senior 4B",
                     "Senior 5A","Senior 5B",
                     "Senior 6A","Senior 6B");
            cmbClass.setPreferredSize(new Dimension(150,34));
            JButton btnS = makeBtn("Search",  ACCENT, WHITE);
            JButton btnR = makeBtn("Refresh", WHITE, NAVY);
            btnR.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(9,18,9,18)));
            btnS.addActionListener(e -> load());
            btnR.addActionListener(e -> { txtSearch.setText(""); cmbClass.setSelectedIndex(0); load(); });
            bar.add(makeLabel("Search:", FONT_BOLD, MUTED));
            bar.add(txtSearch); bar.add(cmbClass); bar.add(btnS); bar.add(btnR);
            wrap.add(bar); wrap.add(Box.createVerticalStrut(14));
            return wrap;
        }

        JScrollPane tableArea() {
            String[] cols = {"Student ID","First Name","Last Name","Gender","DOB","Class","Phone","Status","Actions"};
            model = new DefaultTableModel(cols,0) {
                public boolean isCellEditable(int r,int c) { return c==8; }
            };
            table = new JTable(model);
            table.setRowHeight(40); table.setFont(FONT_REG);
            table.setShowHorizontalLines(true); table.setGridColor(BORDER);
            table.setSelectionBackground(new Color(235,242,255));
            table.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
            table.getTableHeader().setBackground(BG);
            table.getTableHeader().setForeground(MUTED);
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER));
            table.getColumn("Status").setCellRenderer(new StatusRenderer());
            table.getColumn("Actions").setCellRenderer(new ActionRenderer());
            table.getColumn("Actions").setCellEditor(new ActionEditor(new JCheckBox(), dao, this));
            table.setFillsViewportHeight(true);
            JScrollPane sc = new JScrollPane(table);
            sc.setBorder(BorderFactory.createLineBorder(BORDER));
            sc.getViewport().setBackground(WHITE);
            load(); return sc;
        }

        void load() {
            model.setRowCount(0);
            String kw  = txtSearch.getText().trim();
            String cls = (String) cmbClass.getSelectedItem();
            List<Student> list = !kw.isEmpty() ? dao.search(kw)
                : !"All Classes".equals(cls) ? dao.byClass(cls) : dao.getAll();
            for (Student s : list)
                model.addRow(new Object[]{s.studentId,s.firstName,s.lastName,
                    s.gender,s.dob,s.className,s.phone,s.status,"actions"});
        }

        // ── Renderers / Editors ────────────────────────────────────────────
        static class StatusRenderer extends DefaultTableCellRenderer {
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c) {
                JLabel l = new JLabel(v==null?"":v.toString());
                l.setFont(new Font("SansSerif",Font.BOLD,11)); l.setOpaque(true);
                l.setHorizontalAlignment(CENTER); l.setBorder(new EmptyBorder(4,10,4,10));
                if ("Active".equals(v)) { l.setBackground(GREEN_BG); l.setForeground(GREEN_FG); }
                else                   { l.setBackground(AMBER_BG);  l.setForeground(AMBER_FG); }
                return l;
            }
        }

        static class ActionRenderer extends DefaultTableCellRenderer {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER,6,5));
            ActionRenderer() {
                p.setBackground(WHITE);
                p.add(smallBtn("Edit",   ACCENT)); p.add(smallBtn("Delete", RED));
            }
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){return p;}
        }

        static class ActionEditor extends DefaultCellEditor {
            JPanel    p   = new JPanel(new FlowLayout(FlowLayout.CENTER,6,5));
            JButton   bEd = smallBtn("Edit",   ACCENT);
            JButton   bDl = smallBtn("Delete", RED);
            StudentDAO dao; StudentListPanel parent; int row;
            ActionEditor(JCheckBox cb, StudentDAO dao, StudentListPanel parent) {
                super(cb); this.dao=dao; this.parent=parent;
                p.setBackground(WHITE); p.add(bEd); p.add(bDl);
                bEd.addActionListener(e -> {
                    fireEditingStopped();
                    String sid = (String)parent.model.getValueAt(row,0);
                    Student s = dao.byId(sid);
                    if (s!=null) {
                        JDialog dlg = new JDialog();
                        dlg.setTitle("Update Student"); dlg.setSize(740,560);
                        dlg.setLocationRelativeTo(null); dlg.setModal(true);
                        dlg.add(new RegisterForm(s)); dlg.setVisible(true);
                        parent.load();
                    }
                });
                bDl.addActionListener(e -> {
                    fireEditingStopped();
                    String sid  = (String)parent.model.getValueAt(row,0);
                    String name = parent.model.getValueAt(row,1)+" "+parent.model.getValueAt(row,2);
                    int ok = JOptionPane.showConfirmDialog(null,"Delete student: "+name+"?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (ok==JOptionPane.YES_OPTION) {
                        if(dao.delete(sid)) { JOptionPane.showMessageDialog(null,"Student deleted."); parent.load(); }
                    }
                });
            }
            public Component getTableCellEditorComponent(JTable t,Object v,boolean sel,int r,int c){row=r;return p;}
            public Object getCellEditorValue(){return "";}
        }

        static JButton smallBtn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("SansSerif",Font.BOLD,11));
            b.setBackground(bg); b.setForeground(WHITE);
            b.setBorderPainted(false); b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(5,12,5,12));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  UI — REGISTER / UPDATE FORM
    // ══════════════════════════════════════════════════════════════════════════
    static class RegisterForm extends JPanel {
        JTextField    txtFirst, txtLast, txtDob, txtAddress, txtPhone;
        JComboBox<String> cmbGender, cmbClass;
        StudentDAO dao = new StudentDAO();
        Student editing;

        RegisterForm(Student s) {
            editing = s;
            setBackground(BG); setLayout(new BorderLayout());
            setBorder(new EmptyBorder(28,28,28,28));
            JScrollPane sc = new JScrollPane(buildForm());
            sc.setBorder(null); sc.getViewport().setBackground(BG);
            add(sc, BorderLayout.CENTER);
        }

        JPanel buildForm() {
            JPanel form = new JPanel(); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
            form.setBackground(BG);
            String title = editing==null ? "Register New Student" : "Update Student";
            form.add(makeLabel(title, FONT_H1, NAVY));
            form.add(Box.createVerticalStrut(20));

            JPanel card = new JPanel(new GridBagLayout());
            card.setBackground(WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(24,28,28,28)));

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(0,0,14,14);

            // Personal Info section
            g.gridx=0; g.gridy=0; g.gridwidth=2; g.insets=new Insets(0,0,12,0);
            card.add(sectionLabel("Personal Information"), g);
            g.insets=new Insets(0,0,14,14); g.gridwidth=1;

            txtFirst   = makeField(); txtLast  = makeField();
            cmbGender  = makeCombo("Male","Female");
            txtDob     = makeField(); txtDob.setToolTipText("YYYY-MM-DD");
            txtAddress = makeField(); txtPhone = makeField();

            addFormField(card,g,"First Name *",   txtFirst,   0,1,false);
            addFormField(card,g,"Last Name *",    txtLast,    1,1,false);
            addFormField(card,g,"Gender *",       cmbGender,  0,2,false);
            addFormField(card,g,"Date of Birth (YYYY-MM-DD)", txtDob, 1,2,false);
            addFormField(card,g,"Address",        txtAddress, 0,3,true);
            addFormField(card,g,"Phone Number",   txtPhone,   0,4,false);

            // Academic section
            g.gridx=0; g.gridy=5; g.gridwidth=2; g.insets=new Insets(14,0,12,0);
            card.add(sectionLabel("Academic Information"), g);
            g.insets=new Insets(0,0,14,14); g.gridwidth=1;

            cmbClass = makeCombo(
               "Senior 1A", "Senior 1B", "Senior 2A", "Senior 2B", "Senior 3A", "Senior 3B", "Senior 4A","Senior 4B","Senior 5A","Senior 5B","Senior 6A","Senior 6B"
            );
            addFormField(card,g,"Class *", cmbClass, 0,6,false);

            // Pre-fill if editing
            if (editing!=null) {
                txtFirst.setText(editing.firstName); txtLast.setText(editing.lastName);
                cmbGender.setSelectedItem(editing.gender); txtDob.setText(editing.dob);
                txtAddress.setText(editing.address);       txtPhone.setText(editing.phone);
                cmbClass.setSelectedItem(editing.className);
            }

            // Buttons
            g.gridx=0; g.gridy=7; g.gridwidth=2; g.insets=new Insets(12,0,0,0);
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
            btns.setBackground(WHITE);
            JButton bClear = makeBtn("Clear Form", WHITE, MUTED);
            bClear.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(9,22,9,22)));
            JButton bSave = makeBtn(editing==null?"Register Student":"Save Changes", WHITE, ACCENT);
            bSave.setBackground(ACCENT); bSave.setForeground(WHITE);
            bClear.addActionListener(e -> clearForm());
            bSave .addActionListener(e -> save());
            btns.add(bClear); btns.add(bSave);
            card.add(btns, g);

            form.add(card);
            return form;
        }

        void addFormField(JPanel p, GridBagConstraints g, String label, JComponent comp, int col, int row, boolean full) {
            g.gridx=col; g.gridy=row; g.gridwidth=full?2:1; g.weightx=1;
            JPanel w = new JPanel(); w.setLayout(new BoxLayout(w,BoxLayout.Y_AXIS)); w.setBackground(WHITE);
            JLabel l = makeLabel(label, new Font("SansSerif",Font.BOLD,11), MUTED);
            w.add(l); w.add(Box.createVerticalStrut(4));
            comp.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
            w.add(comp); p.add(w,g);
        }

        void save() {
            if (txtFirst.getText().trim().isEmpty()||txtLast.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,"First and Last name are required.","Validation",JOptionPane.WARNING_MESSAGE);
                return;
            }
            Student s = editing==null ? new Student() : editing;
            if (editing==null) s.studentId = dao.generateId();
            s.firstName = txtFirst.getText().trim(); s.lastName  = txtLast.getText().trim();
            s.gender    = (String)cmbGender.getSelectedItem();
            s.dob       = txtDob.getText().trim();
            s.address   = txtAddress.getText().trim(); s.phone = txtPhone.getText().trim();
            s.className = (String)cmbClass.getSelectedItem(); s.status = "Active";
            boolean ok = editing==null ? dao.add(s) : dao.update(s);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                    editing==null ? "Student registered! ID: "+s.studentId : "Student updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                if (editing==null) clearForm();
            } else {
                JOptionPane.showMessageDialog(this,"Operation failed. Check DB connection.","Error",JOptionPane.ERROR_MESSAGE);
            }
        }

        void clearForm() {
            txtFirst.setText(""); txtLast.setText(""); txtDob.setText("");
            txtAddress.setText(""); txtPhone.setText("");
            cmbGender.setSelectedIndex(0); cmbClass.setSelectedIndex(0);
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  UI — MARKS ENTRY
    // ══════════════════════════════════════════════════════════════════════════
    static class MarksEntryForm extends JPanel {
        static final String[] SUBJECTS = {"Mathematics","English","Science","History","ICT"};
        DefaultTableModel model;
        JTable table;
        JComboBox<String> cmbClass, cmbTerm;
        StudentDAO sDao = new StudentDAO();
        MarksDAO   mDao = new MarksDAO();

        MarksEntryForm() {
            setBackground(BG); setLayout(new BorderLayout());
            setBorder(new EmptyBorder(28,28,28,28));
            add(header(), BorderLayout.NORTH);
            add(tableArea(), BorderLayout.CENTER);
        }

        JPanel header() {
            JPanel wrap = new JPanel(); wrap.setLayout(new BoxLayout(wrap,BoxLayout.Y_AXIS));
            wrap.setBackground(BG);
            wrap.add(makeLabel("Marks Entry", FONT_H1, NAVY));
            wrap.add(Box.createVerticalStrut(16));
            JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
            bar.setBackground(BG);
            cmbClass = makeCombo("--Select Class--",
               "Senior 1A", "Senior 1B", "Senior 2A", "Senior 2B", "Senior 3A", "Senior 3B", "Senior 4A","Senior 4B","Senior 5A","Senior 5B","Senior 6A","Senior 6B"
            );
            cmbClass.setPreferredSize(new Dimension(170,34));
            cmbTerm  = makeCombo("Term 1","Term 2","Term 3");
            cmbTerm.setPreferredSize(new Dimension(120,34));
            JButton bLoad = makeBtn("Load Students",   ACCENT, WHITE);
            JButton bSave = makeBtn("Save All Marks",  TEAL,   NAVY);
            bLoad.addActionListener(e -> load());
            bSave.addActionListener(e -> saveAll());
            bar.add(makeLabel("Class:",FONT_BOLD,MUTED)); bar.add(cmbClass);
            bar.add(makeLabel("Term:", FONT_BOLD,MUTED)); bar.add(cmbTerm);
            bar.add(bLoad); bar.add(bSave);
            wrap.add(bar); wrap.add(Box.createVerticalStrut(16));
            return wrap;
        }

        JScrollPane tableArea() {
            String[] cols = {"ID","Student Name","Mathematics","English","Science","History","ICT","Total","Average","Grade"};
            model = new DefaultTableModel(cols,0) {
                public boolean isCellEditable(int r,int c){return c>=2&&c<=6;}
            };
            table = new JTable(model);
            table.setRowHeight(36); table.setFont(FONT_REG);
            table.setGridColor(BORDER); table.setSelectionBackground(new Color(235,242,255));
            table.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
            table.getTableHeader().setBackground(BG); table.getTableHeader().setForeground(MUTED);
            table.getColumn("Grade").setCellRenderer(new GradeRenderer());
            table.setFillsViewportHeight(true);
            model.addTableModelListener(e -> {
                if (e.getColumn()>=2&&e.getColumn()<=6) calcRow(e.getFirstRow());
            });
            JScrollPane sc = new JScrollPane(table);
            sc.setBorder(BorderFactory.createLineBorder(BORDER)); sc.getViewport().setBackground(WHITE);
            return sc;
        }

        void load() {
            String cls = (String)cmbClass.getSelectedItem();
            if (cls==null||cls.startsWith("--")) {
                JOptionPane.showMessageDialog(this,"Please select a class.","Info",JOptionPane.WARNING_MESSAGE); return;
            }
            model.setRowCount(0);
            String term=(String)cmbTerm.getSelectedItem(); String year="2025";
            for (Student s : sDao.byClass(cls)) {
                List<Marks> ex = mDao.getByStudent(s.studentId,term,year);
                double[] scores = new double[SUBJECTS.length];
                for (Marks m : ex)
                    for (int i=0;i<SUBJECTS.length;i++)
                        if (SUBJECTS[i].equalsIgnoreCase(m.subject)) scores[i]=m.score;
                double tot=0; for (double d:scores) tot+=d;
                double avg=tot/SUBJECTS.length;
                model.addRow(new Object[]{s.studentId,s.fullName(),
                    scores[0],scores[1],scores[2],scores[3],scores[4],
                    tot,String.format("%.1f%%",avg),Marks.grade(avg)});
            }
        }

        void calcRow(int row) {
            try {
                double tot=0;
                for (int c=2;c<=6;c++){Object v=model.getValueAt(row,c); tot+=v==null?0:Double.parseDouble(v.toString());}
                double avg=tot/5;
                model.setValueAt(tot,row,7);
                model.setValueAt(String.format("%.1f%%",avg),row,8);
                model.setValueAt(Marks.grade(avg),row,9);
            } catch(Exception ignored){}
        }

        void saveAll() {
            if (model.getRowCount()==0){JOptionPane.showMessageDialog(this,"No marks to save.","Info",JOptionPane.INFORMATION_MESSAGE);return;}
            String term=(String)cmbTerm.getSelectedItem(); String year="2025"; int saved=0;
            for (int r=0;r<model.getRowCount();r++) {
                String sid=(String)model.getValueAt(r,0);
                for (int c=0;c<SUBJECTS.length;c++) {
                    Object v=model.getValueAt(r,c+2);
                    if (v!=null&&!v.toString().isEmpty()) {
                        double sc=Double.parseDouble(v.toString());
                        if (mDao.save(new Marks(sid,SUBJECTS[c],sc,term,year))) saved++;
                    }
                }
            }
            JOptionPane.showMessageDialog(this,saved+" mark records saved.","Success",JOptionPane.INFORMATION_MESSAGE);
        }

        static class GradeRenderer extends DefaultTableCellRenderer {
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                JLabel l=new JLabel(v==null?"":v.toString());
                l.setFont(new Font("SansSerif",Font.BOLD,12)); l.setOpaque(true); l.setHorizontalAlignment(CENTER);
                switch(v==null?"":v.toString()){
                    case "A":l.setBackground(GREEN_BG);l.setForeground(GREEN_FG);break;
                    case "B":l.setBackground(BLUE_BG); l.setForeground(BLUE_FG); break;
                    case "C":l.setBackground(AMBER_BG);l.setForeground(AMBER_FG);break;
                    default: l.setBackground(RED_BG);  l.setForeground(RED_FG);
                }
                return l;
            }
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  UI — REPORT FORM
    // ══════════════════════════════════════════════════════════════════════════
    static class ReportForm extends JPanel {
        DefaultTableModel model;
        JComboBox<String> cmbClass, cmbTerm;
        StudentDAO sDao = new StudentDAO();
        MarksDAO   mDao = new MarksDAO();

        ReportForm() {
            setBackground(BG); setLayout(new BorderLayout());
            setBorder(new EmptyBorder(28,28,28,28));
            add(topArea(), BorderLayout.NORTH);
            add(tableArea(), BorderLayout.CENTER);
        }

        JPanel topArea() {
            JPanel wrap = new JPanel(); wrap.setLayout(new BoxLayout(wrap,BoxLayout.Y_AXIS));
            wrap.setBackground(BG);
            wrap.add(makeLabel("Academic Reports", FONT_H1, NAVY));
            wrap.add(Box.createVerticalStrut(16));

            JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
            bar.setBackground(BG);
            cmbClass = makeCombo(
               "Senior 1A", "Senior 1B", "Senior 2A", "Senior 2B", "Senior 3A", "Senior 3B", "Senior 4A","Senior 4B","Senior 5A","Senior 5B","Senior 6A","Senior 6B"
            );
            cmbClass.setPreferredSize(new Dimension(170,34));
            cmbTerm = makeCombo("Term 1","Term 2","Term 3");
            cmbTerm.setPreferredSize(new Dimension(120,34));
            JButton bGen = makeBtn("Generate Report", ACCENT, WHITE);
            bGen.addActionListener(e -> generate());
            bar.add(makeLabel("Class:",FONT_BOLD,MUTED)); bar.add(cmbClass);
            bar.add(makeLabel("Term:", FONT_BOLD,MUTED)); bar.add(cmbTerm);
            bar.add(bGen);
            wrap.add(bar); wrap.add(Box.createVerticalStrut(16));
            return wrap;
        }

        JScrollPane tableArea() {
            String[] cols = {"Rank","Student ID","Student Name","Math","Eng","Sci","Hist","ICT","Total","Average","Grade"};
            model = new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            JTable t = new JTable(model);
            t.setRowHeight(36); t.setFont(FONT_REG); t.setGridColor(BORDER);
            t.setSelectionBackground(new Color(235,242,255));
            t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
            t.getTableHeader().setBackground(BG); t.getTableHeader().setForeground(MUTED);
            t.getColumn("Grade").setCellRenderer(new MarksEntryForm.GradeRenderer());
            t.setFillsViewportHeight(true);
            JScrollPane sc = new JScrollPane(t);
            sc.setBorder(BorderFactory.createLineBorder(BORDER)); sc.getViewport().setBackground(WHITE);
            return sc;
        }

        void generate() {
            String cls=(String)cmbClass.getSelectedItem();
            if(cls==null||cls.startsWith("--")){JOptionPane.showMessageDialog(this,"Select a class.","Info",JOptionPane.WARNING_MESSAGE);return;}
            model.setRowCount(0);
            String term=(String)cmbTerm.getSelectedItem(); String year="2025";
            String[] SUBJECTS={"Mathematics","English","Science","History","ICT"};

            List<Object[]> rows = new ArrayList<>();
            for (Student s : sDao.byClass(cls)) {
                List<Marks> ex = mDao.getByStudent(s.studentId,term,year);
                double[] scores = new double[5];
                for (Marks m : ex)
                    for (int i=0;i<SUBJECTS.length;i++)
                        if (SUBJECTS[i].equalsIgnoreCase(m.subject)) scores[i]=m.score;
                double tot=0; for (double d:scores) tot+=d; double avg=tot/5;
                rows.add(new Object[]{s.studentId,s.fullName(),scores[0],scores[1],
                    scores[2],scores[3],scores[4],tot,avg,Marks.grade(avg)});
            }
            rows.sort((a,b)->Double.compare((double)b[8],(double)a[8]));
            int rank=1;
            for (Object[] r : rows)
                model.addRow(new Object[]{rank++,r[0],r[1],r[2],r[3],r[4],r[5],r[6],
                    r[7],String.format("%.1f%%",(double)r[8]),r[9]});
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  UI — SEARCH FORM
    // ══════════════════════════════════════════════════════════════════════════
    static class SearchForm extends JPanel {
        JTextField txtId, txtName;
        JComboBox<String> cmbClass;
        DefaultTableModel model;
        StudentDAO dao = new StudentDAO();

        SearchForm() {
            setBackground(BG); setLayout(new BorderLayout());
            setBorder(new EmptyBorder(28,28,28,28));
            add(searchArea(), BorderLayout.NORTH);
            add(resultsArea(), BorderLayout.CENTER);
        }

        JPanel searchArea() {
            JPanel wrap = new JPanel(); wrap.setLayout(new BoxLayout(wrap,BoxLayout.Y_AXIS));
            wrap.setBackground(BG);
            wrap.add(makeLabel("Search Students", FONT_H1, NAVY));
            wrap.add(Box.createVerticalStrut(16));

            JPanel card = new JPanel(new GridBagLayout());
            card.setBackground(WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(20,24,20,24)));
            GridBagConstraints g = new GridBagConstraints();
            g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(0,0,12,14); g.weightx=1;

            txtId   = makeField(); txtId.setToolTipText("e.g. STD-2025-001");
            txtName = makeField(); txtName.setToolTipText("First or last name");
            cmbClass = makeCombo(
               "Any Class","Senior 1A", "Senior 1B", "Senior 2A", "Senior 2B", "Senior 3A", "Senior 3B", "Senior 4A","Senior 4B","Senior 5A","Senior 5B","Senior 6A","Senior 6B"
            );

            g.gridx=0;g.gridy=0; addSearchField(card,g,"Student ID",txtId);
            g.gridx=1;g.gridy=0; addSearchField(card,g,"Student Name",txtName);
            g.gridx=2;g.gridy=0; addSearchField(card,g,"Class",cmbClass);

            JButton bSearch = makeBtn("Search", ACCENT, WHITE);
            JButton bClear  = makeBtn("Clear",  WHITE,  MUTED);
            bClear.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(9,22,9,22)));
            bSearch.addActionListener(e -> doSearch());
            bClear .addActionListener(e -> { txtId.setText(""); txtName.setText(""); cmbClass.setSelectedIndex(0); model.setRowCount(0); });

            g.gridx=3;g.gridy=0; g.weightx=0;
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
            btns.setBackground(WHITE);
            JLabel sp = new JLabel(" ");sp.setFont(FONT_SM);btns.add(sp);
            btns.add(bClear); btns.add(bSearch);
            card.add(btns,g);

            wrap.add(card); wrap.add(Box.createVerticalStrut(16));
            return wrap;
        }

        void addSearchField(JPanel p, GridBagConstraints g, String label, JComponent comp) {
            JPanel w=new JPanel(); w.setLayout(new BoxLayout(w,BoxLayout.Y_AXIS)); w.setBackground(WHITE);
            w.add(makeLabel(label,new Font("SansSerif",Font.BOLD,11),MUTED));
            w.add(Box.createVerticalStrut(4));
            comp.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
            w.add(comp); p.add(w,g);
        }

        JScrollPane resultsArea() {
            String[] cols={"Student ID","First Name","Last Name","Gender","Date of Birth","Class","Phone","Status"};
            model=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            JTable t=new JTable(model);
            t.setRowHeight(36);t.setFont(FONT_REG);t.setGridColor(BORDER);
            t.setSelectionBackground(new Color(235,242,255));
            t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
            t.getTableHeader().setBackground(BG);t.getTableHeader().setForeground(MUTED);
            t.getColumn("Status").setCellRenderer(new StudentListPanel.StatusRenderer());
            t.setFillsViewportHeight(true);
            JScrollPane sc=new JScrollPane(t);
            sc.setBorder(BorderFactory.createLineBorder(BORDER));sc.getViewport().setBackground(WHITE);
            return sc;
        }

        void doSearch() {
            model.setRowCount(0);
            String id  = txtId.getText().trim();
            String nm  = txtName.getText().trim();
            String cls = (String)cmbClass.getSelectedItem();
            List<Student> all = dao.getAll();
            for (Student s : all) {
                boolean mId  = id.isEmpty()  || s.studentId.toLowerCase().contains(id.toLowerCase());
                boolean mNm  = nm.isEmpty()  || s.fullName().toLowerCase().contains(nm.toLowerCase());
                boolean mCls = "Any Class".equals(cls) || s.className.equals(cls);
                if (mId&&mNm&&mCls)
                    model.addRow(new Object[]{s.studentId,s.firstName,s.lastName,
                        s.gender,s.dob,s.className,s.phone,s.status});
            }
            if (model.getRowCount()==0)
                JOptionPane.showMessageDialog(this,"No students found matching your criteria.",
                    "No Results",JOptionPane.INFORMATION_MESSAGE);
        }
    }

}