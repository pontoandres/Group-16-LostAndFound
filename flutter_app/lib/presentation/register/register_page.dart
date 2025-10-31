import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
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

class _RegisterPageContent extends StatefulWidget {
  const _RegisterPageContent();

  @override
  State<_RegisterPageContent> createState() => _RegisterPageContentState();
}

class _RegisterPageContentState extends State<_RegisterPageContent> {
  bool _isRetrying = false;

  @override
  void initState() {
    super.initState();
    _checkPendingRegistration();
    _loadLastRegisteredUser(); //  para poder cargar el último registro exitoso
  }

  Future<void> _checkPendingRegistration() async {
    final prefs = await SharedPreferences.getInstance();
    final hasPending = prefs.containsKey('pending_registration');
    final connectivity = await Connectivity().checkConnectivity();

    if (hasPending && connectivity != ConnectivityResult.none) {
      setState(() => _isRetrying = true);
      final data = prefs.getStringList('pending_registration');
      if (data != null && data.length == 5) {
        final vm = Provider.of<RegisterViewModel>(context, listen: false);
        vm.emailController.text = data[0];
        vm.passwordController.text = data[1];
        vm.nameController.text = data[2];
        vm.uniIdController.text = data[3];
        vm.facultyController.text = data[4];
        await vm.register();
        await prefs.remove('pending_registration');
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text("Pending registration completed.")),
          );
        }
      }
      setState(() => _isRetrying = false);
    }
  }

  // cargar último usuario registrado exitosamente
  Future<void> _loadLastRegisteredUser() async {
    final prefs = await SharedPreferences.getInstance();
    final data = prefs.getStringList('last_registered_user');
    if (data != null && data.length == 4) {
      final vm = Provider.of<RegisterViewModel>(context, listen: false);
      vm.emailController.text = data[0];
      vm.nameController.text = data[1];
      vm.uniIdController.text = data[2];
      vm.facultyController.text = data[3];
    }
  }

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
                  onPressed: () =>
                      Navigator.pushReplacementNamed(context, '/login'),
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
              _buildTextField(vm.emailController,
                  hint: "youremail@uniandes.edu.co"),
              const SizedBox(height: 20),
              const Text("Password", style: _labelStyle),
              const SizedBox(height: 5),
              _buildTextField(vm.passwordController,
                  hint: "Your password", obscure: true),
              const SizedBox(height: 20),
              const Text("Name", style: _labelStyle),
              const SizedBox(height: 5),
              _buildTextField(vm.nameController, hint: "Your name"),
              const SizedBox(height: 20),
              const Text("University ID Number", style: _labelStyle),
              const SizedBox(height: 5),
              _buildTextField(vm.uniIdController, hint: "Your university code"),
              const SizedBox(height: 20),
              const Text("Faculty", style: _labelStyle),
              const SizedBox(height: 5),
              _buildTextField(vm.facultyController,
                  hint: "e.g. Ingeniería, Derecho, Economía"),
              const SizedBox(height: 35),
              if (_isRetrying)
                const Center(
                    child: CircularProgressIndicator(color: Colors.white))
              else
                SizedBox(
                  width: double.infinity,
                  height: 55,
                  child: ElevatedButton(
                    onPressed: () async {
                      final connectivity =
                          await Connectivity().checkConnectivity();
                      final prefs = await SharedPreferences.getInstance();

                      if (connectivity == ConnectivityResult.none) {
                        await prefs.setStringList('pending_registration', [
                          vm.emailController.text.trim(),
                          vm.passwordController.text.trim(),
                          vm.nameController.text.trim(),
                          vm.uniIdController.text.trim(),
                          vm.facultyController.text.trim(),
                        ]);
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                              content: Text(
                                  "No connection. Registration saved locally.")),
                        );
                        return;
                      }

                      final success = await vm.register();
                      if (success && context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                              content:
                                  Text("Account created. Check your email.")),
                        );
                        await prefs.remove('pending_registration');

                        //  guardar último registro exitoso
                        await prefs.setStringList('last_registered_user', [
                          vm.emailController.text.trim(),
                          vm.nameController.text.trim(),
                          vm.uniIdController.text.trim(),
                          vm.facultyController.text.trim(),
                        ]);
                      } else if (vm.errorMessage != null) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text(vm.errorMessage!)),
                        );
                      }
                    },
                    style: _buttonStyle,
                    child: const Text("Create account"),
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
  textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
  shape: RoundedRectangleBorder(
    borderRadius: BorderRadius.circular(8),
    side: const BorderSide(color: Color(0xFF714E1E), width: 2),
  ),
);
