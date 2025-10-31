import 'package:flutter/material.dart';
import 'package:flutter_app/services/supabase_auth_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class LoginViewModel extends ChangeNotifier {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();

  final SupabaseAuthService _authService = SupabaseAuthService();
  final SupabaseClient _client = Supabase.instance.client;

  bool isLoading = false;
  String? errorMessage;


  Future<bool> login() async {
    final email = emailController.text.trim();
    final password = passwordController.text.trim();

    print("Iniciando login...");
    print("Email: $email");

    try {
      isLoading = true;
      errorMessage = null;
      notifyListeners();

     
      final response =
          await _authService.signInWithEmailAndPassword(email, password);
      final user = response.user;

  
      if (user == null) {
        errorMessage = "Credenciales inválidas";
        return false;
      }

      print("Usuario autenticado en Auth: ${user.id}");

      
      final result = await _client
          .from('profiles')
          .select()
          .eq('id', user.id)
          .maybeSingle();

      if (result == null) {
        errorMessage = "Tu cuenta no está registrada en la base de datos";
        await _authService.signOut();
        return false;
      }

      print("Perfil encontrado en profiles: $result");
      return true;
    } on AuthException catch (e) {
      
      errorMessage = e.message;
      print("Error de autenticación: ${e.message}");
      return false;
    } catch (e) {
      
      errorMessage = 'Error inesperado: $e';
      print("Error inesperado en login: $e");
      return false;
    } finally {
  
      isLoading = false;
      notifyListeners();
      print("Login finalizado");
    }
  }

  
  @override
  void dispose() {
    emailController.dispose();
    passwordController.dispose();
    super.dispose();
  }
}

