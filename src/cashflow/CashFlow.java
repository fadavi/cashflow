/*
 * .-github--.   .-----website-----.
 * |         |   |                 |
 * F A D A V I @ F A D A V I . N E T
 * |           |           |       |
 * |           '-telegram--'       |
 * '---email-----------------------'
 */

package cashflow;

import java.util.List;

public final class CashFlow {
    private final int id;
    private String date;
    private int creditorID;
    private int debtorID;
    private int amount;
    private String desc;

    private CashFlow(int id, String date, int creditorID, int debtorID, int amount, String desc) {
        this.id = id;
        this.date = date;
        this.creditorID = creditorID;
        this.debtorID = debtorID;
        this.amount = amount;
        this.desc = desc;
    }

    public int getID() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getCreditorID() {
        return creditorID;
    }

    public Account getCreditor() {
        return Account.byID(creditorID);
    }

    public int getDebtorID() {
        return debtorID;
    }

    public Account getDebtor() {
        return Account.byID(debtorID);
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() {
        return desc;
    }
    
    
    /*************************
     *  DATABASE OPERATIONS  *
     *************************/
    

    public void update(String newDate, int newCreditorID, int newDebtorID, int newAmount, String newDesc) {
        DB.prepare("UPDATE cashflow SET date = ?, creditor = ?,"
                + "debtor = ?, amount = ?, desc = ? WHERE id = ?", ps -> {
            ps.setString(1, date);
            ps.setInt(2, newCreditorID);
            ps.setInt(3, newDebtorID);
            ps.setInt(4, newAmount);
            ps.setString(5, newDesc);
            ps.setInt(6, id);
            ps.execute();

            date = newDate;
            creditorID = newCreditorID;
            debtorID = newDebtorID;
            amount = newAmount;
            desc = newDesc;

            return null;
        });

        Program.getMainFrame().refreshStats();
    }

    public static CashFlow insert(String date, int creditorID, int debtorID, int amount, String desc) {
        return DB.prepare("INSERT INTO cashflow(date, creditor, debtor, amount, desc)"
                + "VALUES(?, ?, ?, ?, ?)", ps -> {
            ps.setString(1, date);
            ps.setInt(2, creditorID);
            ps.setInt(3, debtorID);
            ps.setInt(4, amount);
            ps.setString(5, desc);
            ps.execute();

            Program.getMainFrame().refreshStats();
            return new CashFlow(DB.lastInsertID(), date, creditorID, debtorID, amount, desc);
        });
    }

    public static List<CashFlow> all() {
        return DB.fetch("SELECT * FROM cashflow ORDER BY date, creditor, debtor, amount", rs -> new CashFlow(
            rs.getInt("id"),
            rs.getString("date"),
            rs.getInt("creditor"),
            rs.getInt("debtor"),
            rs.getInt("amount"),
            rs.getString("desc")
        ));
    }

    public static void delete(List<CashFlow> cashflows) {
        DB.prepare("DELETE FROM cashflow WHERE id = ?", ps -> {
            for(CashFlow cf : cashflows) {
                ps.setInt(1, cf.getID());
                ps.addBatch();
            }

            ps.executeBatch();
            return null;
        });

        Program.getMainFrame().refreshStats();
    }
}
