import 'dart:async';
import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

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
        final response = await _client.rpc('get_reports_by_faculty');
        yield List<Map<String, dynamic>>.from(response ?? []);
      } catch (e) {
        yield [];
      }
      await Future.delayed(const Duration(seconds: 10)); // actualización periodica
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Faculty Statistics')),
      //StreamBuilder
      body: StreamBuilder<List<Map<String, dynamic>>>(
        stream: _facultyReportsStream(),
        builder: (context, snapshot) {
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }

          final data = snapshot.data!;
          if (data.isEmpty) {
            return const Center(child: Text('No data available'));
          }

          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: data.length,
            separatorBuilder: (_, __) => const SizedBox(height: 10),
            itemBuilder: (_, index) {
              final row = data[index];
              return Card(
                child: ListTile(
                  leading: const Icon(Icons.school),
                  title: Text(row['faculty'] ?? 'Unknown'),
                  trailing: Text('${row['total_reportes']} items'),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
