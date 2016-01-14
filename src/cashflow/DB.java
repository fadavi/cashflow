package cashflow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class DB {
    private DB() {}
    
    public static interface UnsafeFunction<T, R> {
        R apply(T arg) throws SQLException;
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:cashflow.db");
    }

    public static void initDB() {
        try(Connection con = connect();
            Statement stt = con.createStatement()) {
            stt.execute(
                    "CREATE TABLE IF NOT EXISTS account("
                    + "id integer primary key autoincrement,"
                    + "name text unique not null,"
                    + "desc text)"
            );

            stt.execute(
                    "CREATE TABLE IF NOT EXISTS cashflow("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "date TEXT NOT NULL,"
                    + "creditor INTEGER NOT NULL,"
                    + "debtor INTEGER NOT NULL,"
                    + "amount INTEGER NOT NULL,"
                    + "desc TEXT,"
                    + "FOREIGN KEY(creditor) REFERENCES account(id),"
                    + "FOREIGN KEY(debtor) REFERENCES account(id))"
            );
        } catch(SQLException sqlex) {
            Program.fatal(sqlex);
        }
    }

    public static <R> R connect(UnsafeFunction<Connection, R> fn) {
        try(Connection con = connect()) {
            return fn.apply(con);
        } catch(SQLException sqlex) {
            Program.fatal(sqlex);
        }

        return null;
    }

    public static <R> R prepare(String sql, UnsafeFunction<PreparedStatement, R> fn) {
        return connect(con -> fn.apply(con.prepareStatement(sql)));
    }

    public static <R> R execute(String sql, UnsafeFunction<ResultSet, R> fn) {
        return connect(con -> {
            try(Statement stt = con.createStatement();
                ResultSet rs = stt.executeQuery(sql)) {
                return fn.apply(rs);
            }
        });
    }

    public static <R> List<R> fetch(String sql, UnsafeFunction<ResultSet, R> fn) {
        return execute(sql, rs -> {
            List<R> list = new ArrayList();
            while(rs.next()) {
                list.add(fn.apply(rs));
            }
            return list;
        });
    }

    public static int lastInsertID() {
        return execute("SELECT LAST_INSERT_ROWID() AS 'id'", rs -> {
            rs.next();
            return rs.getInt("id");
        });
    }
    
    public static int accountsCount() {
        return execute("SELECT COUNT(*) AS 'count' FROM account", rs -> {
            rs.next();
            return rs.getInt("count");
        });
    }
    
    public static int cashflowsCount() {
        return execute("SELECT COUNT(*) AS 'count' FROM cashflow", rs -> {
            rs.next();
            return rs.getInt("count");
        });
    }
    
    public static int totalCredits() {
        return execute("SELECT TOTAL(amount) AS 'total' FROM cashflow", rs -> {
            rs.next();
            return rs.getInt("total");
        });
    }
}
