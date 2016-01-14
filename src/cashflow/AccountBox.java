package cashflow;

import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public final class AccountBox extends JComboBox<Account> {
    public AccountBox() {
        super(new AccountBoxModel(null));
    }
    
    public Account getSelectedAccount() {
        return (Account)getSelectedItem();
    }
}

final class AccountBoxModel extends AbstractListModel<Account> implements ComboBoxModel<Account> {
    private final List<Account> data = Account.all();
    private Account selectedItem;
    
    public AccountBoxModel(Account selectedItem) {
        this.selectedItem = selectedItem;
    }
    
    @Override
    public void setSelectedItem(Object anItem) {
        selectedItem = (Account)anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
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