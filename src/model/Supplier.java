package model;

public class Supplier {
    private String supplierId, supplierCompany, supplierPIC, supplierDescription, supplierAddress, supplierPhone, status, createdAt, updatedAt, createdBy, updatedBy;

    public Supplier(String supplierId, String supplierCompany, String supplierPIC, String supplierDescription, String supplierAddress, String supplierPhone, String createdAt, String updatedAt, String createdBy, String updatedBy) {
        this.supplierId = supplierId;
        this.supplierCompany = supplierCompany;
        this.supplierPIC = supplierPIC;
        this.supplierDescription = supplierDescription;
        this.supplierAddress = supplierAddress;
        this.supplierPhone = supplierPhone;
        this.status = "active";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierCompany() { return supplierCompany; }
    public void setSupplierCompany(String supplierCompany) { this.supplierCompany = supplierCompany; }

    public String getSupplierPIC() { return supplierPIC; }
    public void setSupplierPIC(String supplierPIC) { this.supplierPIC = supplierPIC; }

    public String getSupplierDescription() { return supplierDescription; }
    public void setSupplierDescription(String supplierDescription) { this.supplierDescription = supplierDescription; }

    public String getSupplierAddress() { return supplierAddress; }
    public void setSupplierAddress(String supplierAddress) { this.supplierAddress = supplierAddress; }

    public String getSupplierPhone() { return supplierPhone; }
    public void setSupplierPhone(String supplierPhone) { this.supplierPhone = supplierPhone; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String toCSV() {
        return String.join(",", supplierId, supplierCompany, supplierPIC, supplierDescription, supplierAddress, supplierPhone, createdAt, updatedAt, createdBy, updatedBy);
    }

    public String toJSON() {
        return "{" +
                "\"supplierId\": \"" + supplierId + "\"," +
                "\"supplierCompany\": \"" + supplierCompany + "\"," +
                "\"supplierPIC\": " + supplierPIC + "," +
                "\"supplierDescription\": \"" + supplierDescription + "\"," +
                "\"supplierAddress\": \"" + supplierAddress + "\"," +
                "\"supplierPhone\": \"" + supplierPhone + "\"," +
                "\"createdAt\": \"" + createdAt + "\"," +
                "\"updatedAt\": \"" + updatedAt + "\"," +
                "\"createdBy\": \"" + createdBy + "\"," +
                "\"updatedBy\": \"" + updatedBy + "\"" +
                "}";
    }
}
