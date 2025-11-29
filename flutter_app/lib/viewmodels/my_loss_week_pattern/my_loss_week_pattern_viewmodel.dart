import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class DayLossStat {
  final String weekdayLabel; 
  final int count;

  DayLossStat({
    required this.weekdayLabel,
    required this.count,
  });
}

class MyLossWeekPatternViewModel extends ChangeNotifier {
  final SupabaseClient client;

  MyLossWeekPatternViewModel({required this.client});

  bool isLoading = false;
  String? error;

  List<DayLossStat> stats = [];

  Future<void> load() async {
    isLoading = true;
    error = null;
    notifyListeners();

    try {
      final userId = client.auth.currentUser?.id;
      if (userId == null) {
        throw Exception('No active session');
      }

      final response = await client
          .from('lost_items')
          .select('lost_at, lost_date, created_at')
          .eq('user_id', userId);

      final rawStats =
          await compute(_computeWeekdayCounts, jsonEncode(response));

      stats = rawStats
          .map((e) => DayLossStat(
                weekdayLabel: e['weekday'] as String,
                count: e['count'] as int,
              ))
          .toList();
    } catch (e) {
      error = 'Error loading weekly pattern';
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}

List<Map<String, dynamic>> _computeWeekdayCounts(String jsonStr) {
  final list = json.decode(jsonStr) as List;

  final Map<int, int> counters = {
    1: 0, 
    2: 0,
    3: 0,
    4: 0,
    5: 0,
    6: 0,
    7: 0, 
  };

  for (final item in list) {
    DateTime? dateTime;

    if (item['lost_at'] != null) {
      dateTime = DateTime.parse(item['lost_at'] as String);
    } else if (item['lost_date'] != null) {
      dateTime = DateTime.parse(item['lost_date'] as String);
    } else if (item['created_at'] != null) {
      dateTime = DateTime.parse(item['created_at'] as String);
    }

    if (dateTime != null) {
      final weekday = dateTime.weekday; // 1..7
      counters[weekday] = (counters[weekday] ?? 0) + 1;
    }
  }

  const labels = {
    1: 'Monday',
    2: 'Tuesday',
    3: 'Wednesday',
    4: 'Thursday',
    5: 'Friday',
    6: 'Saturday',
    7: 'Sunday',
  };

  return List.generate(7, (index) {
    final day = index + 1;
    return {
      'weekday': labels[day]!,
      'count': counters[day] ?? 0,
    };
  });
}
