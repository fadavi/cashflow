/*
 * .-github--.   .-----website-----.
 * |         |   |                 |
 * F A D A V I @ F A D A V I . N E T
 * |           |           |       |
 * |           '-telegram--'       |
 * '---email-----------------------'
 */

package cashflow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.table.AbstractTableModel;
import java.util.Calendar;
import java.util.List;
import java.util.stream.IntStream;

import static cashflow.Program.button;
import static java.util.stream.Collectors.toList;

public class CashFlowsDialog extends JDialog {
    private final CashFlowsDialogModel tblModel = new CashFlowsDialogModel();
    private final JTable tbl = new JTable(tblModel);

    public CashFlowsDialog(Window parent) {
        super(parent, "گردش‌های نقدی", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        add(new JScrollPane(tbl), BorderLayout.CENTER);

        JToolBar tb = new JToolBar("Buttons");
        tb.setFloatable(false);

        tb.add(button("افزودن", this::onAdd));
        tb.add(button("ویرایش", this::onEdit));
        tb.add(button("حذف", this::onDelete));
        tb.add(button("بستن", e -> setVisible(false)));

        add(tb, BorderLayout.SOUTH);

        pack();
    }

    private void onAdd(ActionEvent e) {
        new CashFlowEditor(this) {
            {
                Calendar cal = Calendar.getInstance();
                txtDate.setText(String.format(
                    "%d/%d/%d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DATE)
                ));
            }

            @Override
            protected void onOK(ActionEvent e) {
                String date = txtDate.getText();
                Account creditor = abxCreditor.getSelectedAccount();
                Account debtor = abxDebtor.getSelectedAccount();
                int amount = Integer.parseInt(txtAmount.getText());
                String desc = txtDesc.getText();

                CashFlow.insert(
                        date, creditor.getID(), debtor.getID(), amount, desc
                );

                tblModel.refresh();
                setVisible(false);
            }
        }.setVisible(true);
    }

    private void onEdit(ActionEvent e) {
        if(tbl.getSelectedRowCount() != 1) {
            return;
        }

        CashFlow cf = tblModel.getRow(tbl.getSelectedRow());

        new CashFlowEditor(this) {
            {
                txtDate.setText(cf.getDate());
                abxCreditor.setSelectedItem(cf.getCreditor());
                abxDebtor.setSelectedItem(cf.getDebtor());
                txtAmount.setText(Integer.toString(cf.getAmount()));
                txtDesc.setText(cf.getDescription());
            }

            @Override
            protected void onOK(ActionEvent e) {
                String date = txtDate.getText();
                Account creditor = abxCreditor.getSelectedAccount();
                Account debtor = abxDebtor.getSelectedAccount();
                int amount = Integer.parseInt(txtAmount.getText());
                String desc = txtDesc.getText();

                cf.update(date, creditor.getID(), debtor.getID(), amount, desc);
                tblModel.refresh();
                setVisible(false);
            }
        }.setVisible(true);
    }

    private void onDelete(ActionEvent e) {
        int[] cashflowRows = tbl.getSelectedRows();

        if(cashflowRows.length == 0) {
            return;
        }

        int option = JOptionPane.showConfirmDialog(
            this,
            "آیا از تصمیم خود مطمئنید؟ حذف کردن گردش‌های نقدی غیرقابل بازگشت خواهد بود.",
            "حذف گردش نقدی",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if(option == JOptionPane.YES_OPTION) {
            List<CashFlow> cashflows = IntStream.of(cashflowRows)
                                                .mapToObj(tblModel::getRow)
                                                .collect(toList());

            CashFlow.delete(cashflows);
            tblModel.refresh();
        }
    }
}

class CashFlowsDialogModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {
        "تاریخ", "بستانکار", "بدهکار", "مبلغ", "شرح"
    };

    private static final Class<?>[] COLUMN_CLASSES = {
        String.class, Account.class, Account.class, Integer.class, String.class
    };

    private List<CashFlow> data = CashFlow.all();

    public void refresh() {
        data = CashFlow.all();
        fireTableDataChanged();
    }

    public CashFlow getRow(int rowIndex) {
        return data.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CashFlow cf = getRow(rowIndex);
        switch(columnIndex) {
            case 0:
                return cf.getDate();
            case 1:
                return cf.getCreditor();
            case 2:
                return cf.getDebtor();
            case 3:
                return cf.getAmount();
            default:
                return cf.getDescription();
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_CLASSES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}

abstract class CashFlowEditor extends JDialog {
    protected final JTextField txtDate = new JTextField(20);
    protected final AccountBox abxCreditor = new AccountBox();
    protected final AccountBox abxDebtor = new AccountBox();
    protected final JTextField txtAmount = new JTextField(20);
    protected final JTextArea txtDesc = new JTextArea(3, 20);

    public CashFlowEditor(Window parent) {
        super(parent, "ویرایش", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        JPanel pnlCenter = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.anchor = GridBagConstraints.BASELINE_LEADING;
        
        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1;
        gbcField.weightx = 1;
        gbcField.fill = GridBagConstraints.BOTH;
        
        gbcField.gridy = gbcLabel.gridy = 0;
        pnlCenter.add(new JLabel("تاریخ:"), gbcLabel);
        pnlCenter.add(txtDate, gbcField);

        gbcField.gridy = gbcLabel.gridy = 1;
        pnlCenter.add(new JLabel("بستانکار:"), gbcLabel);
        pnlCenter.add(abxCreditor, gbcField);

        gbcField.gridy = gbcLabel.gridy = 2;
        pnlCenter.add(new JLabel("بدهکار:"), gbcLabel);
        pnlCenter.add(abxDebtor, gbcField);

        gbcField.gridy = gbcLabel.gridy = 3;
        pnlCenter.add(new JLabel("مبلغ - ریال:"), gbcLabel);
        pnlCenter.add(txtAmount, gbcField);

        gbcField.gridy = gbcLabel.gridy = 4;
        gbcField.weighty = 1;
        pnlCenter.add(new JLabel("شرح:"), gbcLabel);
        pnlCenter.add(new JScrollPane(txtDesc), gbcField);

        add(pnlCenter, BorderLayout.CENTER);

        JToolBar tb = new JToolBar("Buttons");
        tb.setFloatable(false);

        tb.add(button("انصراف", e -> setVisible(false)));
        tb.add(button("تأیید", e -> {
            Account creditor = abxCreditor.getSelectedAccount();
            Account debtor = abxDebtor.getSelectedAccount();

            if(!Program.isValidDate(txtDate.getText())) {
                txtDate.requestFocus();
                txtDate.selectAll();
            } else if(creditor == null) {
                abxCreditor.requestFocus();
            } else if(debtor == null) {
                abxDebtor.requestFocus();
            } else if(creditor.equals(debtor)) {
                abxCreditor.requestFocus();
            } else if(!Program.isValidAmount(txtAmount.getText())) {
                txtAmount.requestFocus();
                txtAmount.selectAll();
            } else {
                onOK(e);
            }
        }));

        add(tb, BorderLayout.SOUTH);

        pack();
    }

    protected abstract void onOK(ActionEvent e);
}
