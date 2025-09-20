import 'package:flutter/material.dart';
import 'package:flutter_app/viewmodels/forgot_viewmodel/forgot_password_viewmodel.dart';
import 'package:provider/provider.dart';

class ForgotPasswordPage extends StatelessWidget {
  const ForgotPasswordPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => ForgotPasswordViewModel(),
      child: const _ForgotPasswordForm(),
    );
  }
}

class _ForgotPasswordForm extends StatelessWidget {
  const _ForgotPasswordForm({super.key});

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<ForgotPasswordViewModel>(context);

    return Scaffold(
      backgroundColor: const Color(0xFF4E919D),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 25, vertical: 40),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Align(
                alignment: Alignment.centerLeft,
                child: ElevatedButton(
                  onPressed: () {
                    Navigator.pushNamed(context, '/login');
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFFE49957),
                    foregroundColor: const Color(0xFF2D3A3A),
                  ),
                  child: const Text("Back"),
                ),
              ),
              const SizedBox(height: 20),

              const Text(
                "Goatfound",
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: Colors.black,
                ),
              ),
              const SizedBox(height: 10),

              const Text(
                "recover your account",
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w600,
                  color: Colors.black,
                ),
              ),
              const SizedBox(height: 10),

              const Text(
                "enter your account email",
                style: TextStyle(fontSize: 16, color: Colors.black),
              ),
              const SizedBox(height: 25),

              const Text("Email", style: _labelStyle),
              const SizedBox(height: 5),
              _buildTextField(
                controller: viewModel.emailController,
                hint: "youremail@account.com",
              ),
              const SizedBox(height: 35),

              SizedBox(
                width: double.infinity,
                height: 55,
                child: ElevatedButton(
                  onPressed: viewModel.isLoading
                      ? null
                      : () async {
                          final success = await viewModel.sendRecoveryEmail();
                          if (context.mounted) {
                            if (success) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text('Se ha enviado un correo de recuperación.'),
                                ),
                              );
                              Navigator.pushNamed(context, '/login');
                            } else {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(
                                  content: Text(viewModel.errorMessage ?? 'Error'),
                                ),
                              );
                            }
                          }
                        },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFFE49957),
                    foregroundColor: const Color(0xFF2D3A3A),
                    textStyle: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.w600,
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                      side: const BorderSide(
                        color: Color(0xFF714E1E),
                        width: 2,
                      ),
                    ),
                  ),
                  child: viewModel.isLoading
                      ? const CircularProgressIndicator(color: Colors.black)
                      : const Text("Recover"),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String hint,
  }) {
    return TextField(
      controller: controller,
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

const TextStyle _labelStyle = TextStyle(
  fontWeight: FontWeight.bold,
  fontSize: 16,
  color: Colors.black,
);
