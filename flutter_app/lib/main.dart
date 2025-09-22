import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'routes/app_routes.dart';   // Mapa de rutas
import 'theme/app_theme.dart';     // Tema visual

/// === Flags de configuración ===
/// Pon en false si quieres correr solo el front (sin backend)
const bool kUseBackend = true;

/// Pantalla inicial: true => Login, false => Profile (cámbialo a lo que necesites)
const bool kStartAtLogin = true;

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  if (kUseBackend) {
    await Supabase.initialize(
      url: 'https://qpfqpjrpjqjtgerbpjtt.supabase.co',
      anonKey:
          'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFwZnFwanJwanFqdGdlcmJwanR0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc4NzkwMTIsImV4cCI6MjA3MzQ1NTAxMn0.u-OZsQWP0AkjHcyamIwLEYZuEcLglRInjoBgkY5bb8Q',
    );
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
      // Elegimos la ruta inicial según el flag
      initialRoute: kStartAtLogin ? AppRoutes.login : AppRoutes.profile,
      // Todas las rutas centralizadas en AppRoutes
      routes: AppRoutes.routes,
    );
  }
}
