import 'package:http/http.dart' as http;
import 'dart:convert';

class EmailService {
 
  static const String baseUrl = 'http://10.0.2.2:3000/send';

  static Future<void> sendEmail({
    required String to,
    required String subject,
    required String message,
    required String replyTo,
  }) async {
    try {
      final response = await http.post(
        Uri.parse(baseUrl),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'to': to,
          'subject': subject,
          'message': message,
          'replyTo': replyTo,
        }),
      );

      if (response.statusCode != 200) {
        throw Exception('Error al enviar correo: ${response.body}');
      }
    } catch (e) {
      print('Error en EmailService: $e');
      rethrow;
    }
  }
}
