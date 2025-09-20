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
    final email = emailController.text;
    final password = passwordController.text;
    final name = nameController.text;
    final uniId = uniIdController.text;

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

      return response.user != null;
    } on AuthException catch (e) {
      errorMessage = e.message;
      return false;
    } catch (_) {
      errorMessage = 'Unexpected error during registration';
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
    super.dispose();
  }
}
