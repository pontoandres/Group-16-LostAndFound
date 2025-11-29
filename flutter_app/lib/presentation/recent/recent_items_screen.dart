// lib/presentation/recent/recent_items_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../viewmodels/recent/recent_items_viewmodel.dart';

class RecentItemsScreen extends StatelessWidget {
  const RecentItemsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => RecentItemsViewModel()..load(),
      child: const _RecentItemsBody(),
    );
  }
}

class _RecentItemsBody extends StatelessWidget {
  const _RecentItemsBody();

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<RecentItemsViewModel>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Recently viewed items'),
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () => context.read<RecentItemsViewModel>().load(),
          child: vm.isLoading && vm.items.isEmpty
              ? const Center(child: CircularProgressIndicator())
              : vm.items.isEmpty
                  ? const Center(
                      child: Text(
                        'No recently viewed items yet.\nOpen some items from the feed.',
                        textAlign: TextAlign.center,
                      ),
                    )
                  : ListView.separated(
                      padding: const EdgeInsets.all(16),
                      itemCount: vm.items.length,
                      separatorBuilder: (_, __) =>
                          const SizedBox(height: 12),
                      itemBuilder: (context, index) {
                        final it = vm.items[index];
                        return Card(
                          child: ListTile(
                            leading: it['image_url'] != null
                                ? ClipRRect(
                                    borderRadius: BorderRadius.circular(6),
                                    child: Image.network(
                                      it['image_url'],
                                      width: 56,
                                      height: 56,
                                      fit: BoxFit.cover,
                                      errorBuilder: (_, __, ___) =>
                                          const Icon(Icons.broken_image),
                                    ),
                                  )
                                : const Icon(Icons.search),
                            title: Text(it['title'] ?? 'Untitled'),
                            subtitle: Text(
                              [
                                if ((it['location'] ?? '').toString().isNotEmpty)
                                  it['location'],
                                if ((it['category'] ?? '').toString().isNotEmpty)
                                  it['category'],
                              ].where((e) => e != null && e.toString().isNotEmpty)
                               .join(' • '),
                            ),
                            onTap: () {
                            },
                          ),
                        );
                      },
                    ),
        ),
      ),
    );
  }
}
