import 'package:flutter/material.dart';
import '../../services/supabase_auth_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

class RegisterViewModel extends ChangeNotifier {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();
  final nameController = TextEditingController();
  final uniIdController = TextEditingController();
  final facultyController = TextEditingController();

  final SupabaseAuthService _authService = SupabaseAuthService();

  bool isLoading = false;
  String? errorMessage;

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

      final connectivity = await Connectivity().checkConnectivity();
      final prefs = await SharedPreferences.getInstance();

      if (connectivity == ConnectivityResult.none) {
        final pending = {
          'email': email,
          'password': password,
          'name': name,
          'uniId': uniId,
          'faculty': faculty,
        };
        await prefs.setString('pending_registration', json.encode(pending));
        errorMessage = '📴 Offline: Registration saved locally.';
        notifyListeners();
        return true;
      }

      
      await _authService.signUpWithEmailAndPassword(
        email,
        password,
        name,
        uniId,
        faculty,
      );

     
      await prefs.remove('pending_registration');
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


  Future<void> trySyncPendingRegistration() async {
    final prefs = await SharedPreferences.getInstance();
    final connectivity = await Connectivity().checkConnectivity();
    final cached = prefs.getString('pending_registration');
    if (cached == null || connectivity == ConnectivityResult.none) return;

    try {
      final data = json.decode(cached);
      await _authService.signUpWithEmailAndPassword(
        data['email'],
        data['password'],
        data['name'],
        data['uniId'],
        data['faculty'],
      );
      await prefs.remove('pending_registration');
    } catch (_) {}
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
