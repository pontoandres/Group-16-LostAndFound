import 'package:flutter/material.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../theme/app_theme.dart';

class NotificationsScreen extends StatelessWidget {
  const NotificationsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final List<Map<String, dynamic>> notifications = [
      {"msg": "You have a new email!", "highlight": true},
      {"msg": "Post successfully created", "highlight": false},
      {"msg": "Object claimed!", "highlight": false},
      {"msg": "You have a new email!", "highlight": false},
    ];

    return Scaffold(
      appBar: const TopBar(
        title: 'Notifications',
        actions: [DebugNavButton()], 
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemBuilder: (context, i) {
          final n = notifications[i];
          return Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: n["highlight"] == true ? AppTheme.orange : Colors.white,
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.08),
                  blurRadius: 6,
                  offset: const Offset(0, 3),
                ),
              ],
            ),
            child: Text(
              n["msg"] as String,
              style: const TextStyle(
                fontWeight: FontWeight.w600,
                fontSize: 16,
              ),
            ),
          );
        },
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemCount: notifications.length,
      ),
      backgroundColor: AppTheme.tealBg,
    );
  }
}
