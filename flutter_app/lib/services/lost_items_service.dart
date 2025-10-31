import 'package:supabase_flutter/supabase_flutter.dart';
import 'dart:typed_data';
import 'package:mime/mime.dart' as mime;

class LostItem {
  final String id;
  final String userId;
  final String title;
  final String? description;
  final String? category;
  final String? location;
  final DateTime? lostAt;
  final String? imageUrl;
  final String status;
  final DateTime createdAt;

  LostItem({
    required this.id,
    required this.userId,
    required this.title,
    this.description,
    this.category,
    this.location,
    this.lostAt,
    this.imageUrl,
    this.status = 'open',
    required this.createdAt,
  });

  factory LostItem.fromMap(Map<String, dynamic> m) => LostItem(
        id: m['id'],
        userId: m['user_id'],
        title: m['title'],
        description: m['description'],
        category: m['category'],
        location: m['location'],
        lostAt: m['lost_at'] != null ? DateTime.parse(m['lost_at']) : null,
        imageUrl: m['image_url'],
        status: m['status'] ?? 'open',
        createdAt: DateTime.parse(m['created_at']),
      );
}

class LostItemsService {
  final _client = Supabase.instance.client;

  Future<LostItem> create({
    required String title,
    String? description,
    String? category,
    String? location,
    DateTime? lostAt,
    String? imageUrl,
  }) async {
    final user = _client.auth.currentUser;
    if (user == null) throw AuthException('Not authenticated');

    final insert = {
      'user_id': user.id,
      'title': title.trim(),
      'description': description?.trim().isEmpty == true ? null : description?.trim(),
      'category': category?.trim().isEmpty == true ? null : category?.trim(),
      'location': location?.trim().isEmpty == true ? null : location?.trim(),
      'lost_at': lostAt?.toIso8601String(),
      'image_url': imageUrl,
    };

    try {
      final data = await _client.from('lost_items').insert(insert).select().single();
      return LostItem.fromMap(data);
    } on PostgrestException catch (e) {
      final isCheckViolation = e.code == '23514' && (e.message ?? '').contains('lost_items_category_check');
      if (isCheckViolation) {
        final retry = Map<String, dynamic>.from(insert)..['category'] = null;
        final data = await _client.from('lost_items').insert(retry).select().single();
        return LostItem.fromMap(data);
      }
      rethrow;
    }
  }

  Future<List<LostItem>> fetchFeed({int limit = 50}) async {
    final rows = await _client
        .from('lost_items')
        .select()
        .order('created_at', ascending: false)
        .limit(limit);

    return (rows as List).map((e) => LostItem.fromMap(e)).toList();
  }

  RealtimeChannel subscribe(void Function(LostItem) onInsert) {
    final ch = _client
        .channel('public:lost_items')
        .onPostgresChanges(
          event: PostgresChangeEvent.insert,
          schema: 'public',
          table: 'lost_items',
          callback: (payload) {
            final rec = payload.newRecord as Map<String, dynamic>;
            onInsert(LostItem.fromMap(rec));
          },
        )
        .subscribe();
    return ch;
  }

  Future<String> uploadImage({
    required Uint8List bytes,
    required String userId,
    String? fileName,
  }) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    final name = fileName ?? 'img_$now.jpg';
    final path = 'lost/$userId/$now-$name';

    final contentType = mime.lookupMimeType('', headerBytes: bytes) ?? 'image/jpeg';

    await _client.storage.from('lost-items').uploadBinary(
      path,
      bytes,
      fileOptions: FileOptions(contentType: contentType),
    );

    return _client.storage.from('lost-items').getPublicUrl(path);
  }
}
