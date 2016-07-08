import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by parthpendurkar on 7/8/16.
 */

public class SearchPanel extends JPanel implements ActionListener {
    private JTextField textField;
    private JTextArea textArea;
    private SearchHandler searchHandler;

    public SearchPanel(SearchHandler searchHandler) {
        super(new GridBagLayout());
        this.searchHandler = searchHandler;
        textField = new JTextField(40);
        textField.addActionListener(this);

        textArea = new JTextArea(5, 40);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(textField, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(scrollPane, c);
    }

    public void actionPerformed(ActionEvent evt) {
        String text = textField.getText();
        textArea.append(text + "\n");
        textField.selectAll();
        searchHandler.printText(text);
        searchHandler.findNode(text);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
