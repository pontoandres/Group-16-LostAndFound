import 'package:flutter/material.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../theme/app_theme.dart';

class HistoryScreen extends StatelessWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final List<Map<String, String>> history = [
      {
        "title": "Claim Request was verified",
        "date": "12/09/2025 at 1:43 PM",
        "detail": "Umbrella found by Martin",
      },
      {
        "title": "Claim Request sent",
        "date": "10/09/2025 at 5:43 PM",
        "detail": "Umbrella found by Martin\nlink to post",
      },
      {
        "title": "Found Object posted",
        "date": "23/07/2025 at 5:43 PM",
        "detail": "Water bottle found\nlink to post",
      },
    ];

    return Scaffold(
      appBar: const TopBar(
        title: 'History',
        actions: [DebugNavButton()], 
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemBuilder: (context, i) {
          final h = history[i];
          return Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: AppTheme.pill,
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.08),
                  blurRadius: 6,
                  offset: const Offset(0, 3),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  h["title"] ?? "",
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 15,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  h["date"] ?? "",
                  style: const TextStyle(fontSize: 12),
                ),
                const SizedBox(height: 6),
                Text(
                  h["detail"] ?? "",
                  style: const TextStyle(fontSize: 13),
                ),
              ],
            ),
          );
        },
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemCount: history.length,
      ),
      backgroundColor: AppTheme.tealBg,
    );
  }
}
