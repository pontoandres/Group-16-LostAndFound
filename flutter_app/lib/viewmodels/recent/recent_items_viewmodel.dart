// lib/viewmodels/recent/recent_items_viewmodel.dart
import 'dart:async';
import 'dart:io';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../../services/recent_items_service.dart';

class RecentItemsViewModel extends ChangeNotifier {
  final _service = RecentItemsService();
  final _client = Supabase.instance.client;

  bool isLoading = false;
  String? error;
  List<Map<String, dynamic>> items = [];

  Future<void> load() async {
    isLoading = true;
    error = null;
    notifyListeners();

    try {
      // 1. Siempre cargamos primero del cache local (local storage strategy)
      items = await _service.loadCached();
      notifyListeners();

      // 2. Intentamos refrescar desde Supabase si hay Internet (eventual connectivity)
      final online = await _hasInternet();
      if (!online) return;

      if (items.isEmpty) return;

      final ids = items.map((e) => e['id']).toList();
      final fresh = await _service.fetchFreshByIds(ids);

      if (fresh.isNotEmpty) {
        items = fresh;
        await _service.overwriteCache(fresh);
        notifyListeners();
      }
    } catch (e) {
      error = 'Error loading recent items: $e';
      notifyListeners();
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> _hasInternet() async {
    final status = await Connectivity().checkConnectivity();
    if (status == ConnectivityResult.none) return false;
    try {
      final result = await InternetAddress.lookup('example.com')
          .timeout(const Duration(seconds: 2));
      return result.isNotEmpty && result.first.rawAddress.isNotEmpty;
    } catch (_) {
      return false;
    }
  }
}
