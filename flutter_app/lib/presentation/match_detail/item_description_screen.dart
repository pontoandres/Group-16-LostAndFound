import 'package:flutter/material.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../theme/app_theme.dart';
import '../../viewmodels/feed/feed_viewmodel.dart';
import '../../routes/app_routes.dart';

class ItemDescriptionScreen extends StatelessWidget {
  const ItemDescriptionScreen({super.key});

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
      appBar: const TopBar(title: 'Goatfound', actions: [DebugNavButton()]),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            const SizedBox(height: 8),
            Center(
              child: Text(
                item.title,
                style: const TextStyle(fontSize: 28, fontWeight: FontWeight.w900),
              ),
            ),
            const SizedBox(height: 4),
            Text("Posted on: ${item.createdAt.toLocal()}"),
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
                child: item.imageUrl != null && item.imageUrl!.isNotEmpty
                    ? Image.network(item.imageUrl!, fit: BoxFit.cover)
                    : Image.asset('assets/images/Rectangle17.png', fit: BoxFit.cover),
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
              child: Text(
                item.category ?? "No description available",
                textAlign: TextAlign.center,
              ),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                Navigator.pushNamed(context, AppRoutes.claim, arguments: item);
              },
              child: const Text('Claim Object'),
            ),
          ],
        ),
      ),
    );
  }
}


