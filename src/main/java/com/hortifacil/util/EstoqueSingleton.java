package com.hortifacil.util;

import com.hortifacil.controller.AdminAdicionarEstoqueController;

/**
 * Singleton para fornecer acesso global ao ControleEstoqueAdminController.
 * Deve ser inicializado uma única vez via setInstance().
 */
public class EstoqueSingleton {

    private static AdminAdicionarEstoqueController instance;

    public static void setInstance(AdminAdicionarEstoqueController ctrl) {
        instance = ctrl;
    }

    public static AdminAdicionarEstoqueController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AdionarEstoqueController não foi inicializado");
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }
}
