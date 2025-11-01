import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

class PasswordChangesByFacultyPage extends StatefulWidget {
  const PasswordChangesByFacultyPage({super.key});

  @override
  State<PasswordChangesByFacultyPage> createState() =>
      _PasswordChangesByFacultyPageState();
}

class _PasswordChangesByFacultyPageState
    extends State<PasswordChangesByFacultyPage> {
  final _client = Supabase.instance.client;


  static const _cacheKey = 'pwd_requests_by_faculty_cache';
  static const _cacheDateKey = 'pwd_requests_by_faculty_cache_date';

  Stream<List<Map<String, dynamic>>> _requestsStream() async* {
    while (true) {
      try {
        final connectivity = await Connectivity().checkConnectivity();
        final prefs = await SharedPreferences.getInstance();

        if (connectivity == ConnectivityResult.none) {

          final cached = prefs.getString(_cacheKey);
          if (cached != null) {
            final decoded =
                List<Map<String, dynamic>>.from(json.decode(cached));
            yield decoded;
          } else {
            yield <Map<String, dynamic>>[];
          }
        } else {

          final response =
              await _client.rpc('get_password_requests_by_faculty');
          final data = List<Map<String, dynamic>>.from(response ?? []);

          
          await prefs.setString(_cacheKey, json.encode(data));
          await prefs.setString(
              _cacheDateKey, DateTime.now().toIso8601String());

          yield data;
        }
      } catch (e) {
        
        final prefs = await SharedPreferences.getInstance();
        final cached = prefs.getString(_cacheKey);
        if (cached != null) {
          yield List<Map<String, dynamic>>.from(json.decode(cached));
        } else {
          yield <Map<String, dynamic>>[];
        }
      }

      await Future.delayed(const Duration(seconds: 15));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
        
          'Password Requests by Faculty',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        backgroundColor: const Color(0xFF57C3C7),
      ),
      body: StreamBuilder<List<Map<String, dynamic>>>(
        stream: _requestsStream(),
        builder: (context, snapshot) {
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }

          final rows = snapshot.data!;
          if (rows.isEmpty) {
            return const Center(child: Text('No data available'));
          }

          
          final top = rows.first;
          final topFaculty =
              (top['faculty'] as String?)?.toString() ?? '(unknown)';
          final topTotal = (top['total_requests'] as num?)?.toInt() ?? 0;

          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              
              Card(
                color: const Color(0xFFE6F6F6),
                elevation: 2,
                child: ListTile(
                  leading: const Icon(Icons.emoji_events, color: Colors.teal),
                  title: Text(
                    'Top faculty: $topFaculty',
                    style: const TextStyle(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  subtitle: const Text('Most password recovery requests'),
                  trailing: Text(
                    '$topTotal requests',
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 12),

             
              ...rows.map((row) {
                final f =
                    (row['faculty'] as String?)?.toString() ?? '(unknown)';
                final t = (row['total_requests'] as num?)?.toInt() ?? 0;
                return Card(
                  elevation: 1.5,
                  child: ListTile(
                    leading: const Icon(Icons.mark_email_read_outlined,
                        color: Colors.teal),
                    title: Text(
                      f,
                      style: const TextStyle(fontWeight: FontWeight.w600),
                    ),
                    trailing: Text(
                      '$t requests',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ),
                );
              }),
            ],
          );
        },
      ),
    );
  }
}
