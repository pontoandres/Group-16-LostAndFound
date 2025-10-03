import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class ResetPasswordViewModel extends ChangeNotifier {
  final newPasswordController = TextEditingController();
  final confirmPasswordController = TextEditingController();

  bool isLoading = false;
  String? errorMessage;

  Future<bool> updatePassword() async {
    final newPass = newPasswordController.text.trim();
    final confirm = confirmPasswordController.text.trim();

    if (newPass.isEmpty || confirm.isEmpty) {
      errorMessage = 'Debes completar ambos campos';
      notifyListeners();
      return false;
    }
    if (newPass != confirm) {
      errorMessage = 'Las contraseñas no coinciden';
      notifyListeners();
      return false;
    }
    if (newPass.length < 6) {
      errorMessage = 'La contraseña debe tener al menos 6 caracteres';
      notifyListeners();
      return false;
    }

    try {
      isLoading = true;
      errorMessage = null;
      notifyListeners();

      // Con el enlace de Supabase hay una sesión temporal válida para cambiar password
      await Supabase.instance.client.auth.updateUser(
        UserAttributes(password: newPass),
      );

      // Por seguridad, que vuelva a iniciar sesión con la nueva contraseña
      await Supabase.instance.client.auth.signOut();
      return true;
    } on AuthException catch (e) {
      errorMessage = e.message;
      return false;
    } catch (e) {
      errorMessage = 'Error inesperado: $e';
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  @override
  void dispose() {
    newPasswordController.dispose();
    confirmPasswordController.dispose();
    super.dispose();
  }
}