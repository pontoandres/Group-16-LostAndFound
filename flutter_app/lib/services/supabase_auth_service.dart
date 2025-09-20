import 'package:supabase_flutter/supabase_flutter.dart';

class SupabaseAuthService {
  final SupabaseClient _client = Supabase.instance.client;

  /// Inicia sesión con correo y contraseña
  Future<AuthResponse> signInWithEmailAndPassword(String email, String password) async {
    final trimmedEmail = email.trim();
    if (!trimmedEmail.endsWith('@uniandes.edu.co')) {
      throw AuthException('Solo se permiten correos @uniandes.edu.co');
    }

    try {
      return await _client.auth.signInWithPassword(
        email: trimmedEmail,
        password: password,
      );
    } on AuthException catch (e) {
      throw AuthException(e.message);
    } catch (e) {
      throw Exception('Error inesperado al iniciar sesión');
    }
  }

  /// Registra un nuevo usuario
  Future<AuthResponse> signUpWithEmailAndPassword(
      String email, String password, String name, String uniId) async {
    final trimmedEmail = email.trim();
    if (!trimmedEmail.endsWith('@uniandes.edu.co')) {
      throw AuthException('Solo se permiten correos @uniandes.edu.co');
    }

    if (password.isEmpty || name.isEmpty || uniId.isEmpty) {
      throw Exception('Todos los campos son obligatorios');
    }

    try {
      return await _client.auth.signUp(
        email: trimmedEmail,
        password: password,
        data: {
          'name': name,
          'university_id': uniId,
        },
      );
    } on AuthException catch (e) {
      throw AuthException(e.message);
    } catch (e) {
      throw Exception('Error inesperado al registrarse');
    }
  }

  /// Recuperar contraseña (envía email con enlace)
  Future<void> resetPassword(String email) async {
    final trimmedEmail = email.trim();
    if (!trimmedEmail.endsWith('@uniandes.edu.co')) {
      throw AuthException('Solo se permiten correos @uniandes.edu.co');
    }

    try {
      await _client.auth.resetPasswordForEmail(trimmedEmail);
    } on AuthException catch (e) {
      throw AuthException(e.message);
    } catch (e) {
      throw Exception('Error inesperado al solicitar recuperación');
    }
  }

  /// Cierra sesión
  Future<void> signOut() async {
    await _client.auth.signOut();
  }

  /// Obtiene el usuario actual
  User? getCurrentUser() {
    return _client.auth.currentUser;
  }
}
