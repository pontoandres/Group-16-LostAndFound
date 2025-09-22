import 'package:flutter/material.dart';

class AppTheme {
  static const Color tealBg = Color(0xFFAECED2);
  static const Color cardBg = Color(0xFFFFFFFF);
  static const Color orange = Color(0xFFD99348);
  static const Color orangeShadow = Color(0xFF7C5A2A);
  static const Color pill = Color(0xFFE7EFEF);

  static ThemeData theme = ThemeData(
    useMaterial3: true,
    scaffoldBackgroundColor: tealBg,
    appBarTheme: const AppBarTheme(
      backgroundColor: tealBg,
      foregroundColor: Colors.black,
      centerTitle: true,
      elevation: 0,
      titleTextStyle: TextStyle(fontSize: 22, fontWeight: FontWeight.w800),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: pill,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(18),
        borderSide: BorderSide.none,
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        backgroundColor: orange,
        foregroundColor: Colors.black,
        minimumSize: const Size.fromHeight(48),
        textStyle: const TextStyle(fontWeight: FontWeight.w800),
        elevation: 6,
        shadowColor: orangeShadow,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: Colors.black,
        side: const BorderSide(color: Colors.black54),
        minimumSize: const Size.fromHeight(48),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      ),
    ),
    // ⬇️ En versiones nuevas es CardThemeData
    cardTheme: const CardThemeData(
      color: cardBg,
      elevation: 2,
      margin: EdgeInsets.zero,
    ),
  );
}
