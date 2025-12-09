import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:provider/provider.dart';
import 'dart:convert';
import 'package:cached_network_image/cached_network_image.dart'; // ← clave para offline

import '../../viewmodels/feed/feed_viewmodel.dart';
import '../../theme/app_theme.dart';
import '../../models/lost_item.dart';
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

    // Buscar datos si están en memoria del FeedViewModel
    final allItems = Provider.of<FeedViewModel>(context, listen: false).items;

    if (allItems.isNotEmpty) {
      setState(() {
        likedItems = allItems.where((item) => likedIds.contains(item.id)).toList();
        isLoading = false;
      });
    } else {
      // Modo offline → Intentar leer desde caché
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
                            ? ClipRRect(
                                borderRadius: BorderRadius.circular(8),
                                child: CachedNetworkImage(
                                  imageUrl: item.imageUrl!,
                                  width: 56,
                                  height: 56,
                                  fit: BoxFit.cover,

                                  // Mientras carga (o si es primera vez)
                                  placeholder: (context, url) => const SizedBox(
                                      width: 30,
                                      height: 30,
                                      child: CircularProgressIndicator(strokeWidth: 2)
                                  ),

                                  // Si falla o no hay conexión
                                  errorWidget: (context, url, error) =>
                                      const Icon(Icons.broken_image),
                                ),
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
