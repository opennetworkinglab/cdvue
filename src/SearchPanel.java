/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class that contains methods and variables to generate a "Search Panel" window with a text field.
 *
 * @author Parth Pendurkar
 * @version 1.0
 */
public class SearchPanel extends JPanel implements ActionListener {

    private JTextField textField;
    private JTextArea textArea;
    private SearchHandler searchHandler;

    /**
     * Constructor for objects of class SearchPanel.
     *
     * @param searchHandler     the SearchHandler object that this panel links to
     */
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

    /**
     * Listens for user input.
     *
     * @param evt
     */
    public void actionPerformed(ActionEvent evt) {
        String text = textField.getText();
        textArea.append(text + "\n");
        textField.selectAll();
        searchHandler.printText(text);
        searchHandler.findNode(text);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
