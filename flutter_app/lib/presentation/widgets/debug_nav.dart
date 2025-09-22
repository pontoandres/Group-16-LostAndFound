import 'package:flutter/material.dart';
import '../../routes/app_routes.dart';

class DebugNavButton extends StatelessWidget {
  const DebugNavButton({super.key});

  @override
  Widget build(BuildContext context) {
    return PopupMenuButton<String>(
      icon: const Icon(Icons.bug_report),
      onSelected: (route) => Navigator.pushNamed(context, route),
      itemBuilder: (context) => const [
        PopupMenuItem(value: AppRoutes.profile,      child: Text('Go Profile')),
        PopupMenuItem(value: AppRoutes.lostReport,   child: Text('Go Lost Report')),
        PopupMenuItem(value: AppRoutes.claim,        child: Text('Go Claim')),
        PopupMenuItem(value: AppRoutes.matchDetail,  child: Text('Go Match Detail')),
        PopupMenuItem(value: AppRoutes.notifications,child: Text('Go Notifications')),
        PopupMenuItem(value: AppRoutes.history,      child: Text('Go History')),
        PopupMenuItem(value: AppRoutes.camera,      child: Text('Go Camera')),
        PopupMenuItem(value: AppRoutes.config,      child: Text('Go Config')),
      ],
    );
  }
}
