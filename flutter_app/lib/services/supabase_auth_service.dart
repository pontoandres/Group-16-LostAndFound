import 'package:supabase_flutter/supabase_flutter.dart';

const String? kAllowedEmailDomain = null;

bool _isEmailAllowed(String email) {
  if (kAllowedEmailDomain == null) return true;
  return email.toLowerCase().trim().endsWith('@$kAllowedEmailDomain');
}

class SupabaseAuthService {
  final SupabaseClient _client = Supabase.instance.client;

  Future<AuthResponse> signInWithEmailAndPassword(String email, String password) async {
    final trimmedEmail = email.trim();

    if (!_isEmailAllowed(trimmedEmail)) {
      throw AuthException('Dominio de correo no permitido');
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

  Future<AuthResponse> signUpWithEmailAndPassword(
    String email,
    String password,
    String name,
    String uniId,
  ) async {
    final trimmedEmail = email.trim();

    if (!_isEmailAllowed(trimmedEmail)) {
      throw AuthException('Dominio de correo no permitido');
    }
    if (password.isEmpty || name.isEmpty || uniId.isEmpty) {
      throw Exception('Todos los campos son obligatorios');
    }

    try {
      final response = await _client.auth.signUp(
        email: trimmedEmail,
        password: password,
      );

      final userId = response.user?.id;
      if (userId == null) {
        throw AuthException('No se pudo crear el usuario');
      }

      // Crea/actualiza el perfil
      await _client.from('profiles').upsert({
        'id': userId,
        'name': name,
        'university_id': uniId,
      });

      return response;
    } on AuthException catch (e) {
      throw AuthException(e.message);
    } catch (e) {
      throw Exception('Error inesperado al registrarse: $e');
    }
  }

  Future<void> resetPassword(String email) async {
    final trimmedEmail = email.trim();

    if (!_isEmailAllowed(trimmedEmail)) {
      throw AuthException('Dominio de correo no permitido');
    }

    try {
      await _client.auth.resetPasswordForEmail(
        trimmedEmail,
        redirectTo: 'io.supabase.flutter://login-callback/',
      );
    } on AuthException catch (e) {
      throw AuthException(e.message);
    } catch (e) {
      throw Exception('Error inesperado al solicitar recuperación');
    }
  }

  Future<void> signOut() async => _client.auth.signOut();

  User? getCurrentUser() => _client.auth.currentUser;
}