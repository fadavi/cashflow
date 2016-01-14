package cashflow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import static cashflow.Program.button;

public final class AccountsDialog extends JDialog {
    private final AccountsDialogListModel listModel = new AccountsDialogListModel();
    private final JList<Account> list = new JList(listModel);

    public AccountsDialog(Window parent) {
        super(parent, "مشتریان", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        add(new JScrollPane(list), BorderLayout.CENTER);

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
        new AccountEditor(this) {
            @Override
            protected void onOK(ActionEvent e) {
                String name = txtName.getText();
                String desc = txtDesc.getText();

                if(name.isEmpty() || !Account.isUnique(name)) {
                    txtName.requestFocus();
                    txtName.selectAll();
                } else {
                    Account.insert(name, desc);
                    setVisible(false);
                    listModel.refresh();
                }
            }
        }.setVisible(true);
    }

    private void onEdit(ActionEvent e) {
        if(list.getSelectedValuesList().size() != 1) {
            return;
        }
        Account acc = list.getSelectedValue();

        new AccountEditor(this) {
            {
                txtName.setText(acc.getName());
                txtDesc.setText(acc.getDescription());
            }

            @Override
            protected void onOK(ActionEvent e) {
                String newName = txtName.getText();
                String newDesc = txtDesc.getText();

                if(newName.isEmpty() || !Account.isUnique(newName)) {
                    txtName.requestFocus();
                    txtName.selectAll();
                }

                if(!acc.getName().equals(newName)
                        || !acc.getDescription().equals(newDesc)) {
                    acc.update(newName, newDesc);
                    listModel.refresh();
                }

                setVisible(false);
            }
        }.setVisible(true);
    }

    private void onDelete(ActionEvent e) {
        List<Account> accounts = list.getSelectedValuesList();
        if(accounts.isEmpty()) {
            return;
        }

        String message = "آیا از تصمیم خود مطمئنید؟ حذف کردن مشتریان غیرقابل بازگشت خواهد بود.\n"
                + "توجه داشته باشید که تنها مشتریانی قابل حذف هستند که هیچ گردش نقدی ثبت‌شده‌ای نداشته باشند.";

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "حذف مشتری",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if(option == JOptionPane.YES_OPTION) {
            for(Account acc : accounts) {
                if(acc.hasCashFlow()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "خطا! یکی از مشتریان انتخاب شده دارای گردش نقدی است.",
                            "خطا!",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Account.delete(accounts);
            listModel.refresh();
        }
    }
}

final class AccountsDialogListModel extends AbstractListModel<Account> {
    private List<Account> data = Account.all();

    public void refresh() {
        int oldSize = data.size();
        data = Account.all();

        fireContentsChanged(this, 0, Math.max(oldSize, data.size()));
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public Account getElementAt(int index) {
        return data.get(index);
    }
}

abstract class AccountEditor extends JDialog {
    protected final JTextField txtName = new JTextField(20);
    protected final JTextArea txtDesc = new JTextArea(3, 20);

    public AccountEditor(Window parent) {
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
        
        gbcLabel.gridy = gbcField.gridy = 0;
        pnlCenter.add(new JLabel("نام:"), gbcLabel);
        pnlCenter.add(txtName, gbcField);

        gbcLabel.gridy = gbcField.gridy = 1;
        gbcField.weighty = 1;
        pnlCenter.add(new JLabel("شرح:"), gbcLabel);
        pnlCenter.add(new JScrollPane(txtDesc), gbcField);

        add(pnlCenter, BorderLayout.CENTER);

        JToolBar tb = new JToolBar("Buttons");
        tb.setFloatable(false);

        tb.add(button("انصراف", e -> setVisible(false)));
        tb.add(button("تأیید", this::onOK));

        add(tb, BorderLayout.SOUTH);

        pack();
    }

    protected abstract void onOK(ActionEvent e);
}
