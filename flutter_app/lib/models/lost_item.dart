class LostItem {
  final String id;
  final String userId;
  final String title;
  final String? description;
  final String? location;
  final String? category;
  final String? imageUrl;
  final DateTime? lostAt;
  final DateTime createdAt;
  final bool isClaimed;
  final String? claimedById;
  final DateTime? claimedAt;
  final String? ownerName;
  final String? ownerEmail;

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
    this.ownerName,
    this.ownerEmail,
  });

  static DateTime? _toDate(dynamic v) {
    if (v == null) return null;
    if (v is DateTime) return v;
    return DateTime.parse(v.toString());
  }

  factory LostItem.fromMap(Map<String, dynamic> row) {
    final profile = row['profiles'] ?? {};
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
      ownerName: profile['name'] ?? row['owner_name'],
      ownerEmail: profile['email'] ?? row['owner_email'],
    );
  }

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
}
