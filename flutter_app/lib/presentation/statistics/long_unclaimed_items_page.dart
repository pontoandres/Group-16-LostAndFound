import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class LongUnclaimedItemsPage extends StatelessWidget {
  const LongUnclaimedItemsPage({super.key});

  Future<List<Map<String, dynamic>>> _loadData() async {
    final client = Supabase.instance.client;

    final data = await client
        .from('lost_items')
        .select(
          'id, title, description, place, category, image_url, status, created_at, lost_at, location',
        )
        .eq('status', 'pending') // <-- aún no reclamados
        .order('created_at', ascending: true) // más antiguos primero
        .limit(50);

    return (data as List).cast<Map<String, dynamic>>();
  }

  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Longest unclaimed items'),
        backgroundColor: const Color.fromARGB(255, 87, 195, 199),
      ),
      body: FutureBuilder<List<Map<String, dynamic>>>(
        future: _loadData(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('Error loading data: ${snapshot.error}'));
          }

          final items = snapshot.data ?? [];
          if (items.isEmpty) {
            return const Center(
              child: Text('There are no pending (unclaimed) items.'),
            );
          }

          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: items.length,
            separatorBuilder: (_, __) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final it = items[index];

              final createdAtRaw = it['created_at']?.toString();
              DateTime? createdAt;
              if (createdAtRaw != null) {
                createdAt = DateTime.tryParse(createdAtRaw);
              }

              final ageDays = createdAt != null
                  ? now.difference(createdAt).inDays
                  : null;

              final title = (it['title'] ?? 'Untitled').toString();
              final category = (it['category'] ?? '').toString();
              final place = (it['place'] ?? '').toString();
              final location = (it['location'] ?? '').toString();
              final description = (it['description'] ?? '').toString();

              final subtitleParts = <String>[];
              if (place.isNotEmpty) subtitleParts.add(place);
              if (location.isNotEmpty) subtitleParts.add(location);
              if (category.isNotEmpty) subtitleParts.add(category);
              if (createdAt != null) {
                subtitleParts
                    .add('Created: ${createdAt.toLocal().toString().split(".").first}');
              }

              final subtitleText = subtitleParts.join(' · ');
              final ageText = ageDays != null
                  ? 'Unclaimed for $ageDays day${ageDays == 1 ? '' : 's'}'
                  : 'Unclaimed';

              return Card(
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: Colors.orange.shade200,
                    child: Text(
                      '#${index + 1}',
                      style: const TextStyle(fontSize: 12),
                    ),
                  ),
                  title: Text(title),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (subtitleText.isNotEmpty) Text(subtitleText),
                      if (description.isNotEmpty) ...[
                        const SizedBox(height: 4),
                        Text(
                          description,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                      const SizedBox(height: 4),
                      Text(
                        ageText,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          color: Colors.redAccent,
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
