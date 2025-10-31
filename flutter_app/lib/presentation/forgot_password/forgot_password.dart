import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_app/viewmodels/forgot_viewmodel/forgot_password_viewmodel.dart';

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
  const _ForgotPasswordForm();

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<ForgotPasswordViewModel>(context, listen: false);

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
                  onPressed: () => Navigator.pushNamed(context, '/login'),
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

              FutureBuilder<String?>(
                future: vm.prefillEmailFuture,
                builder: (context, snap) {
                  return _buildTextField(
                    controller: vm.emailController,
                    hint: "youremail@account.com",
                  );
                },
              ),

              const SizedBox(height: 10),

             
              Consumer<ForgotPasswordViewModel>(
                builder: (_, model, __) {
                  return StreamBuilder<bool>(
                    stream: model.offlineStream,
                    initialData: model.isOffline,
                    builder: (context, snapshot) {
                      final offline = snapshot.data ?? false;
                      if (!offline) return const SizedBox.shrink();
                      return Padding(
                        padding: const EdgeInsets.only(bottom: 12.0),
                        child: MaterialBanner(
                          backgroundColor: Colors.amber.shade200,
                          content: const Text(
                            "You're offline. Recovery email cannot be sent.",
                          ),
                          leading: const Icon(Icons.wifi_off),
                          actions: [
                            TextButton(
                              onPressed: () =>
                                  ScaffoldMessenger.of(context).hideCurrentMaterialBanner(),
                              child: const Text('OK'),
                            ),
                          ],
                        ),
                      );
                    },
                  );
                },
              ),

              const SizedBox(height: 20),

              
              Consumer<ForgotPasswordViewModel>(
                builder: (_, model, __) {
                  return SizedBox(
                    width: double.infinity,
                    height: 55,
                    child: ElevatedButton(
                      onPressed: (model.isLoading)
                          ? null
                          : () async {
                              final success = await model.recover();
                              if (!context.mounted) return;

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
                                    content: Text(model.errorMessage ?? 'Error'),
                                  ),
                                );
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
                      child: model.isLoading
                          ? const CircularProgressIndicator(color: Colors.black)
                          : const Text("Recover"),
                    ),
                  );
                },
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
      keyboardType: TextInputType.emailAddress,
    );
  }
}

const TextStyle _labelStyle = TextStyle(
  fontWeight: FontWeight.bold,
  fontSize: 16,
  color: Colors.black,
);