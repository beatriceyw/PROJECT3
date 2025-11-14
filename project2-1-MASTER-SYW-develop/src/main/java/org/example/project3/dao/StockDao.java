package org.example.project3.dao;

import org.example.project3.dto.StockDTO;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StockDao {
    private final String url;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC 드라이버를 찾을 수 없습니다.", e);
        }
    }

    public StockDao(String sqliteUrl) {
        if (sqliteUrl == null || !sqliteUrl.startsWith("jdbc:sqlite:")) {
            throw new IllegalArgumentException("잘못된 SQLite URL: " + sqliteUrl);
        }
        this.url = sqliteUrl;
        ensureTable();
    }

    private Connection conn() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Stocks (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " stock_code TEXT UNIQUE NOT NULL," +
                " stock_name TEXT," +
                " create_date TEXT DEFAULT (datetime('now','localtime'))," +
                " pbr REAL," +
                " per REAL" +
                ")";
        try (Connection c = conn(); Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(detail("테이블 생성 실패", e), e);
        }
    }

    public List<StockDTO> findAllOrderByName() {
        String sql = "SELECT id, stock_code, stock_name, pbr, per, create_date " +
                "FROM Stocks ORDER BY stock_name COLLATE NOCASE ASC, id ASC";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapList(rs);
        } catch (SQLException e) {
            throw new RuntimeException(detail("SELECT 실패(이름순)", e), e);
        }
    }

    public List<StockDTO> findAllOrderByInserted() {
        String sql = "SELECT id, stock_code, stock_name, pbr, per, create_date " +
                "FROM Stocks ORDER BY id ASC";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapList(rs);
        } catch (SQLException e) {
            throw new RuntimeException(detail("SELECT 실패(입력순)", e), e);
        }
    }

    public List<StockDTO> search(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) return findAllOrderByName();

        String sql = "SELECT id, stock_code, stock_name, pbr, per, create_date " +
                "FROM Stocks " +
                "WHERE stock_code LIKE ? OR stock_name LIKE ? " +
                "ORDER BY stock_name COLLATE NOCASE ASC, id ASC";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + kw + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(detail("SEARCH 실패", e), e);
        }
    }

    /** 정확 일치로 한 건 조회 (code) */
    public StockDTO findByCode(String code) {
        String sql = "SELECT id, stock_code, stock_name, pbr, per, create_date FROM Stocks WHERE stock_code = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                List<StockDTO> list = mapList(rs);
                return list.isEmpty() ? null : list.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(detail("findByCode 실패", e), e);
        }
    }

    /** PK로 한 건 조회 (id) */
    public StockDTO findById(Long id) {
        String sql = "SELECT id, stock_code, stock_name, pbr, per, create_date FROM Stocks WHERE id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<StockDTO> list = mapList(rs);
                return list.isEmpty() ? null : list.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(detail("findById 실패", e), e);
        }
    }

    public void insert(StockDTO d) {
        String sql = "INSERT INTO Stocks (stock_code, stock_name, create_date, pbr, per) " +
                "VALUES (?, ?, datetime('now','localtime'), ?, ?)";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getStockCode());
            ps.setString(2, d.getStockName());
            if (d.getPbr() == null) ps.setNull(3, Types.REAL); else ps.setDouble(3, d.getPbr());
            if (d.getPer()  == null) ps.setNull(4, Types.REAL); else ps.setDouble(4, d.getPer());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(detail("INSERT 실패(중복 코드 가능)", e), e);
        }
    }

    /** PK로 업데이트: stock_code까지 함께 변경 가능 */
    public boolean updateById(Long id, String code, String name, Double pbr, Double per) {
        String sql = "UPDATE Stocks SET stock_code=?, stock_name=?, pbr=?, per=? WHERE id=?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            if (pbr == null) ps.setNull(3, Types.REAL); else ps.setDouble(3, pbr);
            if (per == null) ps.setNull(4, Types.REAL); else ps.setDouble(4, per);
            ps.setLong(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(detail("updateById 실패", e), e);
        }
    }

    /** (참고) 기존 코드 기준 업데이트도 유지하고 싶다면 남겨둠 */
    public boolean update(String code, String name, Double pbr, Double per) {
        String sql = "UPDATE Stocks SET stock_name=?, pbr=?, per=? WHERE stock_code=?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            if (pbr == null) ps.setNull(2, Types.REAL); else ps.setDouble(2, pbr);
            if (per == null) ps.setNull(3, Types.REAL); else ps.setDouble(3, per);
            ps.setString(4, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(detail("UPDATE 실패", e), e);
        }
    }

    public boolean delete(String code) {
        String sql = "DELETE FROM Stocks WHERE stock_code=?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(detail("DELETE 실패", e), e);
        }
    }

    private List<StockDTO> mapList(ResultSet rs) throws SQLException {
        List<StockDTO> out = new ArrayList<>();
        while (rs.next()) {
            StockDTO d = new StockDTO(
                    rs.getLong("id"),
                    rs.getString("stock_code"),
                    rs.getString("stock_name"),
                    rs.getObject("pbr") == null ? null : rs.getDouble("pbr"),
                    rs.getObject("per") == null ? null : rs.getDouble("per"),
                    parseDate(rs.getString("create_date"))
            );
            out.add(d);
        }
        return out;
    }

    private static LocalDateTime parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String s = raw.trim();
        try {
            if (s.contains(" ")) {
                int dot = s.indexOf('.');
                if (dot > 0) {
                    String head = s.substring(0, dot);
                    String frac = s.substring(dot + 1).replaceAll("[^0-9]", "");
                    if (frac.length() > 9) frac = frac.substring(0, 9);
                    String padded = String.format("%-9s", frac).replace(' ', '0');
                    return LocalDateTime.parse(head.replace(' ', 'T') + "." + padded,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"));
                } else {
                    return LocalDateTime.parse(s.replace(' ', 'T'),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            }
            return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignore) {
            try {
                return LocalDateTime.parse(
                        s.replace(' ', 'T').substring(0, Math.min(s.length(), 19)),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                );
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private static String detail(String msg, SQLException e) {
        return msg + " [SQLState=" + e.getSQLState() + ", ErrorCode=" + e.getErrorCode() + ", Message=" + e.getMessage() + "]";
    }
}
