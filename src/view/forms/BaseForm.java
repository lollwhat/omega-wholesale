package view.forms;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import view.UITheme;

public abstract class BaseForm extends JDialog {
    protected final Color darkBlue = UITheme.DARK_BLUE;
    protected final Color mediumBlue = UITheme.MEDIUM_BLUE;
    protected final Color lightBlue = UITheme.LIGHT_BLUE;
    protected final Color highlightBlue = UITheme.HIGHLIGHT_BLUE;
    protected final Color textWhite = UITheme.TEXT_WHITE;
    protected final Color errorRed = UITheme.ERROR_RED;

    protected JFrame parentFrame;

    protected Map<Component, JLabel> errorLabelMap = new HashMap<>();

    public BaseForm(JFrame parent, String title, int height) {
        super(parent, title, true);
        this.parentFrame = parent;
        initializeUI(height);
    }

    protected void initializeUI(int height) {
        setSize(450, height);
        setLocationRelativeTo(parentFrame);
        getContentPane().setBackground(mediumBlue);
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        JPanel formPanel = createFormPanel();
        JPanel buttonsPanel = createButtonsPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    protected JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mediumBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel(getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(highlightBlue);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        return headerPanel;
    }

    protected JPanel createFormFieldPanel(String labelText, String placeholder) {
        JPanel fieldContainerPanel = new JPanel(new BorderLayout(0, 2));
        fieldContainerPanel.setBackground(mediumBlue);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(textWhite);

        JTextField textField = new JTextField();
        textField.setBackground(darkBlue);
        textField.setForeground(textWhite);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setCaretColor(textWhite);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lightBlue),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));

        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(textWhite);
                }
                clearFieldError(textField);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        errorLabel.setForeground(errorRed);
        errorLabel.setVisible(false);
        errorLabelMap.put(textField, errorLabel);

        JPanel errorWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        errorWrapperPanel.setBackground(mediumBlue);
        errorWrapperPanel.add(errorLabel);

        fieldContainerPanel.add(label, BorderLayout.NORTH);
        fieldContainerPanel.add(textField, BorderLayout.CENTER);
        fieldContainerPanel.add(errorWrapperPanel, BorderLayout.SOUTH);

        return fieldContainerPanel;
    }

    protected JPanel createFormTextAreaPanel(String labelText, String placeholder, int rows) {
        JPanel areaContainerPanel = new JPanel(new BorderLayout(0, 2));
        areaContainerPanel.setBackground(mediumBlue);
        areaContainerPanel.setName("textAreaContainer_" + labelText.replaceAll("\\s+", ""));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(textWhite);

        JTextArea textArea = new JTextArea(rows, 20);
        textArea.setBackground(darkBlue);
        textArea.setForeground(textWhite);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setCaretColor(textWhite);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lightBlue),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        textArea.setName("textArea_" + labelText.replaceAll("\\s+", ""));

        textArea.setText(placeholder);
        textArea.setForeground(Color.GRAY);
        textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textArea.getText().equals(placeholder)) {
                    textArea.setText("");
                    textArea.setForeground(textWhite);
                }
                clearFieldError(textArea);
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textArea.getText().isEmpty()) {
                    textArea.setText(placeholder);
                    textArea.setForeground(Color.GRAY);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(200, textArea.getPreferredSize().height + (rows > 1 ? 10 : 0) ));

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        errorLabel.setForeground(errorRed);
        errorLabel.setVisible(false);
        errorLabelMap.put(textArea, errorLabel);

        JPanel errorWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        errorWrapperPanel.setBackground(mediumBlue);
        errorWrapperPanel.add(errorLabel);

        areaContainerPanel.add(label, BorderLayout.NORTH);
        areaContainerPanel.add(scrollPane, BorderLayout.CENTER);
        areaContainerPanel.add(errorWrapperPanel, BorderLayout.SOUTH);

        return areaContainerPanel;
    }

    protected JPanel createPhoneFieldWithCountryCodePanel(String labelText, String[] countryCodes, String defaultCode, String phonePlaceholder) {
        JPanel phoneContainerPanel = new JPanel(new BorderLayout(0, 2));
        phoneContainerPanel.setBackground(mediumBlue);
        phoneContainerPanel.setName("phoneContainer_" + labelText.replaceAll("\\s+", ""));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(textWhite);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(mediumBlue);

        JComboBox<String> countryCodeComboBox = new JComboBox<>(countryCodes);
        countryCodeComboBox.setSelectedItem(defaultCode);
        countryCodeComboBox.setBackground(darkBlue);
        countryCodeComboBox.setForeground(textWhite);
        countryCodeComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        countryCodeComboBox.setPreferredSize(new Dimension(120, countryCodeComboBox.getPreferredSize().height));
        countryCodeComboBox.setMaximumSize(new Dimension(130, countryCodeComboBox.getPreferredSize().height + 10));


        JTextField phoneField = new JTextField();
        phoneField.setBackground(darkBlue);
        phoneField.setForeground(textWhite);
        phoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        phoneField.setCaretColor(textWhite);
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lightBlue),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        phoneField.setName("phoneField_" + labelText.replaceAll("\\s+", ""));

        ((AbstractDocument) phoneField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if (string.matches("[0-9]+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                if (text.matches("[0-9]+")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        phoneField.setText(phonePlaceholder);
        phoneField.setForeground(Color.GRAY);
        phoneField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (phoneField.getText().equals(phonePlaceholder)) {
                    phoneField.setText("");
                    phoneField.setForeground(textWhite);
                }
                clearFieldError(phoneField);
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (phoneField.getText().isEmpty()) {
                    phoneField.setText(phonePlaceholder);
                    phoneField.setForeground(Color.GRAY);
                }
            }
        });

        inputPanel.add(countryCodeComboBox, BorderLayout.WEST);
        inputPanel.add(phoneField, BorderLayout.CENTER);

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        errorLabel.setForeground(errorRed);
        errorLabel.setVisible(false);
        errorLabelMap.put(phoneField, errorLabel);

        JPanel errorWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        errorWrapperPanel.setBackground(mediumBlue);
        errorWrapperPanel.add(errorLabel);

        phoneContainerPanel.add(label, BorderLayout.NORTH);
        phoneContainerPanel.add(inputPanel, BorderLayout.CENTER);
        phoneContainerPanel.add(errorWrapperPanel, BorderLayout.SOUTH);

        return phoneContainerPanel;
    }

    protected JPanel createComboBoxPanel(String labelText, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setBackground(mediumBlue);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(textWhite);

        comboBox.setBackground(darkBlue);
        comboBox.setForeground(textWhite);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setPreferredSize(new Dimension(200, 30));
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));


        panel.add(label, BorderLayout.NORTH);
        panel.add(comboBox, BorderLayout.CENTER);


        return panel;
    }

    protected JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(textWhite);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));
        return button;
    }

    protected JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonsPanel.setBackground(mediumBlue);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton saveButton = createButton("Save", highlightBlue);
        JButton cancelButton = createButton("Cancel", lightBlue);

        saveButton.addActionListener(_ -> saveAction());
        cancelButton.addActionListener(_ -> dispose());

        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        return buttonsPanel;
    }

    protected void setFieldValue(JTextField field, String value, String placeholder) {
        if (value != null && !value.trim().isEmpty() && !value.equalsIgnoreCase("null")) {
            field.setText(value.trim());
            field.setForeground(textWhite);
        } else {
            field.setText(placeholder);
            field.setForeground(Color.GRAY);
        }
        clearFieldError(field);
    }

    protected void setTextAreaValue(JTextArea area, String value, String placeholder) {
        if (value != null && !value.trim().isEmpty() && !value.equalsIgnoreCase("null")) {
            area.setText(value.trim());
            area.setForeground(textWhite);
        } else {
            area.setText(placeholder);
            area.setForeground(Color.GRAY);
        }
        clearFieldError(area);
    }


    protected void showFieldError(Component fieldComponent, String message) {
        JLabel errorLabel = errorLabelMap.get(fieldComponent);
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
        if (fieldComponent instanceof JComponent) {
            ((JComponent) fieldComponent).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(errorRed, 1),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        }
        if (fieldComponent.getParent() != null) {
            fieldComponent.getParent().revalidate();
            fieldComponent.getParent().repaint();
        }
    }

    protected void clearFieldError(Component fieldComponent) {
        JLabel errorLabel = errorLabelMap.get(fieldComponent);
        if (errorLabel != null) {
            errorLabel.setText(" ");
            errorLabel.setVisible(false);
        }
        if (fieldComponent instanceof JComponent) {
            ((JComponent) fieldComponent).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(lightBlue),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        }
        if (fieldComponent.getParent() != null) {
            fieldComponent.getParent().revalidate();
            fieldComponent.getParent().repaint();
        }
    }

    protected void clearAllFieldErrors() {
        for (Map.Entry<Component, JLabel> entry : errorLabelMap.entrySet()) {
            Component fieldComponent = entry.getKey();
            JLabel errorLabel = entry.getValue();
            if (errorLabel != null) {
                errorLabel.setText(" ");
                errorLabel.setVisible(false);
            }
            if (fieldComponent instanceof JComponent) {
                ((JComponent) fieldComponent).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(lightBlue),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        }
    }


    protected void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    protected void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Warning",
                JOptionPane.WARNING_MESSAGE
        );
    }


    protected void showSuccess(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    protected String getTextFieldValue(JTextField field, String placeholder) {
        String value = field.getText().trim();
        if (value.equals(placeholder) || value.isEmpty()) {
            return null;
        }
        return value;
    }

    protected String getTextAreaValue(JTextArea area, String placeholder) {
        String value = area.getText().trim();
        return (value.equals(placeholder) || value.isEmpty()) ? null : value;
    }

    protected boolean validateField(JTextField field, String placeholder, String fieldName) {
        String value = getTextFieldValue(field, placeholder);
        if (value == null) {
            showFieldError(field, fieldName + " cannot be empty.");
            return false;
        }
        clearFieldError(field);
        return true;
    }

    protected boolean validateTextArea(JTextArea area, String placeholder, String fieldName, boolean isMandatory) {
        String value = getTextAreaValue(area, placeholder);
        if (isMandatory && value == null) {
            showFieldError(area, fieldName + " cannot be empty.");
            return false;
        }
        return true;
    }

    protected boolean validateNumericField(JTextField field, String placeholder, String fieldName) {
        if (!validateField(field, placeholder, fieldName)) {
            return false;
        }
        try {
            Double.parseDouble(field.getText().trim());
            clearFieldError(field);
            return true;
        } catch (NumberFormatException e) {
            showFieldError(field, fieldName + " must be a valid number.");
            return false;
        }
    }

    protected abstract JPanel createFormPanel();

    protected abstract void saveAction();
}