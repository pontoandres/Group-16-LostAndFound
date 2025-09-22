import 'package:flutter/material.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart'; 

class ClaimObjectScreen extends StatelessWidget {
  const ClaimObjectScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final msg = TextEditingController(text: 'Hola este es mi objeto');

    return Scaffold(
      appBar: const TopBar(
        title: 'Claim Object',
        actions: [DebugNavButton()], 
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Enviar correo'),
            const SizedBox(height: 8),
            TextField(
              controller: msg,
              maxLines: 6,
              decoration: const InputDecoration(),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {},
              child: const Text('Send + Generate Code'),
            ),
          ],
        ),
      ),
    );
  }
}
