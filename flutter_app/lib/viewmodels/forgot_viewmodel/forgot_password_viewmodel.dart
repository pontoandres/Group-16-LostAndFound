import 'dart:async';
import 'package:flutter/foundation.dart'; // compute
import 'package:flutter/material.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'package:flutter_app/services/supabase_auth_service.dart';

class ForgotPasswordViewModel extends ChangeNotifier {
  final emailController = TextEditingController();
  bool isLoading = false;
  String? errorMessage;

  final SupabaseAuthService _authService = SupabaseAuthService();

  bool isOffline = false;
  late final Stream<bool> offlineStream;

  late final Future<String?> prefillEmailFuture;

  ForgotPasswordViewModel() {
    prefillEmailFuture = _loadLastEmail();

    final connectivity = Connectivity();
    offlineStream = connectivity.onConnectivityChanged
        .map((r) => r == ConnectivityResult.none)
        .distinct();

    _seedConnectivity();
  }

  Future<void> _seedConnectivity() async {
    final r = await Connectivity().checkConnectivity();
    isOffline = (r == ConnectivityResult.none);
    notifyListeners();
  }

  Future<String?> _loadLastEmail() async {
    final prefs = await SharedPreferences.getInstance();
    final last = prefs.getString('last_logged_email');
    if (last != null && last.isNotEmpty) {
      emailController.text = last;
    }
    return last;
  }

  Future<void> _saveLastEmail(String email) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('last_logged_email', email);
  }

  static Map<String, dynamic> _validateEmailIsolate(String raw) {
    final trimmed = raw.trim().toLowerCase();
    // Regex razonable para validación básica
    final re = RegExp(r"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$");
    final ok = re.hasMatch(trimmed);
    return <String, dynamic>{'ok': ok, 'email': trimmed};
  }

  Future<bool> recover() async {
    errorMessage = null;

    final v = await compute<String, Map<String, dynamic>>(
      _validateEmailIsolate,
      emailController.text,
    );
    if (!v['ok']) {
      errorMessage = 'Invalid email format';
      notifyListeners();
      return false;
    }
    final email = v['email'] as String;

    final now = await Connectivity().checkConnectivity();
    if (now == ConnectivityResult.none) {
      errorMessage = "You don't have Internet connection";
      notifyListeners();
      return false;
    }

    try {
      isLoading = true;
      notifyListeners();

      await _authService.resetPassword(email); 
      await _saveLastEmail(email);            

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