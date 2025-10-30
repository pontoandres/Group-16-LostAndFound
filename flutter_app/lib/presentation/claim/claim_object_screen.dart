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
    final arg = ModalRoute.of(context)?.settings.arguments;
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
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 20),
            Text(
              "You can contact ${item.ownerName ?? 'the user'} through this email:",
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
                    fontSize: 16, fontWeight: FontWeight.w600),
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
                  minimumSize: const Size(200, 45),
                ),
                child: const Text("Go back to Feed"),
              ),
            ),
            const SizedBox(height: 15),
          ],
        ),
      ),
    );
  }
}
