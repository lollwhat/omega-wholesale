# omega-wholesale

## A. Quick Start
### 1. Setup Project
Setup your project by running the code below in your cmd line // terminal (in the file you want the code store in)
<br><br/>
a. Clone the Repo
```bash
git clone https://github.com/lollwhat/omega-wholesale.git
```
b. Go to dev branch:
```bash
git checkout dev
```
c. Create and go to your branch:
```bash
git checkout -b <ur-branch>
```
d. Push and create your branch in remote(GitHub):
```bash
git push -u origin <ur-branch>
```
<br><br>
### 2. Commit & Push - Update your changes
Whenever you make changes, you can use terminal in IntelliJ to commit this way
<br><br>
a. Check the status of your files:
```bash
git status
```
b. Stage all your files:
```bash
git add .
```
OR Stage specific files:
```bash
git add <file1> <file2> ...
```
c. Commit your files:
```bash
git commit -m "<ur-message>"
```
d. Push to remote(GitHub):
```bash
git push origin HEAD
```
<br><br>
### 3. Pull & Merge - Get the latest changes
Whenever need to merge dev to your branch (to update your branch to newest version
<br><br>
a. Go to local dev branch:
```bash
git checkout dev
```
b. Fetch and merge from remote dev branch:
```bash
git pull origin dev
```
c. Go to your local branch:
```bash
git checkout <ur-branch>
```
d. Merge dev to your local branch:
```bash
git merge dev
```
e. Push latest to your remote branch:
```bash
git push origin HEAD
```
<br><br>
## B. Project File Structure:
```markdown
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
```
