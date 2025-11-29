import 'package:flutter/material.dart';
import 'package:flutter_app/viewmodels/login_viewmodel/login_viewmodel.dart';
import 'package:provider/provider.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';

class LoginPage extends StatelessWidget {
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => LoginViewModel(),
      child: const _LoginForm(),
    );
  }
}

class _LoginForm extends StatefulWidget {
  const _LoginForm();

  @override
  State<_LoginForm> createState() => _LoginFormState();
}

class _LoginFormState extends State<_LoginForm> {
  @override
  void initState() {
    super.initState();
    _checkCachedLogin();
    _loadLastEmail(); //
  }

  Future<void> _checkCachedLogin() async {
    final prefs = await SharedPreferences.getInstance();
    final savedUser = prefs.getString('user_email');
    if (savedUser != null) {
      if (mounted) Navigator.pushReplacementNamed(context, '/feed');
    }
  }

  //  carga último email escrito
  Future<void> _loadLastEmail() async {
    final prefs = await SharedPreferences.getInstance();
    final lastEmail = prefs.getString('last_logged_email');
    if (lastEmail != null && mounted) {
      setState(() => context.read<LoginViewModel>().emailController.text = lastEmail);
    }
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<LoginViewModel>(context);

    return Scaffold(
      backgroundColor: const Color(0xFF4E919D),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          children: [
            const SizedBox(height: 40),
            Image.asset('assets/images/login.png', height: 280),
            const SizedBox(height: 20),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text("Email",
                  style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black)),
            ),
            const SizedBox(height: 5),
            TextField(
              controller: viewModel.emailController,
              style: const TextStyle(color: Color(0xFF635F5F)),
              decoration: InputDecoration(
                hintText: "youremail@uniandes.edu.co",
                hintStyle: const TextStyle(color: Color(0xFF6DAEAE)),
                filled: true,
                fillColor: const Color(0xFF6DAEAE),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: BorderSide.none,
                ),
              ),
            ),
            const SizedBox(height: 20),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text("Password",
                  style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black)),
            ),
            const SizedBox(height: 5),
            TextField(
              controller: viewModel.passwordController,
              obscureText: true,
              style: const TextStyle(color: Color(0xFF635F5F)),
              decoration: InputDecoration(
                hintText: "Your password",
                hintStyle: const TextStyle(color: Color(0xFF6DAEAE)),
                filled: true,
                fillColor: const Color(0xFF6DAEAE),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: BorderSide.none,
                ),
              ),
            ),
            const SizedBox(height: 30),
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFFE49957),
                  foregroundColor: Colors.black,
                  textStyle: const TextStyle(fontSize: 18),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                    side: const BorderSide(color: Color(0xFF714E1E), width: 2),
                  ),
                ),
                onPressed: viewModel.isLoading
                    ? null
                    : () async {
                        final connectivity = await Connectivity().checkConnectivity();
                        final prefs = await SharedPreferences.getInstance();

                        if (connectivity == ConnectivityResult.none) {
                          final savedUser = prefs.getString('user_email');
                          if (savedUser != null) {
                            if (context.mounted) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(
                                  content: Text("Offline login: Welcome back, $savedUser"),
                                ),
                              );
                              Navigator.pushReplacementNamed(context, '/feed');
                            }
                            return;
                          } else {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text("Offline mode unavailable — please connect."),
                              ),
                            );
                            return;
                          }
                        }

                        final success = await viewModel.login();
                        if (success && context.mounted) {
                          await prefs.setString('user_email',
                              viewModel.emailController.text.trim());
                          
                          await prefs.setString('last_logged_email',
                              viewModel.emailController.text.trim());
                          Navigator.pushReplacementNamed(context, '/feed');
                        } else if (viewModel.errorMessage != null && context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text(viewModel.errorMessage!)),
                          );
                        }
                      },
                child: viewModel.isLoading
                    ? const CircularProgressIndicator(color: Colors.black)
                    : const Text("Log in"),
              ),
            ),
            const SizedBox(height: 20),
            TextButton(
              onPressed: () => Navigator.pushNamed(context, '/register'),
              child: const Text("Are you not registered yet?",
                  style: TextStyle(color: Colors.black)),
            ),
            TextButton(
              onPressed: () => Navigator.pushNamed(context, '/forgot_password'),
              child: const Text("Forgot your password?",
                  style: TextStyle(color: Colors.black)),
            ),
          ],
        ),
      ),
      bottomNavigationBar: const Padding(
        padding: EdgeInsets.all(12),
        child: Text(
          "Solo se permiten correos @uniandes.edu.co",
          textAlign: TextAlign.center,
          style: TextStyle(color: Colors.white70, fontSize: 12),
        ),
      ),
    );
  }
}
