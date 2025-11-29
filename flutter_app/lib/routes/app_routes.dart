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
import '../presentation/camera/camera_screen.dart';
import '../presentation/config/config_screen.dart';
import '../presentation/reset_password/reset_password_page.dart';

import '../presentation/statistics/reports_by_faculty_page.dart';
import '../presentation/statistics/category_statistics_page.dart';
import '../presentation/statistics/password_changes_by_faculty_page.dart';
import '../presentation/statistics/reports_by_hour_page.dart';

import '../viewmodels/feed/feed_viewmodel.dart'; 

class AppRoutes {
  static const String login = '/login';
  static const String register = '/register';
  static const String forgotPassword = '/forgot_password';
  static const String feed = '/feed';
  static const String profile = '/profile';
  static const String lostReport = '/lost_report';
  static const String claim = '/claim';

  static const String matchDetail = '/match_detail';

  static const String notifications = '/notifications';
  static const String history = '/history';
  static const String camera = '/camera';
  static const String config = '/config';
  static const String resetPassword = '/reset_password';

  static const String reportsByFaculty = '/reports_by_faculty';
  static const String categoryStats = '/category_stats';
  static const String passwordChangesByFaculty =
      '/password_changes_by_faculty';
  static const String reportsByHour = '/reports_by_hour';

  static Map<String, WidgetBuilder> get routes {
    return {
      login: (_) => const LoginPage(),
      register: (_) => const RegisterPage(),
      forgotPassword: (_) => const ForgotPasswordPage(),
      feed: (_) => FeedPage(),
      profile: (_) => const ProfileScreen(),
      lostReport: (_) => const LostReportScreen(),
      claim: (_) => const ClaimObjectScreen(),
      notifications: (_) => const NotificationsScreen(),
      history: (_) => const HistoryScreen(),
      camera: (_) => const CameraScreen(),
      config: (_) => const ConfigScreen(),
      resetPassword: (_) => const ResetPasswordPage(),
      reportsByFaculty: (_) => const ReportsByFacultyPage(),
      categoryStats: (_) => const CategoryStatisticsPage(),
      passwordChangesByFaculty: (_) =>
          const PasswordChangesByFacultyPage(),
      reportsByHour: (_) => const ReportsByHourPage(),
    };
  }

  static Route<dynamic> generateRoute(RouteSettings settings) {
    switch (settings.name) {
      case matchDetail:
        final item = settings.arguments as FeedItem;
        return MaterialPageRoute(
          builder: (_) => ItemDescriptionScreen(item: item),
        );

      default:
        return MaterialPageRoute(
          builder: (_) => const Scaffold(
            body: Center(child: Text("Route not found")),
          ),
        );
    }
  }
}
