import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

class ReportsByHourPage extends StatefulWidget {
  const ReportsByHourPage({super.key});

  @override
  _ReportsByHourPageState createState() => _ReportsByHourPageState();
}

class _ReportsByHourPageState extends State<ReportsByHourPage> {
  final supabase = Supabase.instance.client;
  static const _cacheKey = 'reports_by_hour_cache';

  Future<List<Map<String, dynamic>>> _loadData() async {
    final prefs = await SharedPreferences.getInstance();
    final connectivity = await Connectivity().checkConnectivity();

    // Si no hay conexión, tratar de leer del caché
    if (connectivity == ConnectivityResult.none) {
      final cached = prefs.getString(_cacheKey);
      if (cached != null) {
        return List<Map<String, dynamic>>.from(json.decode(cached));
      } else {
        throw Exception('No internet and no cached data');
      }
    }

    // Si hay conexión, llamar al RPC y guardar en caché
    final data = await supabase.rpc('get_reports_by_hour');
    final list = List<Map<String, dynamic>>.from(data);
    await prefs.setString(_cacheKey, json.encode(list));
    return list;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Reports by Hour"),
        backgroundColor: const Color.fromARGB(255, 87, 195, 199),
      ),
      body: FutureBuilder(
        future: _loadData(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            return Center(child: Text("Error: ${snapshot.error}"));
          }

          final data = snapshot.data as List<Map<String, dynamic>>;
          if (data.isEmpty) {
            return const Center(child: Text("No reports found"));
          }

          return ListView.builder(
            padding: const EdgeInsets.all(12),
            itemCount: data.length,
            itemBuilder: (context, index) {
              final row = data[index];
              final hour = row['hour_of_day'];
              final total = row['total_reports'];

              return Card(
                child: ListTile(
                  leading: const Icon(Icons.access_time),
                  title: Text("$hour:00 hrs"),
                  subtitle: Text("Reports: $total"),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
