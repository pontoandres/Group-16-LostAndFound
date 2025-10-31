import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

class ReportsByFacultyPage extends StatefulWidget {
  const ReportsByFacultyPage({super.key});

  @override
  State<ReportsByFacultyPage> createState() => _ReportsByFacultyPageState();
}

class _ReportsByFacultyPageState extends State<ReportsByFacultyPage> {
  final _client = Supabase.instance.client;

  Stream<List<Map<String, dynamic>>> _facultyReportsStream() async* {
    while (true) {
      try {
        final connectivity = await Connectivity().checkConnectivity();
        final prefs = await SharedPreferences.getInstance();

        debugPrint('Estado de red: $connectivity');

        if (connectivity == ConnectivityResult.none) {
          final cached = prefs.getString('faculty_cache');
          if (cached != null) {
            debugPrint('Usando datos desde caché local');
            final decoded =
                List<Map<String, dynamic>>.from(json.decode(cached));
            yield decoded;
          } else {
            debugPrint('No hay datos en caché');
            yield [];
          }
        } else {
          debugPrint('Consultando Supabase RPC...');
          final response = await _client.rpc('get_reports_by_faculty');
          debugPrint('Tipo de respuesta RPC: ${response.runtimeType}');
          debugPrint('Contenido bruto RPC: $response');

          List<Map<String, dynamic>> data = [];
          if (response is List) {
            data = List<Map<String, dynamic>>.from(response);
          } else if (response is Map && response.containsKey('data')) {
            data = List<Map<String, dynamic>>.from(response['data']);
          } else {
            debugPrint('Formato inesperado de respuesta RPC');
          }

          debugPrint('Datos parseados (${data.length} filas): $data');

          await prefs.setString('faculty_cache', json.encode(data));
          await prefs.setString(
              'faculty_cache_date', DateTime.now().toIso8601String());

          yield data;
        }
      } catch (e) {
        debugPrint('Error en el stream: $e');
        final prefs = await SharedPreferences.getInstance();
        final cached = prefs.getString('faculty_cache');
        if (cached != null) {
          debugPrint('Recuperando datos desde caché tras error');
          yield List<Map<String, dynamic>>.from(json.decode(cached));
        } else {
          debugPrint('Sin caché disponible');
          yield [];
        }
      }

      debugPrint('Esperando 15 segundos para siguiente actualización');
      await Future.delayed(const Duration(seconds: 15));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Faculty Statistics',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        backgroundColor: const Color(0xFF57C3C7),
      ),
      body: StreamBuilder<List<Map<String, dynamic>>>(
        stream: _facultyReportsStream(),
        builder: (context, snapshot) {
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }

          final data = snapshot.data!;
          debugPrint('Snapshot actualizado: ${data.length} registros');

          if (data.isEmpty) {
            return const Center(child: Text('No data available'));
          }

          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: data.length,
            separatorBuilder: (_, __) => const SizedBox(height: 10),
            itemBuilder: (_, index) {
              final row = data[index];
              debugPrint('Renderizando fila $index: $row');

              return Card(
                elevation: 2,
                child: ListTile(
                  leading: const Icon(Icons.school, color: Colors.teal),
                  title: Text(
                    row['faculty'] ?? 'Unknown',
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                  trailing: Text(
                    '${row['total_reportes']} items',
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
