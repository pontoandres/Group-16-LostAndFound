import 'package:flutter/material.dart';
import 'package:flutter_app/services/supabase_auth_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class ForgotPasswordViewModel extends ChangeNotifier {
  final emailController = TextEditingController();
  final SupabaseAuthService _authService = SupabaseAuthService();

  bool isLoading = false;
  String? errorMessage;

  Future<bool> sendRecoveryEmail() async {
    final email = emailController.text.trim();

    try {
      isLoading = true;
      errorMessage = null;
      notifyListeners();

      await _authService.resetPassword(email);
      return true;
    } on AuthException catch (e) {
      errorMessage = e.message;
      return false;
    } catch (_) {
      errorMessage = 'Unexpected error during password recovery';
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  @override
  void dispose() {
    emailController.dispose();
    super.dispose();
  }
}
