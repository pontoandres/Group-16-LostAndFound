

import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class ReportsByFacultyPage extends StatefulWidget {
  const ReportsByFacultyPage({super.key});

  @override
  State<ReportsByFacultyPage> createState() => _ReportsByFacultyPageState();
}

class _ReportsByFacultyPageState extends State<ReportsByFacultyPage> {
  final _client = Supabase.instance.client;
  List<Map<String, dynamic>> _results = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _loading = true;
      _error = null;
    });

    try {
      final response = await _client.rpc('get_reports_by_faculty');

      print('RPC response: $response'); // Para debug

      if (response != null && response is List) {
        setState(() {
          _results = List<Map<String, dynamic>>.from(response);
        });
      } else {
        setState(() {
          _results = [];
        });
      }
    } catch (e) {
      setState(() => _error = 'Unexpected error: $e');
    } finally {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Faculty Statistics')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text('Error: $_error'))
              : _results.isEmpty
                  ? const Center(child: Text('No data'))
                  : ListView.separated(
                      padding: const EdgeInsets.all(16),
                      itemCount: _results.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 10),
                      itemBuilder: (_, index) {
                        final row = _results[index];
                        return Card(
                          child: ListTile(
                            leading: const Icon(Icons.school),
                            title: Text(row['faculty'] ?? 'Unknown'),
                            trailing: Text('${row['total_reportes']} items'),
                          ),
                        );
                      },
                    ),
    );
  }
}
