import view.LoginView;

import controller.AuthController;

public class main {
    public static void main(String[] args) {
        AuthController authController = new AuthController();
        authController.displayLoginMenu();
    }

