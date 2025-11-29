import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../feed/feed_viewmodel.dart'; 

class MyReportsViewModel extends ChangeNotifier {
  final SupabaseClient client;
  final Connectivity connectivity;

  MyReportsViewModel({
    required this.client,
    required this.connectivity,
  });

  bool isLoading = false;
  bool isOffline = false;
  String? error;

  List<FeedItem> _items = [];
  List<FeedItem> get items => List.unmodifiable(_items);

  static const _cacheKey = 'my_reports_cache';

  Future<void> load() async {
    isLoading = true;
    error = null;
    notifyListeners();

    final prefs = await SharedPreferences.getInstance();

    final cachedJson = prefs.getString(_cacheKey);
    if (cachedJson != null) {
      try {
        final cachedItems = await compute(_decodeReports, cachedJson);
        _items = cachedItems;
        notifyListeners(); 
      } catch (_) {
      }
    }

    final status = await connectivity.checkConnectivity();
    if (status == ConnectivityResult.none) {
      isOffline = true;
      isLoading = false;
      notifyListeners();
      return;
    }

    try {
      final userId = client.auth.currentUser?.id;
      if (userId == null) {
        throw Exception('No active session');
      }

      final response = await client
          .from('lost_items')
          .select()
          .eq('user_id', userId)
          .order('created_at', ascending: false);

      final jsonList = List<Map<String, dynamic>>.from(response);
      _items = jsonList.map((e) => FeedItem.fromJson(e)).toList();

      await prefs.setString(_cacheKey, json.encode(jsonList));

      isOffline = false;
    } catch (e) {
      error = 'Error loading your reports';
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  static List<FeedItem> _decodeReports(String jsonStr) {
    final decoded = json.decode(jsonStr) as List;
    return decoded
        .map((e) => FeedItem.fromJson(e as Map<String, dynamic>))
        .toList();
  }
}
