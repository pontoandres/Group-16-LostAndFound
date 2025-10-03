import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_app/viewmodels/reset_password_viewmodel/reset_password_viewmodel.dart';

class ResetPasswordPage extends StatelessWidget {
  const ResetPasswordPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => ResetPasswordViewModel(),
      child: const _ResetPasswordForm(),
    );
  }
}

class _ResetPasswordForm extends StatelessWidget {
  const _ResetPasswordForm();

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<ResetPasswordViewModel>(context);

    return Scaffold(
      backgroundColor: const Color(0xFF4E919D),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 25, vertical: 40),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                "Set new password",
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.black),
              ),
              const SizedBox(height: 20),

              const Text("New password", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black)),
              const SizedBox(height: 6),
              _field(vm.newPasswordController, hint: "New password", obscure: true),
              const SizedBox(height: 16),

              const Text("Confirm password", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black)),
              const SizedBox(height: 6),
              _field(vm.confirmPasswordController, hint: "Confirm password", obscure: true),
              const SizedBox(height: 28),

              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton(
                  onPressed: vm.isLoading
                      ? null
                      : () async {
                          final ok = await vm.updatePassword();
                          if (!context.mounted) return;

                          if (ok) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Contraseña actualizada. Inicia sesión de nuevo.')),
                            );
                            // Limpia el stack y vuelve al login
                            Navigator.pushNamedAndRemoveUntil(context, '/login', (route) => false);
                          } else if (vm.errorMessage != null) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text(vm.errorMessage!)),
                            );
                          }
                        },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFFE49957),
                    foregroundColor: Colors.black,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                      side: const BorderSide(color: Color(0xFF714E1E), width: 2),
                    ),
                  ),
                  child: vm.isLoading
                      ? const CircularProgressIndicator(color: Colors.black)
                      : const Text("Update password"),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _field(TextEditingController c, {required String hint, bool obscure = false}) {
    return TextField(
      controller: c,
      obscureText: obscure,
      style: const TextStyle(color: Color(0xFF2D3A3A)),
      decoration: InputDecoration(
        hintText: hint,
        hintStyle: const TextStyle(color: Color(0xFF6DAEAE)),
        filled: true,
        fillColor: const Color(0xFF6DAEAE),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide.none,
        ),
      ),
    );
  }
}