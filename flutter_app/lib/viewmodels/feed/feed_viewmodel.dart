import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class FeedItem {
  final String id;
  final String userId;
  final String title;
  final String? location;
  final String? category;
  final String? imageUrl;
  final DateTime createdAt;
  final String? ownerName; 
  final String? ownerEmail; 

  FeedItem({
    required this.id,
    required this.userId,
    required this.title,
    this.location,
    this.category,
    this.imageUrl,
    required this.createdAt,
    this.ownerName,
    this.ownerEmail,
  });

  factory FeedItem.fromJson(Map<String, dynamic> json) => FeedItem(
        id: json['id'] as String,
        userId: json['user_id'] as String,
        title: json['title'] as String,
        location: json['location'] as String?,
        category: json['category'] as String?,
        imageUrl: json['image_url'] as String?,
        createdAt: DateTime.parse(json['created_at'] as String),

  
        ownerName: json['profiles']?['name'] ?? json['owner_name'],
        ownerEmail: json['profiles']?['email'] ?? json['owner_email'],
      );
}

class FeedViewModel extends ChangeNotifier {
  final _client = Supabase.instance.client;

  final List<FeedItem> items = [];
  bool isLoading = false;
  String? error;
  RealtimeChannel? _channel;

  Future<void> init() async {
    await load();
    _subscribeRealtime();
  }

 
  Future<void> load() async {
    try {
      isLoading = true;
      error = null;
      notifyListeners();

      
      final res = await _client
          .from('lost_items')
          .select('''
            id,
            user_id,
            title,
            location,
            category,
            image_url,
            created_at,
            profiles!lost_items_user_id_fkey(name,email)
          ''')
          .order('created_at', ascending: false);

      items
        ..clear()
        ..addAll((res as List)
            .map((e) => FeedItem.fromJson(e as Map<String, dynamic>)));
    } on PostgrestException catch (e) {
      error = e.message;
    } catch (e) {
      error = 'Unexpected error loading feed: $e';
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

 
  void _subscribeRealtime() {
    _channel?.unsubscribe();
    _channel = _client.channel('public:lost_items')
      ..onPostgresChanges(
        event: PostgresChangeEvent.insert,
        schema: 'public',
        table: 'lost_items',
        callback: (payload) {
          final newRow = payload.newRecord;
          if (newRow != null) {
            items.insert(0, FeedItem.fromJson(newRow));
            notifyListeners();
          }
        },
      )
      ..subscribe();
  }

  @override
  void dispose() {
    _channel?.unsubscribe();
    super.dispose();
  }
}
