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
  bool offlineMode = false;

  Future<List<Map<String, dynamic>>> _loadData() async {
    final prefs = await SharedPreferences.getInstance();
    final connectivity = await Connectivity().checkConnectivity();

    if (connectivity == ConnectivityResult.none) {
      final cached = prefs.getString(_cacheKey);
      offlineMode = true;

      if (cached != null) {
        return List<Map<String, dynamic>>.from(json.decode(cached));
      } else {
        return []; 
      }
    }

    try {
     
      final data = await supabase.rpc('get_reports_by_hour');
      final list = List<Map<String, dynamic>>.from(data);

      await prefs.setString(_cacheKey, json.encode(list));
      offlineMode = false;

      return list;
    } catch (e) {
      
      final cached = prefs.getString(_cacheKey);
      if (cached != null) {
        offlineMode = true;
        return List<Map<String, dynamic>>.from(json.decode(cached));
      }
      rethrow;
    }
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

          if (!snapshot.hasData || (snapshot.data as List).isEmpty) {
            return Center(
              child: Text(
                offlineMode
                    ? "No hay internet y no existe información guardada."
                    : "No se encontraron reportes.",
              ),
            );
          }

          final data = snapshot.data as List<Map<String, dynamic>>;

          return Column(
            children: [
             
              if (offlineMode)
                Container(
                  width: double.infinity,
                  color: Colors.orange.shade200,
                  padding: const EdgeInsets.all(8),
                  child: const Text(
                    "Modo sin internet",
                    textAlign: TextAlign.center,
                  ),
                ),

              Expanded(
                child: ListView.builder(
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
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
