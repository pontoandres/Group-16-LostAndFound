import 'dart:async';
import 'dart:io';                   
import 'package:flutter/foundation.dart';
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
  final SupabaseClient _client = Supabase.instance.client; 

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

  Future<bool> _hasInternet() async {
    final status = await Connectivity().checkConnectivity();
    if (status == ConnectivityResult.none) return false;

    try {
      final res = await InternetAddress.lookup('example.com')
          .timeout(const Duration(seconds: 2));
      return res.isNotEmpty && res.first.rawAddress.isNotEmpty;
    } on SocketException {
      return false;
    } on TimeoutException {
      return false;
    }
  }

  static Map<String, dynamic> _validateEmailIsolate(String raw) {
    final trimmed = raw.trim().toLowerCase();
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

    if (!await _hasInternet()) {
      errorMessage =
          "You can’t change your password, because you don’t have Internet.";
      notifyListeners();
      return false;
    }

    try {
      isLoading = true;
      notifyListeners();

      await _authService.resetPassword(email);

      await _saveLastEmail(email);

      try {
        await _client.from('password_audit_requests').insert({
          'email': email,
          
        });
      } catch (e) {
        debugPrint('Audit insert error (password_audit_requests): $e');
      }

      return true;
    } on SocketException {
      errorMessage =
          "You can’t change your password, because you don’t have Internet.";
      return false;
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
