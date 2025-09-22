import 'package:flutter/material.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../theme/app_theme.dart';

class ConfigScreen extends StatefulWidget {
  const ConfigScreen({super.key});

  @override
  State<ConfigScreen> createState() => _ConfigScreenState();
}

class _ConfigScreenState extends State<ConfigScreen> {
  bool notificationsOn = false;

  void _toggleNotifications() {
    setState(() => notificationsOn = !notificationsOn);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          notificationsOn
              ? 'Notifications turned ON'
              : 'Notifications turned OFF',
        ),
      ),
    );
  }

  void _signOut() {
    // Mock sign out
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Signed out (mock)')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const TopBar(
        title: 'Goatfound',
        actions: [DebugNavButton()],
      ),
      backgroundColor: AppTheme.tealBg,
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            const SizedBox(height: 8),
            ElevatedButton(
              onPressed: _signOut,
              style: ElevatedButton.styleFrom(
                minimumSize: const Size.fromHeight(52),
              ),
              child: const Text('Sign Out'),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _toggleNotifications,
              style: ElevatedButton.styleFrom(
                minimumSize: const Size.fromHeight(52),
                backgroundColor: AppTheme.orange,
              ),
              child: Text(
                notificationsOn
                    ? 'Turn off notifications'
                    : 'Turn on notifications',
              ),
            ),
          ],
        ),
      ),
    );
  }
}
