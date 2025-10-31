import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';

class CategoryStatisticsPage extends StatefulWidget {
  const CategoryStatisticsPage({super.key});

  @override
  State<CategoryStatisticsPage> createState() => _CategoryStatisticsPageState();
}

class _CategoryStatisticsPageState extends State<CategoryStatisticsPage> {
  final _client = Supabase.instance.client;

  List<Map<String, dynamic>> _rows = const [];
  RealtimeChannel? _ch;
  Timer? _poll;

  static const _cacheKey = 'category_stats_cache';
  static const _cacheDateKey = 'category_stats_cache_date';

  @override
  void initState() {
    super.initState();
    _subscribeRealtime();
    _refresh();
    _poll = Timer.periodic(const Duration(seconds: 15), (_) => _refresh());
  }

  @override
  void dispose() {
    _poll?.cancel();
    if (_ch != null) _client.removeChannel(_ch!);
    super.dispose();
  }

  void _subscribeRealtime() {
    _ch = _client
        .channel('public:lost_items_category_stats')
        .onPostgresChanges(
          event: PostgresChangeEvent.insert,
          schema: 'public',
          table: 'lost_items',
          callback: (_) => _refresh(),
        )
        .onPostgresChanges(
          event: PostgresChangeEvent.update,
          schema: 'public',
          table: 'lost_items',
          callback: (_) => _refresh(),
        )
        .onPostgresChanges(
          event: PostgresChangeEvent.delete,
          schema: 'public',
          table: 'lost_items',
          callback: (_) => _refresh(),
        )
        .subscribe();
  }

  Future<void> _refresh() async {
    try {
      final conn = await Connectivity().checkConnectivity();
      final prefs = await SharedPreferences.getInstance();

      if (conn == ConnectivityResult.none) {
        final cached = prefs.getString(_cacheKey);
        if (cached != null && mounted) {
          setState(() =>
              _rows = List<Map<String, dynamic>>.from(json.decode(cached)));
        }
        return;
      }

      final resp = await _client.rpc('get_reports_by_category');
      final list = List<Map<String, dynamic>>.from(resp ?? []);

      await prefs.setString(_cacheKey, json.encode(list));
      await prefs.setString(_cacheDateKey, DateTime.now().toIso8601String());

      if (!mounted) return;
      setState(() => _rows = list);
    } catch (_) {
      final prefs = await SharedPreferences.getInstance();
      final cached = prefs.getString(_cacheKey);
      if (cached != null && mounted) {
        setState(() =>
            _rows = List<Map<String, dynamic>>.from(json.decode(cached)));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final rows = _rows;

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Category Statistics',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        backgroundColor: const Color(0xFF57C3C7),
        actions: [
          IconButton(onPressed: _refresh, icon: const Icon(Icons.refresh)),
        ],
      ),
      body: rows.isEmpty
          ? const Center(child: Text('No data available'))
          : ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: rows.length,
              separatorBuilder: (_, __) => const SizedBox(height: 10),
              itemBuilder: (_, i) {
                final r = rows[i];
                final cat = (r['category'] ?? '(unknown)').toString();
                final total = (r['total_reportes'] ?? 0) as int;

                return Card(
                  elevation: 2,
                  child: ListTile(
                    leading: const Icon(Icons.category, color: Colors.teal),
                    title: Text(cat,
                        style: const TextStyle(fontWeight: FontWeight.w600)),
                    trailing: Text(
                      '$total items',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ),
                );
              },
            ),
    );
  }
}
