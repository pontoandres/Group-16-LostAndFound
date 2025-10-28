import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../services/lost_items_service.dart';

class LostReportViewModel extends ChangeNotifier {
  final titleCtrl = TextEditingController();
  final descriptionCtrl = TextEditingController();
  final locationCtrl = TextEditingController();
  final categoryCtrl = TextEditingController();

  Uint8List? imageBytes;
  String? imageUrl;
  DateTime? lostAt = DateTime.now();

  bool isLoading = false;
  String? error;

  final _client = Supabase.instance.client;
  final _service = LostItemsService();

  Future<void> pickImage({ImageSource source = ImageSource.gallery}) async {
    final picker = ImagePicker();
    final XFile? picked = await picker.pickImage(source: source, maxWidth: 1024);
    if (picked != null) {
      imageBytes = await picked.readAsBytes();
      notifyListeners();
    }
  }

  void setLostAt(DateTime d) {
    lostAt = d;
    notifyListeners();
  }

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

      if (imageBytes != null) {
        imageUrl = await _service.uploadImage(bytes: imageBytes!, userId: user.id);
      }

      await _service.create(
        title: title,
        description: descriptionCtrl.text,
        category: categoryCtrl.text,
        location: locationCtrl.text,
        lostAt: lostAt,
        imageUrl: imageUrl,
      );

      _clear();
      return true;
    } catch (e) {
      error = 'Error: $e';
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
