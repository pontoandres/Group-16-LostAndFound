import 'package:flutter/material.dart';
import '../../services/lost_items_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart'
    show RealtimeChannel; 



class FeedViewModel extends ChangeNotifier {
  final _service = LostItemsService();

  List<LostItem> items = [];
  bool isLoading = false;
  String? error;
  RealtimeChannel? _channel;

  Future<void> load() async {
    try {
      isLoading = true;
      error = null;
      notifyListeners();

      items = await _service.fetchFeed();
    } catch (e) {
      error = '$e';
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  /// Actualizaciones en tiempo real
  void subscribeRealtime() {
    _channel?.unsubscribe();
    _channel = _service.subscribe((newItem) {
      items = [newItem, ...items]; // prepend
      notifyListeners();
    });
  }

  @override
  void dispose() {
    _channel?.unsubscribe();
    super.dispose();
  }
}
