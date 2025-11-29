import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:provider/provider.dart';
import 'dart:convert';

import '../../viewmodels/feed/feed_viewmodel.dart';
import '../../theme/app_theme.dart';
import '../../models/lost_item.dart'; // Asegúrate de importar FeedItem si no lo tienes
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';

class LikedItemsScreen extends StatefulWidget {
  const LikedItemsScreen({super.key});

  @override
  State<LikedItemsScreen> createState() => _LikedItemsScreenState();
}

class _LikedItemsScreenState extends State<LikedItemsScreen> {
  List<FeedItem> likedItems = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadLikedItems();
  }

  Future<void> _loadLikedItems() async {
    final prefs = await SharedPreferences.getInstance();
    final likedIds = prefs.getStringList('liked_items') ?? [];

    // Intenta obtener los items desde FeedViewModel
    final allItems = Provider.of<FeedViewModel>(context, listen: false).items;

    if (allItems.isNotEmpty) {
      setState(() {
        likedItems = allItems.where((item) => likedIds.contains(item.id)).toList();
        isLoading = false;
      });
    } else {
      // Si no hay items en memoria (por ejemplo, en modo offline), intenta desde caché
      final cached = prefs.getString('feed_cache');
      if (cached != null) {
        try {
          final decoded = json.decode(cached) as List;
          final fromCache = decoded.map((e) => FeedItem.fromJson(e)).toList();

          setState(() {
            likedItems = fromCache.where((item) => likedIds.contains(item.id)).toList();
            isLoading = false;
          });
        } catch (_) {
          setState(() {
            likedItems = [];
            isLoading = false;
          });
        }
      } else {
        // No hay nada en memoria ni en caché
        setState(() {
          likedItems = [];
          isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const TopBar(title: 'Liked Items', actions: [DebugNavButton()]),
      body: isLoading
          ? const Center(child: CircularProgressIndicator())
          : likedItems.isEmpty
              ? const Center(child: Text('No liked items yet.'))
              : ListView.builder(
                  padding: const EdgeInsets.all(12),
                  itemCount: likedItems.length,
                  itemBuilder: (context, index) {
                    final item = likedItems[index];
                    return Card(
                      child: ListTile(
                        leading: item.imageUrl != null
                            ? Image.network(
                                item.imageUrl!,
                                width: 56,
                                height: 56,
                                fit: BoxFit.cover,
                              )
                            : const Icon(Icons.broken_image),
                        title: Text(
                          item.title,
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                        subtitle: Text(item.category ?? "No description"),
                        trailing: const Icon(Icons.favorite, color: Colors.red),
                        onTap: () {
                          Navigator.pushNamed(context, '/match_detail', arguments: item);
                        },
                      ),
                    );
                  },
                ),
    );
  }
}
