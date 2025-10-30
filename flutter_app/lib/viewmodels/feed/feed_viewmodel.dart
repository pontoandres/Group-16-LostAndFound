import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

class FeedItem {
  final String id;
  final String userId;
  final String title;
  final String? location;
  final String? category;
  final String? imageUrl;
  final DateTime createdAt;
  final String? ownerName;
  final String? ownerEmail;

  FeedItem({
    required this.id,
    required this.userId,
    required this.title,
    this.location,
    this.category,
    this.imageUrl,
    required this.createdAt,
    this.ownerName,
    this.ownerEmail,
  });

  factory FeedItem.fromJson(Map<String, dynamic> json) => FeedItem(
        id: json['id'] as String,
        userId: json['user_id'] as String,
        title: json['title'] as String,
        location: json['location'] as String?,
        category: json['category'] as String?,
        imageUrl: json['image_url'] as String?,
        createdAt: DateTime.parse(json['created_at'] as String),
        ownerName: json['profiles']?['name'] ?? json['owner_name'],
        ownerEmail: json['profiles']?['email'] ?? json['owner_email'],
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'user_id': userId,
        'title': title,
        'location': location,
        'category': category,
        'image_url': imageUrl,
        'created_at': createdAt.toIso8601String(),
        'owner_name': ownerName,
        'owner_email': ownerEmail,
      };
}

class FeedViewModel extends ChangeNotifier {
  final List<FeedItem> items = [];
  bool isLoading = false;
  String? error;
  RealtimeChannel? _channel;

  Future<void> init() async {
    await load();
    _subscribeRealtime();
  }

  Future<void> load() async {
    isLoading = true;
    error = null;
    notifyListeners();

    final prefs = await SharedPreferences.getInstance();

    
    final cached = prefs.getString('feed_cache');
    if (cached != null) {
      try {
        final decoded = json.decode(cached) as List;
        items
          ..clear()
          ..addAll(decoded.map((e) => FeedItem.fromJson(e)));
        notifyListeners();
      } catch (_) {}
    }

    
    final connectivity = await Connectivity().checkConnectivity();
    if (connectivity == ConnectivityResult.none) {
      
      isLoading = false;
      notifyListeners();
      return;
    }

    // Si hay red: actualizamos desde Supabase
    try {
      final client = Supabase.instance.client;
      final res = await client
          .from('lost_items')
          .select('''
            id,user_id,title,location,category,image_url,created_at,
            profiles!lost_items_user_id_fkey(name,email)
          ''')
          .order('created_at', ascending: false);

      items
        ..clear()
        ..addAll((res as List).map((e) => FeedItem.fromJson(e)));

      await prefs.setString(
        'feed_cache',
        json.encode(items.map((e) => e.toJson()).toList()),
      );
    } catch (e) {
      error = 'Offline mode — showing saved data.';
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  void _subscribeRealtime() {
    try {
      final client = Supabase.instance.client;
      _channel?.unsubscribe();
      _channel = client.channel('public:lost_items')
        ..onPostgresChanges(
          event: PostgresChangeEvent.insert,
          schema: 'public',
          table: 'lost_items',
          callback: (payload) {
            final newRow = payload.newRecord;
            if (newRow != null) {
              items.insert(0, FeedItem.fromJson(newRow));
              notifyListeners();
            }
          },
        )
        ..subscribe();
    } catch (_) {}
  }

  @override
  void dispose() {
    _channel?.unsubscribe();
    super.dispose();
  }
}
