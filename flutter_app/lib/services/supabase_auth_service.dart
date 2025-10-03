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
      print('AuthException al iniciar sesión: ${e.message}');
      throw AuthException(e.message);
    } catch (e) {
      print('Error inesperado al iniciar sesión: $e');
      throw Exception('Error inesperado al iniciar sesión');
    }
  }

  /// Registra un nuevo usuario (crea Auth + crea/actualiza perfil)
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
      // Crear usuario en Auth
      final response = await _client.auth.signUp(
        email: trimmedEmail,
        password: password,
      );

      final userId = response.user?.id;
      if (userId == null) {
        throw AuthException('No se pudo crear el usuario');
      }

      print('Usuario creado en Auth: $userId');

      // Insertar/actualizar perfil (ya no usamos .error)
      final responseUpsert = await _client.from('profiles').upsert({
        'id': userId,
        'name': name,
        'university_id': uniId,
      }).select(); // select() devuelve los datos insertados/actualizados

      print('Resultado upsert profiles: $responseUpsert');

      return response;
    } on AuthException catch (e) {
      print('AuthException en signUpWithEmailAndPassword: ${e.message}');
      throw AuthException(e.message);
    } catch (e) {
      print('Error inesperado en signUpWithEmailAndPassword: $e');
      throw Exception('Error al registrarse: $e');
    }
  }

  Future<void> resetPassword(String email) async {
    final trimmedEmail = email.trim();
    if (!trimmedEmail.endsWith('@uniandes.edu.co')) {
      throw AuthException('Solo se permiten correos @uniandes.edu.co');
    }

    try {
      await _client.auth.resetPasswordForEmail(trimmedEmail);
    } on AuthException catch (e) {
      print('AuthException en resetPassword: ${e.message}');
      throw AuthException(e.message);
    } catch (e) {
      print('Error inesperado en resetPassword: $e');
      throw Exception('Error inesperado al solicitar recuperación');
    }
  }

  Future<void> signOut() async {
    await _client.auth.signOut();
  }

  User? getCurrentUser() {
    return _client.auth.currentUser;
  }
}

