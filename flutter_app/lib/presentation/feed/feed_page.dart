import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../viewmodels/feed/feed_viewmodel.dart';

class FeedPage extends StatefulWidget {
  const FeedPage({super.key});

  @override
  State<FeedPage> createState() => _FeedPageState();
}

class _FeedPageState extends State<FeedPage> {
  @override
  void initState() {
    super.initState();
    // Cargar y suscribirse cuando el provider esté creado
    // Usamos addPostFrame para tener contexto válido
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final vm = context.read<FeedViewModel>();
      vm.load();
      vm.subscribeRealtime(); // puedes quitarlo si no quieres realtime
    });
  }

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => FeedViewModel(),
      child: const _FeedBody(),
    );
  }
}

class _FeedBody extends StatelessWidget {
  const _FeedBody();

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<FeedViewModel>();

    return Scaffold(
      appBar: AppBar(title: const Text('Goatfound')),
      body: RefreshIndicator(
        onRefresh: () => context.read<FeedViewModel>().load(),
        child: Builder(
          builder: (_) {
            if (vm.isLoading && vm.items.isEmpty) {
              return const Center(child: CircularProgressIndicator());
            }
            if (vm.error != null && vm.items.isEmpty) {
              return Center(child: Text('Error: ${vm.error}'));
            }
            if (vm.items.isEmpty) {
              return const Center(child: Text('No lost items reported yet.'));
            }
            return ListView.separated(
              padding: const EdgeInsets.all(12),
              itemCount: vm.items.length,
              separatorBuilder: (_, __) => const SizedBox(height: 10),
              itemBuilder: (_, i) {
                final it = vm.items[i];
                return Card(
                  child: ListTile(
            leading: it.imageUrl != null
            ? ClipRRect(
                borderRadius: BorderRadius.circular(6),
                    child: Image.network(
                  it.imageUrl!,
                  width: 56,
                  height: 56,
                  fit: BoxFit.cover,
                      ),
                    )
                      : const Icon(Icons.search),

                    title: Text(it.title),
                    subtitle: Text([
                      if (it.location != null && it.location!.isNotEmpty) '📍 ${it.location}',
                      if (it.category != null && it.category!.isNotEmpty) '🏷️ ${it.category}',
                      '🕒 ${it.createdAt.toLocal()}',
                    ].where((e) => e.isNotEmpty).join(' · ')),
                  ),
                );
              },
            );
          },
        ),
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: SizedBox(
            height: 52,
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () => Navigator.pushNamed(context, '/lost_report')
                  .then((_) => context.read<FeedViewModel>().load()),
              child: const Text('Report a lost item'),
            ),
          ),
        ),
      ),
    );
  }
}
