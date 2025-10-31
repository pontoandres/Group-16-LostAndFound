import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'routes/app_routes.dart';
import 'theme/app_theme.dart';

const bool kUseBackend = true;
final GlobalKey<NavigatorState> appNavigatorKey = GlobalKey<NavigatorState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  String initialRoute = AppRoutes.login;

  if (kUseBackend) {
    await Supabase.initialize(
      url: 'https://qpfqpjrpjqjtgerbpjtt.supabase.co',
      anonKey:
          'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFwZnFwanJwanFqdGdlcmJwanR0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc4NzkwMTIsImV4cCI6MjA3MzQ1NTAxMn0.u-OZsQWP0AkjHcyamIwLEYZuEcLglRInjoBgkY5bb8Q',
      authOptions: const FlutterAuthClientOptions(
        authFlowType: AuthFlowType.pkce,
      ),
    );

    final connectivity = await Connectivity().checkConnectivity();

    if (connectivity != ConnectivityResult.none) {
      final session = Supabase.instance.client.auth.currentSession;
      if (session != null) {
        debugPrint('Sesión activa encontrada');
        initialRoute = AppRoutes.feed;
      } else {
        debugPrint('No hay sesión activa, se requerirá login');
        initialRoute = AppRoutes.login;
      }
    } else {
      debugPrint('Sin conexión: iniciando en modo offline');
      initialRoute = AppRoutes.feed;
    }

    Supabase.instance.client.auth.onAuthStateChange.listen((data) {
      final event = data.event;
      if (event == AuthChangeEvent.passwordRecovery) {
        appNavigatorKey.currentState?.pushNamed(AppRoutes.resetPassword);
      }
    });
  }

  runApp(MyApp(initialRoute: initialRoute));
}

class MyApp extends StatelessWidget {
  final String initialRoute;
  const MyApp({super.key, required this.initialRoute});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Lost & Found Tracker',
      theme: AppTheme.theme,
      navigatorKey: appNavigatorKey,
      initialRoute: initialRoute,
      routes: AppRoutes.routes,
    );
  }
}
