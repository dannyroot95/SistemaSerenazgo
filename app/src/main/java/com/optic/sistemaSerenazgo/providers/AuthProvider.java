package com.optic.sistemaSerenazgo.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthProvider {

    //CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

    FirebaseAuth mAuth;

    //PARA AUTENTICAR USUARIO
    public AuthProvider() {
        mAuth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> register(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    //PARA INICIAR SESION
    public Task<AuthResult> login(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    //PARA CERRAR SESION
    public void logout() {
        mAuth.signOut();
    }

    //PARA OBTENER EL ID DEL USUARIO
    public String getId() {
        return mAuth.getCurrentUser().getUid();
    }

    // PARA SABER EL INICIO DE SESION
    public boolean existSession() {
        boolean exist = false;
        if (mAuth.getCurrentUser() != null) {
            exist = true;
        }
        return exist;
    }

}
