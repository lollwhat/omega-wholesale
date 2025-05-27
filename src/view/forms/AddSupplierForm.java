package view.forms;

import controller.SupplierController;
import view.SupplierManagementView;
import view.UITheme;

import javax.swing.*;
import java.awt.*;

public class AddSupplierForm extends BaseForm {
    private JTextField supplierCompanyField;
    private JTextField supplierPICField;
    private JTextArea supplierDescriptionArea;
    private JTextArea supplierAddressArea;
    private JComboBox<String> countryCodeComboBox;
    private JTextField supplierPhoneField;
    private JTextField supplierEmailField;
    private JComboBox<String> supplierStatusComboBox;
    private JLabel generalErrorLabel;

    private String editSupplierId;

    private final String COMPANY_PLACEHOLDER = "Enter supplier company name";
    private final String PIC_PLACEHOLDER = "Enter Person-In-Charge name";
    private final String DESC_PLACEHOLDER = "Enter supplier description (optional)";
    private final String ADDRESS_PLACEHOLDER = "Enter supplier address (optional)";
    private final String PHONE_PLACEHOLDER = "Enter supplier phone number";
    private final String EMAIL_PLACEHOLDER = "Enter supplier email address";

    private static final String[] SUPPLIER_STATUSES = {"Pending", "Approved", "Suspended"};

    private static final String[] COUNTRY_CODES = {"+1 (USA/CAN)", "+44 (UK)", "+49 (DE)", "+60 (MY)", "+61 (AU)", "+65 (SG)", "+81 (JP)", "+86 (CN)", "+91 (IN)" /* ... more codes */};
    private static final String DEFAULT_COUNTRY_CODE = "+60 (MY)";


    public AddSupplierForm(JFrame parent) {
        super(parent, "Add New Supplier", 700);
        this.editSupplierId = null;
    }

    public AddSupplierForm(JFrame parent, String dbSupplierId) {
        super(parent, "Edit Supplier", 700);
        this.editSupplierId = dbSupplierId;

        if (this.editSupplierId != null) {
            SwingUtilities.invokeLater(this::loadEditData);
        }
    }

    private void loadEditData() {
        // System.out.println("Loading edit data for supplier ID: " + editSupplierId);
        if (editSupplierId != null) {
            loadSupplierData();
        }
    }


    @Override
    protected JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(mediumBlue);
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 20));

        // Supplier Company
        JPanel companyPanel = createFormFieldPanel("Supplier Company:", COMPANY_PLACEHOLDER);
        supplierCompanyField = findTextFieldInPanel(companyPanel);
        formPanel.add(companyPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Supplier PIC
        JPanel picPanel = createFormFieldPanel("Supplier PIC:", PIC_PLACEHOLDER);
        supplierPICField = findTextFieldInPanel(picPanel);
        formPanel.add(picPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Supplier Description (JTextArea)
        JPanel descPanel = createFormTextAreaPanel("Description:", DESC_PLACEHOLDER, 3); // 3 rows
        supplierDescriptionArea = findTextAreaInPanel(descPanel);
        formPanel.add(descPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Supplier Address (JTextArea)
        JPanel addressPanel = createFormTextAreaPanel("Address:", ADDRESS_PLACEHOLDER, 4); // 4 rows
        supplierAddressArea = findTextAreaInPanel(addressPanel);
        formPanel.add(addressPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Supplier Phone (Country Code + Number)
        JPanel phoneContainerPanel = createPhoneFieldWithCountryCodePanel("Phone:", COUNTRY_CODES, DEFAULT_COUNTRY_CODE, PHONE_PLACEHOLDER);
        countryCodeComboBox = findComboBoxInPanel(phoneContainerPanel);
        supplierPhoneField = findTextFieldInPanel(phoneContainerPanel);
        formPanel.add(phoneContainerPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Supplier Email
        JPanel emailPanel = createFormFieldPanel("Email:", EMAIL_PLACEHOLDER);
        supplierEmailField = findTextFieldInPanel(emailPanel);
        formPanel.add(emailPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Supplier Status ComboBox
        supplierStatusComboBox = new JComboBox<>(SUPPLIER_STATUSES);
        JPanel statusPanel = createComboBoxPanel("Status:", supplierStatusComboBox);
        formPanel.add(statusPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // General Error Label
        generalErrorLabel = new JLabel(" ");
        generalErrorLabel.setForeground(UITheme.ERROR_RED);
        generalErrorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        generalErrorLabel.setVisible(false);
        JPanel generalErrorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        generalErrorPanel.setBackground(mediumBlue);
        generalErrorPanel.add(generalErrorLabel);
        formPanel.add(generalErrorPanel);

        formPanel.add(Box.createVerticalGlue());

        return formPanel;
    }

    private JTextField findTextFieldInPanel(JPanel containerPanel) {
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout)containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JTextField) {
                return (JTextField) centerComponent;
            }
        }
        for (Component comp : containerPanel.getComponents()) {
            if (comp instanceof JTextField) return (JTextField) comp;
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JTextField) return (JTextField) subComp;
                }
            }
        }
        throw new RuntimeException("JTextField not found in panel structure. Review findTextFieldInPanel and BaseForm.createFormFieldPanel.");
    }

    private JTextArea findTextAreaInPanel(JPanel containerPanel) {
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout)containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JScrollPane) {
                Component view = ((JScrollPane) centerComponent).getViewport().getView();
                if (view instanceof JTextArea) return (JTextArea) view;
            } else if (centerComponent instanceof JTextArea) {
                return (JTextArea) centerComponent;
            }
        }
        for (Component comp : containerPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                Component view = ((JScrollPane) comp).getViewport().getView();
                if (view instanceof JTextArea) return (JTextArea) view;
            } else if (comp instanceof JTextArea) {
                return (JTextArea) comp;
            }
        }
        System.err.println("Warning: JTextArea not found as expected in panel: " + containerPanel.getName());
        return null;
    }

    private JComboBox<String> findComboBoxInPanel(JPanel containerPanel) {
        if (containerPanel.getLayout() instanceof BorderLayout) {
            Component centerComponent = ((BorderLayout)containerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JPanel) {
                for(Component subComp : ((JPanel)centerComponent).getComponents()){
                    if(subComp instanceof JComboBox) return (JComboBox<String>) subComp;
                }
            } else if (centerComponent instanceof JComboBox) {
                return (JComboBox<String>) centerComponent;
            }
        }
        for (Component comp : containerPanel.getComponents()) {
            if (comp instanceof JComboBox) return (JComboBox<String>) comp;
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JComboBox) return (JComboBox<String>) subComp;
                }
            }
        }
        System.err.println("Warning: JComboBox not found as expected in panel: " + containerPanel.getName());
        return null;
    }

    private void loadSupplierData() {
        if (supplierCompanyField == null || supplierAddressArea == null || supplierPhoneField == null) {
            System.err.println("AddSupplierForm.loadSupplierData() called before all fields are initialized.");
            showError("Form initialization error. Cannot load supplier data.");
            return;
        }
        try {
            SupplierController supplierController = new SupplierController();
            String supplierData = supplierController.getOneWithId(editSupplierId);
            if (supplierData != null) {
                String[] parts = supplierData.split(",");
                if (parts.length >= 8) {
                    setFieldValue(supplierCompanyField, parts[1], COMPANY_PLACEHOLDER);
                    setFieldValue(supplierPICField, parts[2], PIC_PLACEHOLDER);
                    setTextAreaValue(supplierDescriptionArea, parts[3], DESC_PLACEHOLDER);
                    setTextAreaValue(supplierAddressArea, parts[4], ADDRESS_PLACEHOLDER);

                    String fullPhoneNumber = parts[5];
                    boolean codeFound = false;
                    for (String codePrefixWithLabel : COUNTRY_CODES) {
                        String actualCode = codePrefixWithLabel.split(" ")[0];
                        if (fullPhoneNumber.startsWith(actualCode)) {
                            countryCodeComboBox.setSelectedItem(codePrefixWithLabel);
                            String numberPart = fullPhoneNumber.substring(actualCode.length()).trim();
                            setFieldValue(supplierPhoneField, numberPart, PHONE_PLACEHOLDER);
                            codeFound = true;
                            break;
                        }
                    }
                    if (!codeFound) {
                        countryCodeComboBox.setSelectedItem(DEFAULT_COUNTRY_CODE);
                        setFieldValue(supplierPhoneField, fullPhoneNumber, PHONE_PLACEHOLDER);
                    }

                    setFieldValue(supplierEmailField, parts[6], EMAIL_PLACEHOLDER);

                    String statusFromFile = parts[7];
                    supplierStatusComboBox.setSelectedItem(statusFromFile);
                    if (supplierStatusComboBox.getSelectedIndex() == -1) {
                        supplierStatusComboBox.setSelectedItem(SUPPLIER_STATUSES[0]);
                    }
                } else {
                    showError("Corrupted supplier data. ID: " + editSupplierId);
                }
            } else {
                showError("Could not load supplier data for ID: " + editSupplierId);
            }
        } catch (Exception e) {
            showError("Error loading supplier data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void saveAction() {
        clearAllFieldErrors();
        generalErrorLabel.setText(" ");
        generalErrorLabel.setVisible(false);

        boolean isValid = true;

        if (!validateField(supplierCompanyField, COMPANY_PLACEHOLDER, "Supplier Company")) isValid = false;
        if (!validateField(supplierPICField, PIC_PLACEHOLDER, "Supplier PIC")) isValid = false;
        if (!validateTextArea(supplierDescriptionArea, DESC_PLACEHOLDER, "Description", false)) isValid = false;
        if (!validateTextArea(supplierAddressArea, ADDRESS_PLACEHOLDER, "Address", false)) isValid = false;

        String phoneDigits = getTextFieldValue(supplierPhoneField, PHONE_PLACEHOLDER);
        if (phoneDigits == null || phoneDigits.isEmpty()) {
            showFieldError(supplierPhoneField, "Phone number cannot be empty.");
            isValid = false;
        } else if (!phoneDigits.matches("^[0-9]+$")) {
            showFieldError(supplierPhoneField, "Phone number must contain only digits.");
            isValid = false;
        }


        if (!validateField(supplierEmailField, EMAIL_PLACEHOLDER, "Supplier Email")) isValid = false;
        else {
            String email = getTextFieldValue(supplierEmailField, EMAIL_PLACEHOLDER);
            if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                showFieldError(supplierEmailField, "Invalid email format.");
                isValid = false;
            }
        }

        String selectedCountryCodeItem = (String) countryCodeComboBox.getSelectedItem();
        if (selectedCountryCodeItem == null) {
            generalErrorLabel.setText("Please select a country code.");
            generalErrorLabel.setVisible(true);
            isValid = false;
        }


        if (!isValid) {
            return;
        }

        String company = getTextFieldValue(supplierCompanyField, COMPANY_PLACEHOLDER);
        String pic = getTextFieldValue(supplierPICField, PIC_PLACEHOLDER);
        String description = getTextAreaValue(supplierDescriptionArea, DESC_PLACEHOLDER);
        String address = getTextAreaValue(supplierAddressArea, ADDRESS_PLACEHOLDER);

        String countryCode = selectedCountryCodeItem.split(" ")[0];
        String fullPhoneNumber = countryCode + phoneDigits.trim();

        String email = getTextFieldValue(supplierEmailField, EMAIL_PLACEHOLDER);
        String selectedStatus = ((String) supplierStatusComboBox.getSelectedItem() == "Approved") ? "1" : ((String) supplierStatusComboBox.getSelectedItem() == "Pending" ? "0" : "2");

        SupplierController supplierController = new SupplierController();

        try {
            if (editSupplierId == null) {
                supplierController.addSupplier(company, pic, description, address, fullPhoneNumber, email, selectedStatus);
                showSuccess("Supplier added successfully!");
            } else {
                supplierController.updateSupplier(editSupplierId, company, pic, description, address, fullPhoneNumber, email, selectedStatus);
                showSuccess("Supplier updated successfully!");
            }
            dispose();
            refreshParentView();
        } catch (IllegalArgumentException iae) {
            showError("Validation Error: " + iae.getMessage());
        } catch (Exception e) {
            showError("Error saving supplier: " + e.getMessage());
        }
    }

    private void refreshParentView() {
        if (parentFrame instanceof SupplierManagementView) {
            ((SupplierManagementView) parentFrame).refreshTable();
        } else {
            System.err.println("Parent frame is not SupplierManagementView or null, cannot refresh table for suppliers.");
        }
    }
}