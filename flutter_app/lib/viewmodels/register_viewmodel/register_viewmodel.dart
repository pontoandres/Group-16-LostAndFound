import 'package:flutter/material.dart';
import '../../services/supabase_auth_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class RegisterViewModel extends ChangeNotifier {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();
  final nameController = TextEditingController();
  final uniIdController = TextEditingController();

  final SupabaseAuthService _authService = SupabaseAuthService();

  bool isLoading = false;
  String? errorMessage;

  Future<bool> register() async {
    final email = emailController.text.trim();
    final password = passwordController.text.trim();
    final name = nameController.text.trim();
    final uniId = uniIdController.text.trim();

    print('Iniciando registro...');
    print('Email: $email');
    print('Password: $password');
    print('Name: $name');
    print('University ID: $uniId');

    try {
      isLoading = true;
      errorMessage = null;
      notifyListeners();

      final response = await _authService.signUpWithEmailAndPassword(
        email,
        password,
        name,
        uniId,
      );

      print('Registro completado con éxito. User ID: ${response.user?.id}');
      return true;
    } on AuthException catch (e) {
      errorMessage = e.message;
      print('Error de autenticación: ${e.message}');
      return false;
    } catch (e) {
      errorMessage = e.toString();
      print('Error inesperado: $e');
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
      print('Registro finalizado.');
    }
  }

  @override
  void dispose() {
    emailController.dispose();
    passwordController.dispose();
    nameController.dispose();
    uniIdController.dispose();
    super.dispose();
  }
}

