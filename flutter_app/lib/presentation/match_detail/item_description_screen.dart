import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../theme/app_theme.dart';
import '../../viewmodels/feed/feed_viewmodel.dart';
import '../../routes/app_routes.dart';
import '../../services/recent_items_service.dart';

class ItemDescriptionScreen extends StatelessWidget {
  final FeedItem item;

  const ItemDescriptionScreen({
    super.key,
    required this.item,
  });


  @override
  Widget build(BuildContext context) {
    final createdAtText = 'Posted on: ${item.createdAt.toLocal()}';
     Future.microtask(() {
      RecentItemsService().addToRecent({
        'id': item.id,
        'title': item.title,
        'category': item.category,
        'location': item.location,
        'image_url': item.imageUrl,
        'created_at': item.createdAt.toIso8601String(),
      });
    });
    return Scaffold(
      appBar: const TopBar(
        title: 'Goatfound',
        actions: [DebugNavButton()],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Text(
                    item.title,
                    style: const TextStyle(
                      fontSize: 28,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
                ElevatedButton(
                  onPressed: () async {
                    final prefs = await SharedPreferences.getInstance();
                    final likedItems = prefs.getStringList('liked_items') ?? [];

                    if (!likedItems.contains(item.id)) {
                      likedItems.add(item.id);
                      await prefs.setStringList('liked_items', likedItems);

                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text("Item added to Likes")),
                        );
                      }
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.red.shade300,
                    minimumSize: const Size(64, 36),
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: const Text("Like", style: TextStyle(color: Colors.white)),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(createdAtText),
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
                    : Image.asset('assets/images/Rectangle17.png',
                        fit: BoxFit.cover),
              ),
            ),
            const SizedBox(height: 16),
            const Text(
              'Description',
              style: TextStyle(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppTheme.orange.withOpacity(.6),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Text(
                item.category ?? 'No description available',
                textAlign: TextAlign.center,
              ),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                Navigator.pushNamed(
                  context,
                  AppRoutes.claim,
                  arguments: item,
                );
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: AppTheme.orange,
                foregroundColor: Colors.black,
              ),
              child: const Text('Contact'),
            ),
          ],
        ),
      ),
    );
  }
}
