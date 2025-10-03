import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../viewmodels/register_viewmodel/register_viewmodel.dart';

class RegisterPage extends StatelessWidget {
  const RegisterPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => RegisterViewModel(),
      child: const _RegisterPageContent(),
    );
  }
}

class _RegisterPageContent extends StatelessWidget {
  const _RegisterPageContent();

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<RegisterViewModel>(context);

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
                  onPressed: () => Navigator.pushReplacementNamed(context, '/login'),
                  style: _buttonStyle,
                  child: const Text("Back to Login"),
                ),
              ),
              const SizedBox(height: 20),
              const Text(
                "Welcome to Goatfound!",
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Colors.black,
                ),
              ),
              const SizedBox(height: 10),
              const Text(
                "Please fill in all the required fields.",
                style: TextStyle(fontSize: 16, color: Colors.black),
              ),
              const SizedBox(height: 25),
              const Text("Email", style: _labelStyle),
              const SizedBox(height: 5),
              _buildTextField(vm.emailController, hint: "youremail@uniandes.edu.co"),
              const SizedBox(height: 20),
              const Text("Password", style: _labelStyle),
              const SizedBox(height: 5),
              _buildTextField(vm.passwordController, hint: "Your password", obscure: true),
              const SizedBox(height: 20),
              const Text("Name", style: _labelStyle),
              const SizedBox(height: 5),
              _buildTextField(vm.nameController, hint: "Your name"),
              const SizedBox(height: 20),
              const Text("University ID Number", style: _labelStyle),
              const SizedBox(height: 5),
              _buildTextField(vm.uniIdController, hint: "Your university code"),
              const SizedBox(height: 35),
              SizedBox(
                width: double.infinity,
                height: 55,
                child: ElevatedButton(
                  onPressed: vm.isLoading
                      ? null
                      : () async {
                          print('Botón Create Account presionado');
                          final success = await vm.register();
                          print('Resultado de vm.register(): $success');

                          if (success && context.mounted) {
                            print('Navegando a /login');
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text("Account created. Check your email.")),
                            );
                            Future.delayed(const Duration(milliseconds: 300), () {
                              Navigator.pushNamedAndRemoveUntil(context, '/login', (route) => false);
                            });
                          } else if (vm.errorMessage != null) {
                            print('Error mostrado al usuario: ${vm.errorMessage}');
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text(vm.errorMessage!)),
                            );
                          }
                        },
                  style: _buttonStyle,
                  child: vm.isLoading
                      ? const CircularProgressIndicator(color: Colors.white)
                      : const Text("Create account"),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTextField(TextEditingController controller,
      {required String hint, bool obscure = false}) {
    return TextField(
      controller: controller,
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

const TextStyle _labelStyle = TextStyle(
  fontWeight: FontWeight.bold,
  fontSize: 16,
  color: Colors.black,
);

final ButtonStyle _buttonStyle = ElevatedButton.styleFrom(
  backgroundColor: const Color(0xFFE49957),
  foregroundColor: const Color(0xFF2D3A3A),
  textStyle: const TextStyle(
    fontSize: 16,
    fontWeight: FontWeight.w600,
  ),
  shape: RoundedRectangleBorder(
    borderRadius: BorderRadius.circular(8),
    side: const BorderSide(
      color: Color(0xFF714E1E),
      width: 2,
    ),
  ),
);

