import 'package:flutter/material.dart';


import '../presentation/login/login_page.dart';
import '../presentation/register/register_page.dart';
import '../presentation/forgot_password/forgot_password.dart';
import '../presentation/feed/feed_page.dart';
import '../presentation/profile/profile_screen.dart';
import '../presentation/lost_report/lost_report_screen.dart';
import '../presentation/claim/claim_object_screen.dart';
import '../presentation/match_detail/item_description_screen.dart';
import '../presentation/notifications/notifications_screen.dart';
import '../presentation/history/history_screen.dart';
class AppRoutes {
  static const String login = '/login';
  static const String register = '/register';
  static const String forgotPassword = '/forgot_password';
  static const String feed = '/feed'; 
  static const String profile = '/profile';
  static const String lostReport = '/lost_report';
  static const String foundReport = '/found_report';
  static const String claim = '/claim';
  static const String matchDetail = '/match_detail';
  static const String notifications = '/notifications';
  static const String history = '/history';

  static Map<String, WidgetBuilder> get routes {
    return {
      login: (_) => const LoginPage(),
      register: (_) => const RegisterPage(),
      forgotPassword: (_) => const ForgotPasswordPage(),
      feed: (_) => FeedPage(),
      profile: (_) => const ProfileScreen(),
      lostReport: (_) => const LostReportScreen(),
      claim: (_) => const ClaimObjectScreen(),
      matchDetail: (_) => const ItemDescriptionScreen(),
          notifications: (_) => const NotificationsScreen(),
          history: (_) => const HistoryScreen()

    };
  }
}
