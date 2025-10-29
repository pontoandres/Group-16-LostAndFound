import 'package:flutter/material.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../viewmodels/feed/feed_viewmodel.dart';
import '../../theme/app_theme.dart';
import '../../routes/app_routes.dart';

class ClaimObjectScreen extends StatelessWidget {
  const ClaimObjectScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // ✅ Recupera el argumento enviado desde la pantalla de detalle
    final arg = ModalRoute.of(context)?.settings.arguments;

    // ✅ Verifica que el argumento sea del tipo FeedItem
    if (arg is! FeedItem) {
      return const Scaffold(
        body: Center(child: Text("No item data provided")),
      );
    }

    final item = arg;

    return Scaffold(
      appBar: const TopBar(
        title: 'Contact',
        actions: [DebugNavButton()],
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 20),
            const Text(
              "Contact Information",
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 20),

            // ✅ Mensaje con nombre y correo del publicador
            Text(
              "You can contact ${item.ownerName ?? 'the user'} "
              "through this email:",
              style: const TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 10),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppTheme.orange.withOpacity(0.7),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                item.ownerEmail ?? "No email available",
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            const SizedBox(height: 30),
            const Text(
              "If you lost this item, please contact the user directly via email.",
              style: TextStyle(
                fontSize: 14,
                fontStyle: FontStyle.italic,
              ),
            ),
            const Spacer(),
            Center(
              child: ElevatedButton(
                onPressed: () {
                  Navigator.pushNamedAndRemoveUntil(
                    context,
                    AppRoutes.feed,
                    (route) => false,
                  );
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppTheme.orange,
                  foregroundColor: Colors.black,
                  minimumSize: const Size(180, 45),
                ),
                child: const Text("Go back to Feed"),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
