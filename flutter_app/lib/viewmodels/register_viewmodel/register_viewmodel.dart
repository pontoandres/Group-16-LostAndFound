import 'package:flutter/material.dart';
import '../../services/supabase_auth_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart';



class RegisterViewModel extends ChangeNotifier {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();
  final nameController = TextEditingController();
  final uniIdController = TextEditingController();
  final facultyController = TextEditingController();

  final SupabaseAuthService _authService = SupabaseAuthService();

  bool isLoading = false;
  String? errorMessage;

  /// Método asíncrono de registro que retorna un Future<bool>.
  /// Se ejecuta de forma concurrente sin afectar la interfaz.
  Future<bool> register() async {
    final email = emailController.text.trim();
    final password = passwordController.text.trim();
    final name = nameController.text.trim();
    final uniId = uniIdController.text.trim();
    final faculty = facultyController.text.trim();

    try {
      isLoading = true;
      errorMessage = null;
      notifyListeners();

      final response = await _authService.signUpWithEmailAndPassword(
        email,
        password,
        name,
        uniId,
        faculty,
      );

      return true;
    } on AuthException catch (e) {
      errorMessage = e.message;
      return false;
    } catch (e) {
      errorMessage = e.toString();
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  @override
  void dispose() {
    emailController.dispose();
    passwordController.dispose();
    nameController.dispose();
    uniIdController.dispose();
    facultyController.dispose();
    super.dispose();
  }
}
