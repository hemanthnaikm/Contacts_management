package contact;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ContactsApp extends JFrame {

    // 1. Core MySQL Credentials (Change password to your local MySQL password)
    private static final String MYSQL_SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "ContactsDB";
    private static final String FULL_DB_URL = MYSQL_SERVER_URL + DB_NAME;
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1122"; 

    // UI Components
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JPanel mainListPanel;
    private JPanel favListPanel;
    private JTextField searchField;

    public ContactsApp() {
        // Run the fully automated database and table setup first
        initializeDatabaseAndTable();

        // Setup Main App Frame Window
        setTitle("Contacts Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null); 
        getContentPane().setBackground(Color.WHITE);

        // Top Navigation Menu Panel
        JPanel topMenu = new JPanel(new GridLayout(1, 3, 10, 0));
        topMenu.setBackground(Color.WHITE);
        topMenu.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnAdd = createStyledButton("Add Contact", new Color(0, 123, 255)); 
        JButton btnSearch = createStyledButton("Search Contact", new Color(108, 117, 125)); 
        JButton btnFav = createStyledButton("Favorites", new Color(40, 167, 69)); 

        topMenu.add(btnAdd);
        topMenu.add(btnSearch);
        topMenu.add(btnFav);

        // Card Layout View Setup
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);

        setupMainPage();
        setupAddContactPage();
        setupSearchPage();
        setupFavoritesPage();

        // Menu Button Functionality
        btnAdd.addActionListener(e -> cardLayout.show(cardPanel, "ADD"));
        btnSearch.addActionListener(e -> cardLayout.show(cardPanel, "SEARCH"));
        btnFav.addActionListener(e -> {
            refreshFavoritesList();
            cardLayout.show(cardPanel, "FAV");
        });

        setLayout(new BorderLayout());
        add(topMenu, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        // Load the database contacts into the view immediately on start
        refreshMainList();
    }

    // --- AUTOMATED DATABASE GENERATOR ---
    private void initializeDatabaseAndTable() {
        // Step A: Connect directly to MySQL Server to build the Database
        try (Connection serverConn = DriverManager.getConnection(MYSQL_SERVER_URL, DB_USER, DB_PASSWORD);
             Statement stmt = serverConn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("Database initialization verified/created successfully.");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Could not connect to MySQL Server. Please ensure MySQL is running and your root credentials match.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // Step B: Connect to the newly verified Database to build the Tables
        try (Connection dbConn = DriverManager.getConnection(FULL_DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = dbConn.createStatement()) {
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS contacts ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "first_name VARCHAR(50) NOT NULL, "
                    + "last_name VARCHAR(50) NOT NULL, "
                    + "phone_number VARCHAR(15) NOT NULL UNIQUE, "
                    + "email VARCHAR(100), "
                    + "is_favourite BOOLEAN DEFAULT FALSE"
                    + ");";
            
            stmt.executeUpdate(createTableSQL);
            System.out.println("Contacts table initialization verified/created successfully.");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error verifying database structural tables.", "Schema Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Helper to request a dynamic live DB connection
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(FULL_DB_URL, DB_USER, DB_PASSWORD);
    }

    // Modern Flat Button UI Factory
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- 1. MAIN PAGE PANEL VIEW ---
    private void setupMainPage() {
        JPanel mainPage = new JPanel(new BorderLayout());
        mainPage.setBackground(Color.WHITE);

        JLabel title = new JLabel("All Contacts", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        mainPage.add(title, BorderLayout.NORTH);

        mainListPanel = new JPanel();
        mainListPanel.setLayout(new BoxLayout(mainListPanel, BoxLayout.Y_AXIS));
        mainListPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(mainListPanel);
        scrollPane.setBorder(null);
        mainPage.add(scrollPane, BorderLayout.CENTER);

        cardPanel.add(mainPage, "MAIN");
    }

    private void refreshMainList() {
        mainListPanel.removeAll();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM contacts ORDER BY first_name ASC")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String fName = rs.getString("first_name");
                String lName = rs.getString("last_name");
                String phone = rs.getString("phone_number");
                String email = rs.getString("email");
                boolean isFav = rs.getBoolean("is_favourite");

                JPanel contactRow = new JPanel(new BorderLayout());
                contactRow.setBackground(new Color(245, 245, 245));
                contactRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
                contactRow.setMaximumSize(new Dimension(430, 50));

                JLabel nameLabel = new JLabel("  " + fName + " " + lName);
                nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                contactRow.add(nameLabel, BorderLayout.CENTER);

                contactRow.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showContactDetailDialog(id, fName, lName, phone, email, isFav);
                    }
                });

                mainListPanel.add(contactRow);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        mainListPanel.revalidate();
        mainListPanel.repaint();
    }

    // --- 2. DYNAMIC POP-UP MODAL DIALOG ---
    private void showContactDetailDialog(int id, String fName, String lName, String phone, String email, boolean isFav) {
        JDialog dialog = new JDialog(this, "Contact Details", true);
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblName = new JLabel("Full Name: " + fName + " " + lName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel lblPhone = new JLabel("Phone: " + phone);
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel lblEmail = new JLabel("Email: " + (email == null || email.isEmpty() ? "Not Added" : email));
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        body.add(lblName); body.add(Box.createVerticalStrut(10));
        body.add(lblPhone); body.add(Box.createVerticalStrut(10));
        body.add(lblEmail);

        JPanel footer = new JPanel(new GridLayout(1, 2, 10, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton btnToggleFav = createStyledButton(isFav ? "Unfavourite" : "Favourite", new Color(255, 193, 7)); 
        btnToggleFav.setForeground(Color.BLACK);
        JButton btnDelete = createStyledButton("Delete", new Color(220, 53, 69)); 

        btnToggleFav.addActionListener(e -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE contacts SET is_favourite = ? WHERE id = ?")) {
                ps.setBoolean(1, !isFav);
                ps.setInt(2, id);
                ps.executeUpdate();
                dialog.dispose();
                refreshMainList();
                refreshFavoritesList();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog, "Delete this contact?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM contacts WHERE id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    dialog.dispose();
                    refreshMainList();
                    refreshFavoritesList();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        footer.add(btnToggleFav);
        footer.add(btnDelete);

        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // --- 3. ADD CONTACT PANEL VIEW ---
    private void setupAddContactPage() {
        JPanel addPage = new JPanel(null);
        addPage.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Add New Contact");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setBounds(30, 20, 200, 30);
        addPage.add(lblTitle);

        JLabel lblReq = new JLabel("* First Name:"); lblReq.setBounds(30, 70, 100, 25); addPage.add(lblReq);
        JTextField txtFirst = new JTextField(); txtFirst.setBounds(140, 70, 250, 30); addPage.add(txtFirst);

        JLabel lblLast = new JLabel("Last Name:"); lblLast.setBounds(30, 120, 100, 25); addPage.add(lblLast);
        JTextField txtLast = new JTextField(); txtLast.setBounds(140, 120, 250, 30); addPage.add(txtLast);

        JLabel lblPhone = new JLabel("* Phone:"); lblPhone.setBounds(30, 170, 100, 25); addPage.add(lblPhone);
        JTextField txtPhone = new JTextField(); txtPhone.setBounds(140, 170, 250, 30); addPage.add(txtPhone);

        JLabel lblEmail = new JLabel("Email:"); lblEmail.setBounds(30, 220, 100, 25); addPage.add(lblEmail);
        JTextField txtEmail = new JTextField(); txtEmail.setBounds(140, 220, 250, 30); addPage.add(txtEmail);

        JButton btnSave = createStyledButton("Save Contact", new Color(40, 167, 69));
        btnSave.setBounds(140, 280, 150, 40);
        addPage.add(btnSave);

        btnSave.addActionListener(e -> {
            String fName = txtFirst.getText().trim();
            String lName = txtLast.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();

            if (fName.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(addPage, "First Name and Phone are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO contacts (first_name, last_name, phone_number, email) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, fName);
                ps.setString(2, lName);
                ps.setString(3, phone);
                ps.setString(4, email.isEmpty() ? null : email);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(addPage, "Contact Added Successfully!");
                txtFirst.setText(""); txtLast.setText(""); txtPhone.setText(""); txtEmail.setText("");
                refreshMainList();
                cardLayout.show(cardPanel, "MAIN");
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062) {
                    JOptionPane.showMessageDialog(addPage, "This phone number is already saved to another contact!", "Duplicate Contact", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(addPage, "Database execution error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            }
        });

        cardPanel.add(addPage, "ADD");
    }

    // --- 4. SEARCH PANEL VIEW ---
    private void setupSearchPage() {
        JPanel searchPage = new JPanel(new BorderLayout());
        searchPage.setBackground(Color.WHITE);

        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton btnDoSearch = createStyledButton("Search", new Color(0, 123, 255));

        topBar.add(searchField, BorderLayout.CENTER);
        topBar.add(btnDoSearch, BorderLayout.EAST);
        searchPage.add(topBar, BorderLayout.NORTH);

        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        searchPage.add(scrollPane, BorderLayout.CENTER);

        btnDoSearch.addActionListener(e -> {
            String query = searchField.getText().trim();
            resultsPanel.removeAll();

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM contacts WHERE first_name LIKE ? OR last_name LIKE ? OR phone_number LIKE ?")) {
                String match = "%" + query + "%";
                ps.setString(1, match); ps.setString(2, match); ps.setString(3, match);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String fName = rs.getString("first_name");
                    String lName = rs.getString("last_name");
                    String phone = rs.getString("phone_number");
                    String email = rs.getString("email");
                    boolean isFav = rs.getBoolean("is_favourite");

                    JPanel row = new JPanel(new BorderLayout());
                    row.setBackground(new Color(245, 245, 245));
                    row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
                    row.setMaximumSize(new Dimension(430, 50));

                    JLabel lbl = new JLabel("  " + fName + " " + lName + " (" + phone + ")");
                    row.add(lbl, BorderLayout.CENTER);

                    row.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            showContactDetailDialog(id, fName, lName, phone, email, isFav);
                        }
                    });
                    resultsPanel.add(row);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            resultsPanel.revalidate();
            resultsPanel.repaint();
        });

        cardPanel.add(searchPage, "SEARCH");
    }

    // --- 5. FAVORITES PANEL VIEW ---
    private void setupFavoritesPage() {
        JPanel favPage = new JPanel(new BorderLayout());
        favPage.setBackground(Color.WHITE);

        JLabel title = new JLabel("Favorites", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(255, 193, 7));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        favPage.add(title, BorderLayout.NORTH);

        favListPanel = new JPanel();
        favListPanel.setLayout(new BoxLayout(favListPanel, BoxLayout.Y_AXIS));
        favListPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(favListPanel);
        scrollPane.setBorder(null);
        favPage.add(scrollPane, BorderLayout.CENTER);

        cardPanel.add(favPage, "FAV");
    }

    private void refreshFavoritesList() {
        favListPanel.removeAll();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM contacts WHERE is_favourite = true ORDER BY first_name ASC")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String fName = rs.getString("first_name");
                String lName = rs.getString("last_name");
                String phone = rs.getString("phone_number");
                String email = rs.getString("email");

                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(new Color(254, 251, 234)); 
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
                row.setMaximumSize(new Dimension(430, 50));

                JLabel lbl = new JLabel("  ★ " + fName + " " + lName);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                row.add(lbl, BorderLayout.CENTER);

                row.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showContactDetailDialog(id, fName, lName, phone, email, true);
                    }
                });
                favListPanel.add(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        favListPanel.revalidate();
        favListPanel.repaint();
    }

    // --- MAIN ENGINE RUN ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ContactsApp().setVisible(true);
        });
    }
}
