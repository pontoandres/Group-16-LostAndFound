import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../viewmodels/my_reports/my_reports_viewmodel.dart';
import '../../viewmodels/feed/feed_viewmodel.dart';
import '../../routes/app_routes.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';

class MyReportsPage extends StatelessWidget {
  const MyReportsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => MyReportsViewModel(
        client: Supabase.instance.client,
        connectivity: Connectivity(),
      )..load(),
      child: const _MyReportsBody(),
    );
  }
}

class _MyReportsBody extends StatelessWidget {
  const _MyReportsBody({super.key});

  @override
  Widget build(BuildContext context) {
    final isLoading =
        context.select<MyReportsViewModel, bool>((vm) => vm.isLoading);
    final isOffline =
        context.select<MyReportsViewModel, bool>((vm) => vm.isOffline);
    final error =
        context.select<MyReportsViewModel, String?>((vm) => vm.error);
    final items =
        context.select<MyReportsViewModel, List<FeedItem>>((vm) => vm.items);

    return Scaffold(
      appBar: const TopBar(
        title: 'My Reports',
        actions: [DebugNavButton()],
      ),
      body: Column(
        children: [
          if (isOffline)
            Container(
              width: double.infinity,
              color: Colors.orange.shade200,
              padding: const EdgeInsets.all(8),
              child: const Text(
                'You are offline. Showing cached reports.',
                textAlign: TextAlign.center,
              ),
            ),
          if (error != null)
            Padding(
              padding: const EdgeInsets.all(8),
              child: Text(
                error,
                style: const TextStyle(color: Colors.red),
              ),
            ),
          Expanded(
            child: isLoading && items.isEmpty
                ? const Center(child: CircularProgressIndicator())
                : items.isEmpty
                    ? const Center(
                        child: Text('You have not reported any items yet.'),
                      )
                    : ListView.separated(
                        padding: const EdgeInsets.all(12),
                        itemCount: items.length,
                        separatorBuilder: (_, __) =>
                            const SizedBox(height: 10),
                        itemBuilder: (_, index) {
                          final it = items[index];
                          return Card(
                            child: ListTile(
                              leading: it.imageUrl != null &&
                                      it.imageUrl!.isNotEmpty
                                  ? ClipRRect(
                                      borderRadius: BorderRadius.circular(6),
                                      child: Image.network(
                                        it.imageUrl!,
                                        width: 56,
                                        height: 56,
                                        fit: BoxFit.cover,
                                      ),
                                    )
                                  : const Icon(Icons.image_not_supported),
                              title: Text(it.title),
                              subtitle: Text(
                                it.createdAt.toLocal().toString(),
                              ),
                              onTap: () {
                                Navigator.pushNamed(
                                  context,
                                  AppRoutes.matchDetail,
                                  arguments: it,
                                );
                              },
                            ),
                          );
                        },
                      ),
          ),
        ],
      ),
    );
  }
}
