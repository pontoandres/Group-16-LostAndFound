import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'presentation/login/login_page.dart'; // Importa la pantalla de login
import 'routes/app_routes.dart';            // Importa las rutas de navegación

// Punto de entrada principal de la aplicación
Future<void> main() async {
  // Asegura que el binding esté inicializado antes de usar plugins asincrónicos
  WidgetsFlutterBinding.ensureInitialized();

  // hay que ocupatar esto con el tiempo
  await Supabase.initialize(
    url: 'https://qpfqpjrpjqjtgerbpjtt.supabase.co', 
    anonKey: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFwZnFwanJwanFqdGdlcmJwanR0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc4NzkwMTIsImV4cCI6MjA3MzQ1NTAxMn0.u-OZsQWP0AkjHcyamIwLEYZuEcLglRInjoBgkY5bb8Q',         
  );

  // Ejecuta la app principal
  runApp(const MyApp());
}

// Widget principal de la app
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,   // Quita el banner de "DEBUG" en pantalla
      title: 'Lost & Found Tracker',       // Título de la app
      home: const LoginPage(),             // Pantalla inicial: Login de momento incia desde home
      routes: AppRoutes.routes,            // Mapa de rutas definidas
    );
  }
}
