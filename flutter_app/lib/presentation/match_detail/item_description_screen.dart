import 'dart:io';
import 'package:flutter/material.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../theme/app_theme.dart';

class ItemDescriptionScreen extends StatelessWidget {
  const ItemDescriptionScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final verifyCtrl = TextEditingController();
    final arg = ModalRoute.of(context)?.settings.arguments;
    final String? imagePath = (arg is String && arg.isNotEmpty) ? arg : null;

    return Scaffold(
      appBar: const TopBar(title: 'Goatfound', actions: [DebugNavButton()]),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            const SizedBox(height: 8),
            const Center(child: Text('Umbrella', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w900))),
            const SizedBox(height: 4),
            const Text('Posted by Martin'),
            const SizedBox(height: 12),

            Container(
              height: 220,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: Colors.black26, width: 2),
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: imagePath != null
                    ? Image.file(File(imagePath), fit: BoxFit.cover)
                    : Image.asset('assets/images/Rectangle 17.png', fit: BoxFit.cover),
              ),
            ),

            const SizedBox(height: 16),
            const Text('Description', style: TextStyle(fontWeight: FontWeight.w700)),
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppTheme.orange.withOpacity(.6),
                borderRadius: BorderRadius.circular(16),
              ),
              child: const Text(
                'This object was found in room 606 of the ML building on Monday. '
                'Chat with me to coordinate delivery.',
                textAlign: TextAlign.center,
              ),
            ),
            const SizedBox(height: 16),

            ElevatedButton(onPressed: () {}, child: const Text('Claim Object')),
            const SizedBox(height: 12),
            TextField(controller: verifyCtrl, decoration: const InputDecoration(hintText: 'Insert Verification Code')),
            const SizedBox(height: 8),
            ElevatedButton(onPressed: () {}, child: const Text('Verify')),
          ],
        ),
      ),
    );
  }
}
