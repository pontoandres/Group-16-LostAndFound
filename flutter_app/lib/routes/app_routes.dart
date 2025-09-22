import 'package:flutter/material.dart';
import '../presentation/login/login_page.dart';
import '../presentation/register/register_page.dart';
import '../presentation/forgot_password/forgot_password.dart';
import '../presentation/feed/feed_page.dart';
import '../presentation/lost_report/lost_report_page.dart';
import '../presentation/found_report/found_report_page.dart';
import '../presentation/match_detail/match_detail_page.dart';

class AppRoutes {
  // Definimos nombres de rutas como constantes
  static const String login = '/login';
  static const String register = '/register';
  static const String forgot_password = '/forgot_password';
  static const String feed = '/feed';
  static const String lostReport = '/lost_report';
  static const String foundReport = '/found_report';
  static const String matchDetail = '/match_detail';

  // Mapeo entre nombres de rutas y pantallas
  static Map<String, WidgetBuilder> get routes {
    return {
      login: (context) => const LoginPage(),
      register: (context) => const RegisterPage(),
      forgot_password: (context) => const ForgotPasswordPage(),
      
      //feed: (context) => const FeedPage(),
      //lostReport: (context) => const LostReportPage(),
      //foundReport: (context) => const FoundReportPage(),
      //matchDetail: (context) => const MatchDetailPage(),
    };
  }
}