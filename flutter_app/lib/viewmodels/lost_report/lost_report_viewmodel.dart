import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/foundation.dart';
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

  late final Future<List<String>> categoriesFuture;
  final _descRemainingCtrl = StreamController<int>.broadcast();
  Stream<int> get descRemainingStream => _descRemainingCtrl.stream;

  LostReportViewModel() {
    categoriesFuture = _loadCategories();
    descriptionCtrl.addListener(_onDescriptionChanged);
    _onDescriptionChanged();
  }

  void _onDescriptionChanged() {
    final remaining = 300 - descriptionCtrl.text.characters.length;
    _descRemainingCtrl.add(remaining);
  }

  Future<List<String>> _loadCategories() async {
    final data = await _client.from('categories').select('name').order('name');
    final list = (data as List).map((e) => (e['name'] ?? '').toString()).where((e) => e.isNotEmpty).toList();
    if (list.isEmpty) {
      return ['Backpack', 'Keys', 'Card', 'Laptop', 'Headphones', 'Bottle', 'Calculator'];
    }
    return list;
  }

  Future<void> pickImage({ImageSource source = ImageSource.gallery}) async {
    final picker = ImagePicker();
    final XFile? picked = await picker.pickImage(source: source, maxWidth: 1280);
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

  static String _smartTitleIsolate(Map<String, String> m) {
    final d = m['description'] ?? '';
    final loc = m['location'] ?? '';
    final words = d
        .toLowerCase()
        .replaceAll(RegExp(r'[^a-z0-9áéíóúñ\s]'), ' ')
        .split(RegExp(r'\s+'))
        .where((w) => w.length >= 4)
        .toList();
    words.sort((a, b) => b.length.compareTo(a.length));
    final top = words.take(3).toList();
    final base = top.isEmpty ? 'Lost item' : top.join(' ');
    if (loc.trim().isEmpty) return base[0].toUpperCase() + base.substring(1);
    return '${base[0].toUpperCase()}${base.substring(1)} at $loc';
  }

  Future<void> suggestSmartTitle() async {
    final suggestion = await compute<Map<String, String>, String>(
      _smartTitleIsolate,
      {
        'description': descriptionCtrl.text,
        'location': locationCtrl.text,
      },
    );
    if (titleCtrl.text.trim().isEmpty) {
      titleCtrl.text = suggestion;
    } else {
      titleCtrl.text = '$suggestion';
    }
    notifyListeners();
  }

  void _clear() {
    titleCtrl.clear();
    descriptionCtrl.clear();
    locationCtrl.clear();
    categoryCtrl.clear();
    imageBytes = null;
    imageUrl = null;
    lostAt = DateTime.now();
    _onDescriptionChanged();
  }

  @override
  void dispose() {
    titleCtrl.dispose();
    descriptionCtrl.dispose();
    locationCtrl.dispose();
    categoryCtrl.dispose();
    _descRemainingCtrl.close();
    super.dispose();
  }
}
