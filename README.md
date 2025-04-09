# omega-wholesale

Project File Structure:
/owsb-purchase-order-system
├── src/com/owsb
│   ├── model            ← Data classes (POJOs), logic, file handling
│   │   ├── User.java
│   │   ├── Item.java
│   │   ├── Supplier.java
│   │   ├── PurchaseRequisition.java
│   │   └── PurchaseOrder.java
│   │
│   ├── view             ← UI logic (menus, prompts, CLI/GUI)
│   │   ├── AdminView.java
│   │   ├── LoginView.java
│   │   └── SalesManagerView.java
│   │
│   ├── controller       ← Controls flow between model and view
│   │   ├── AdminController.java
│   │   ├── LoginController.java
│   │   └── SalesManagerController.java
│   │
│   └── Main.java        ← Entry point
│
├── data                 ← Text files used for data persistence
│   ├── users.txt
│   ├── items.txt
│   ├── suppliers.txt
│   ├── requisitions.txt
│   └── orders.txt
│
├── docs                 ← UML diagrams, report
│
├── .gitignore
├── README.md
