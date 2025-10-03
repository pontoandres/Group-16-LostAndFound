import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class LostReportViewModel extends ChangeNotifier {
  // --- Controllers para el formulario ---
  final titleCtrl = TextEditingController();
  final descriptionCtrl = TextEditingController();
  final locationCtrl = TextEditingController();
  final categoryCtrl = TextEditingController();

  // --- Estado ---
  Uint8List? imageBytes;         // para el preview
  String? imageUrl;              // url pública en storage
  DateTime? lostAt = DateTime.now();

  bool isLoading = false;
  String? error;

  final SupabaseClient _client = Supabase.instance.client;

  // -----------------------------------------------
  // Seleccionar imagen (galería o cámara)
  // -----------------------------------------------
  Future<void> pickImage({ImageSource source = ImageSource.gallery}) async {
    try {
      final picker = ImagePicker();
      final XFile? file = await picker.pickImage(
        source: source,
        imageQuality: 85,
        maxWidth: 1600,
      );
      if (file == null) return;

      imageBytes = await file.readAsBytes();
      notifyListeners();
    } catch (e) {
      error = 'No se pudo seleccionar la imagen';
      notifyListeners();
    }
  }

  // -----------------------------------------------
  // Subir imagen al Storage y devolver URL pública
  // -----------------------------------------------
  Future<String?> _uploadImage({
    required Uint8List bytes,
    required String userId,
  }) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    final path = 'lost/$userId/$now.jpg';

    await _client.storage
        .from('lost-items')
        .uploadBinary(path, bytes, fileOptions: const FileOptions(
          contentType: 'image/jpeg',
          upsert: true,
        ));

    // URL pública
    return _client.storage.from('lost-items').getPublicUrl(path);
  }

  // -----------------------------------------------
  // Cambiar la fecha en el formulario
  // -----------------------------------------------
  void setLostAt(DateTime d) {
    lostAt = d;
    notifyListeners();
  }

  // -----------------------------------------------
  // Enviar el reporte (insert en lost_items)
  // -----------------------------------------------
  Future<bool> submit() async {
    final user = _client.auth.currentUser;
    if (user == null) {
      error = 'No hay sesión activa';
      notifyListeners();
      return false;
    }

    final title = titleCtrl.text.trim();
    if (title.isEmpty) {
      error = 'El título es obligatorio';
      notifyListeners();
      return false;
    }

    try {
      isLoading = true;
      error = null;
      notifyListeners();

      // Subir imagen si existe
      String? url;
      if (imageBytes != null) {
        url = await _uploadImage(bytes: imageBytes!, userId: user.id);
      }

      // Insertar registro
      await _client.from('lost_items').insert({
        'user_id': user.id,
        'title': title,
        'description': descriptionCtrl.text.trim().isEmpty
            ? null
            : descriptionCtrl.text.trim(),
        'location': locationCtrl.text.trim().isEmpty
            ? null
            : locationCtrl.text.trim(),
        'category': categoryCtrl.text.trim().isEmpty
            ? null
            : categoryCtrl.text.trim(),
        'image_url': url,
        'lost_at': (lostAt ?? DateTime.now()).toIso8601String(),
      });

      // Limpiar formulario
      _clear();
      return true;
    } on PostgrestException catch (e) {
      error = e.message;
      return false;
    } catch (e) {
      error = 'Error inesperado: $e';
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  void _clear() {
    titleCtrl.clear();
    descriptionCtrl.clear();
    locationCtrl.clear();
    categoryCtrl.clear();
    imageBytes = null;
    imageUrl = null;
    lostAt = DateTime.now();
  }

  @override
  void dispose() {
    titleCtrl.dispose();
    descriptionCtrl.dispose();
    locationCtrl.dispose();
    categoryCtrl.dispose();
    super.dispose();
  }
}
