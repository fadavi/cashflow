/*
 * .-github--.   .-----website-----.
 * |         |   |                 |
 * F A D A V I @ F A D A V I . N E T
 * |           |           |       |
 * |           '-telegram--'       |
 * '---email-----------------------'
 */

package cashflow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import static cashflow.Program.button;

public final class MainFrame extends JFrame {
    private final JTextArea txtStats = new JTextArea(4, 30);
    private final JTextArea txtReport = new JTextArea(4, 30);
    
    public MainFrame() {
        super("دریافت و پرداخت");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setMinimumSize(new Dimension(300, 300));
        setLayout(new BorderLayout());

        initMenuBar();
        initToolBar();
        initCenter();
        initSouth();
        
        pack();
        Program.moveToCenter(this);
        
        refreshStats();
    }

    private void initToolBar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        tb.add(button("مشتریان", this::onAccounts));
        tb.add(button("گردش‌های نقدی", this::onCashFlows));
        tb.add(button("دربارهٔ برنامه", this::onAbout));

        add(tb, BorderLayout.NORTH);
    }

    private void initMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu mnuFile = new JMenu("سرفصل");
        mnuFile.add(menuItem("خروج", this::onExit, null));
        mb.add(mnuFile);

        JMenu mnuTools = new JMenu("ابزار");
        mnuTools.add(menuItem("مشتریان", this::onAccounts, null));
        mnuTools.add(menuItem("گردش‌های نقدی", this::onCashFlows, null));
        mb.add(mnuTools);

        JMenu mnuHelp = new JMenu("راهنما");
        mnuHelp.add(menuItem("دریارهٔ برنامه", this::onAbout, null));
        mb.add(mnuHelp);

        setJMenuBar(mb);
    }

    private void initCenter() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createTitledBorder("آمار"));
        txtStats.setEditable(false);
        pnl.add(new JScrollPane(txtStats));
        add(pnl, BorderLayout.CENTER);
    }
    
    private void initSouth() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createTitledBorder("گزارش وضعیت"));
        
        AccountBox abxReport = new AccountBox();
        abxReport.addItemListener(this::onReport);
        pnl.add(abxReport, BorderLayout.NORTH);
        
        txtReport.setEditable(false);
        pnl.add(new JScrollPane(txtReport), BorderLayout.CENTER);
        
        add(pnl, BorderLayout.SOUTH);
    }

    private void onAccounts(ActionEvent e) {
        new AccountsDialog(this).setVisible(true);
    }

    private void onCashFlows(ActionEvent e) {
        new CashFlowsDialog(this).setVisible(true);
    }

    private void onAbout(ActionEvent e) {
        JOptionPane.showMessageDialog(
                this, ":)", "دربارهٔ برنامه", JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void onExit(ActionEvent e) {
        int option = JOptionPane.showConfirmDialog(
                this,
                "آیا مطمئنید؟",
                "خروج",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if(option == JOptionPane.YES_OPTION) {
            setVisible(false);
        }
    }
    
    private void onReport(ItemEvent e) {
        final String NL = "\n";
        
        if(e.getStateChange() == ItemEvent.SELECTED) {
            Account acc = (Account)e.getItem();
            int credit = acc.getCredit();
            int debt = acc.getDebt();
            
            String report = "";
            report += "مجموع بستانکاری: " + credit + " ریال";
            report += NL + "مجموع بدهکاری: " + debt + " ریال";
            report += NL + NL + "وضعیت: ";
            
            if(credit > debt) {
                report += (credit - debt) + " ریال بستانکار";
            } else if(credit < debt) {
                report += (debt - credit) + " ریال بدهکار";
            } else {
                report += "صفر";
            }
            
            txtReport.setText(report);
        }
    }
    
    public void refreshStats() {
        final String NL = "\n";
        String stats = "";
        
        stats += "تعداد مشتریان: " + DB.accountsCount() + " نفر";
        stats += NL + "تعداد گردش‌های نقدی ثبت‌شده: " + DB.cashflowsCount() + " گردش";
        stats += NL + NL + "مجموع گردش مالی: " + DB.totalCredits() + " ریال";
        
        txtStats.setText(stats);
    }

    private static JMenuItem menuItem(String text, ActionListener actionListener, ImageIcon icon) {
        JMenuItem mnui = new JMenuItem(text, icon);
        mnui.addActionListener(actionListener);
        return mnui;
    }
}
