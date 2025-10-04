// lib/models/lost_item.dart
// Modelo base para items perdidos (feed + detalle)

class LostItem {
  final String id;               // uuid (PK)
  final String userId;           // uuid (quien reporta)
  final String title;            // título
  final String? description;     // descripción
  final String? location;        // lugar (texto corto)
  final String? category;        // ej. "Clothing", "Electronics"
  final String? imageUrl;        // URL pública o ruta de Storage
  final DateTime? lostAt;        // fecha que el usuario selecciona
  final DateTime createdAt;      // now() en DB
  final bool isClaimed;          // si ya fue reclamado
  final String? claimedById;     // uuid del que reclama
  final DateTime? claimedAt;     // cuándo se marcó como entregado

  const LostItem({
    required this.id,
    required this.userId,
    required this.title,
    this.description,
    this.location,
    this.category,
    this.imageUrl,
    this.lostAt,
    required this.createdAt,
    this.isClaimed = false,
    this.claimedById,
    this.claimedAt,
  });

  // ---------- Helpers de parse seguro ----------
  static DateTime? _toDate(dynamic v) {
    if (v == null) return null;
    if (v is DateTime) return v;
    return DateTime.parse(v.toString());
  }

  // ---------- Factory desde Supabase (row con snake_case) ----------
  factory LostItem.fromMap(Map<String, dynamic> row) {
    return LostItem(
      id: row['id'] as String,
      userId: row['user_id'] as String,
      title: row['title'] as String,
      description: row['description'] as String?,
      location: row['location'] as String?,
      category: row['category'] as String?,
      imageUrl: row['image_url'] as String?,
      lostAt: _toDate(row['lost_at']),
      createdAt: _toDate(row['created_at']) ?? DateTime.now(),
      isClaimed: (row['is_claimed'] as bool?) ?? false,
      claimedById: row['claimed_by_id'] as String?,
      claimedAt: _toDate(row['claimed_at']),
    );
  }

  /// Para `insert`/`update` en Supabase (usa snake_case).
  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'user_id': userId,
      'title': title,
      'description': description,
      'location': location,
      'category': category,
      'image_url': imageUrl,
      'lost_at': lostAt?.toIso8601String(),
      'created_at': createdAt.toIso8601String(),
      'is_claimed': isClaimed,
      'claimed_by_id': claimedById,
      'claimed_at': claimedAt?.toIso8601String(),
    };
  }

  LostItem copyWith({
    String? id,
    String? userId,
    String? title,
    String? description,
    String? location,
    String? category,
    String? imageUrl,
    DateTime? lostAt,
    DateTime? createdAt,
    bool? isClaimed,
    String? claimedById,
    DateTime? claimedAt,
  }) {
    return LostItem(
      id: id ?? this.id,
      userId: userId ?? this.userId,
      title: title ?? this.title,
      description: description ?? this.description,
      location: location ?? this.location,
      category: category ?? this.category,
      imageUrl: imageUrl ?? this.imageUrl,
      lostAt: lostAt ?? this.lostAt,
      createdAt: createdAt ?? this.createdAt,
      isClaimed: isClaimed ?? this.isClaimed,
      claimedById: claimedById ?? this.claimedById,
      claimedAt: claimedAt ?? this.claimedAt,
    );
  }
}
