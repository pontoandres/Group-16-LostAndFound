// lib/main.dart
import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'routes/app_routes.dart';
import 'theme/app_theme.dart';

const bool kUseBackend = true;
const bool kStartAtLogin = true;

// Clave global para navegar desde listeners
final GlobalKey<NavigatorState> appNavigatorKey = GlobalKey<NavigatorState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  if (kUseBackend) {
    await Supabase.initialize(
      url: 'https://qpfqpjrpjqjtgerbpjtt.supabase.co',
      anonKey: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFwZnFwanJwanFqdGdlcmJwanR0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc4NzkwMTIsImV4cCI6MjA3MzQ1NTAxMn0.u-OZsQWP0AkjHcyamIwLEYZuEcLglRInjoBgkY5bb8Q',
      authOptions: const FlutterAuthClientOptions(
        authFlowType: AuthFlowType.pkce,
      ),
    );

    // Si el usuario abre el link del correo, llega este evento:
    Supabase.instance.client.auth.onAuthStateChange.listen((data) {
      final event = data.event;
      if (event == AuthChangeEvent.passwordRecovery) {
        // Navegar a la pantalla para establecer la nueva contraseña
        appNavigatorKey.currentState?.pushNamed(AppRoutes.resetPassword);
      }
    });
  }

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Lost & Found Tracker',
      theme: AppTheme.theme,
      navigatorKey: appNavigatorKey, 
      initialRoute: kStartAtLogin ? AppRoutes.login : AppRoutes.profile,
      routes: AppRoutes.routes,
    );
  }
}