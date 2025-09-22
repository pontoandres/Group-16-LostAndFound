import 'package:flutter/material.dart';
import '../../theme/app_theme.dart';

class TopBar extends StatelessWidget implements PreferredSizeWidget {
  final String title;
  final bool showBack;
  final List<Widget>? actions;

  const TopBar({
    super.key,
    required this.title,
    this.showBack = true,
    this.actions,
  });

  @override
  Widget build(BuildContext context) {
    return AppBar(
      backgroundColor: AppTheme.tealBg,
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w800)),
      centerTitle: true,
      leading: IconButton(icon: const Icon(Icons.menu_rounded), onPressed: () {}),
      actions: actions,
    );
  }

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);
}
