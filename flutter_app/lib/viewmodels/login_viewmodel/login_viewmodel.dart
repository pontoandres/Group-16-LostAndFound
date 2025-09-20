import 'package:flutter/material.dart';
import 'package:flutter_app/services/supabase_auth_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class LoginViewModel extends ChangeNotifier {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();

  final SupabaseAuthService _authService = SupabaseAuthService();

  bool isLoading = false;
  String? errorMessage;

  /// Handles user login and manages state updates
  Future<bool> login() async {
    final email = emailController.text;
    final password = passwordController.text;

    try {
      isLoading = true;
      errorMessage = null;
      notifyListeners();

      final response = await _authService.signInWithEmailAndPassword(email, password);
      return response.user != null;
    } on AuthException catch (e) {
      errorMessage = e.message;
      return false;
    } catch (_) {
      errorMessage = 'Unexpected error during login';
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  /// Disposes controllers to free resources
  @override
  void dispose() {
    emailController.dispose();
    passwordController.dispose();
    super.dispose();
  }
}

