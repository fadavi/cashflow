package cashflow;

import java.util.List;
import java.sql.*;

public final class Account {
    private final int id;
    private String name;
    private String desc;

    private Account(int id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object that) {
        return that != null
                && that instanceof Account
                && this.id == ((Account) that).id;
    }

    @Override
    public int hashCode() {
        return id + 335;
    }
    
    /***********************
     * DATABASE OPERATIONS *
     ***********************/
    
    public void update(String newName, String newDesc) {
        DB.prepare("UPDATE account SET name = ?, desc = ? WHERE id = ?", ps -> {
            ps.setString(1, newName);
            ps.setString(2, newDesc);
            ps.setInt(3, id);
            ps.execute();

            name = newName;
            desc = newDesc;

            return null;
        });

        Program.getMainFrame().refreshStats();
    }

    public boolean hasCashFlow() {
        return DB.prepare("SELECT COUNT(*) AS 'count' FROM cashflow WHERE creditor = ? OR debtor = ?", ps -> {
            ps.setInt(1, id);
            ps.setInt(2, id);

            try(ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("count") != 0;
            }
        });
    }

    public int getCredit() {
        return DB.prepare("SELECT TOTAL(amount) AS 'credit' FROM cashflow WHERE creditor = ?", ps -> {
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("credit");
            }
        });
    }

    public int getDebt() {
        return DB.prepare("SELECT TOTAL(amount) AS 'debt' FROM cashflow WHERE debtor = ?", ps -> {
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("debt");
            }
        });
    }

    public static Account byID(int id) {
        return DB.prepare("SELECT name, desc FROM account WHERE id = ?", ps -> {
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()) {
                rs.next();

                return new Account(id, rs.getString("name"), rs.getString("desc"));
            }
        });
    }

    public static boolean isUnique(String name) {
        return DB.prepare("SELECT count(*) AS 'count' FROM account WHERE name = ?", ps -> {
            ps.setString(1, name);
            try(ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("count") == 0;
            }
        });
    }

    public static Account insert(String name, String desc) {
        return DB.prepare("INSERT INTO account(name, desc) VALUES(?, ?)", ps -> {
            ps.setString(1, name);
            ps.setString(2, desc);
            ps.execute();

            Program.getMainFrame().refreshStats();
            return new Account(DB.lastInsertID(), name, desc);
        });
    }

    public static List<Account> all() {
        return DB.fetch("SELECT * FROM account ORDER BY name", rs -> new Account(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("desc")
                ));
    }

    public static void delete(List<Account> accounts) {
        DB.prepare("DELETE FROM account WHERE id = ?", ps -> {
            for(Account acc : accounts) {
                ps.setInt(1, acc.getID());
                ps.addBatch();
            }
            ps.executeBatch();
            return null;
        });

        Program.getMainFrame().refreshStats();
    }
}
