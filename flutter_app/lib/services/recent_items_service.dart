// lib/services/recent_items_service.dart
import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class RecentItemsService {
  static const _cacheKey = 'recent_items_v1';
  static const _maxItems = 20;

  final _client = Supabase.instance.client;

  /// Guarda un item como "recientemente visto" en local storage.
  /// [item] debe contener al menos 'id', 'title', 'category', 'location', 'image_url'.
  Future<void> addToRecent(Map<String, dynamic> item) async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_cacheKey);
    List<dynamic> list =
        raw != null ? json.decode(raw) as List<dynamic> : <dynamic>[];

    // Evitar duplicados: quitar cualquier item con el mismo id
    list.removeWhere((e) => e['id'] == item['id']);

    // Insertar al inicio
    list.insert(0, item);

    // Limitar tamaño
    if (list.length > _maxItems) {
      list = list.sublist(0, _maxItems);
    }

    await prefs.setString(_cacheKey, json.encode(list));
  }

  /// Lee el cache local (siempre funciona, con o sin Internet).
  Future<List<Map<String, dynamic>>> loadCached() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_cacheKey);
    if (raw == null) return [];
    final list = json.decode(raw) as List<dynamic>;
    return list.cast<Map<String, dynamic>>();
  }

  /// Pide a Supabase la versión "fresca" de esos items por id.
  Future<List<Map<String, dynamic>>> fetchFreshByIds(
      List<dynamic> ids) async {
    if (ids.isEmpty) return [];
 final data = await _client
    .from('lost_items')
    .select()
    .inFilter('id', ids) // ✅ método nuevo
    .order('created_at', ascending: false);


    return (data as List).cast<Map<String, dynamic>>();
  }

  /// Reemplaza el cache local con una nueva lista.
  Future<void> overwriteCache(List<Map<String, dynamic>> items) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_cacheKey, json.encode(items));
  }
}
