import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../viewmodels/feed/feed_viewmodel.dart';
import '../../routes/app_routes.dart';

class FeedPage extends StatelessWidget {
  const FeedPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => FeedViewModel()..init(),
      child: const _FeedBody(),
    );
  }
}

class _FeedBody extends StatefulWidget {
  const _FeedBody();

  @override
  State<_FeedBody> createState() => _FeedBodyState();
}

class _FeedBodyState extends State<_FeedBody> {
  bool _warningShown = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _maybeShowNightWarning());
  }

  void _maybeShowNightWarning() {
    if (_warningShown) return;
    final now = DateTime.now();
    if (now.hour >= 18) {
      _warningShown = true;
      final messenger = ScaffoldMessenger.of(context);

      messenger.showMaterialBanner(
        MaterialBanner(
          content: const Text('Careful! At night it’s easier to lose your belongings.'),
          leading: const Icon(Icons.nightlight_round),
          actions: [
            TextButton(
              onPressed: () => messenger.hideCurrentMaterialBanner(),
              child: const Text('OK'),
            ),
          ],
          backgroundColor: Colors.amber.shade100,
        ),
      );

      Future.delayed(const Duration(seconds: 6), () {
        if (mounted) {
          ScaffoldMessenger.of(context).hideCurrentMaterialBanner();
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<FeedViewModel>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Goatfound'),
        backgroundColor: const Color.fromARGB(255, 87, 195, 199),
        leading: Builder(
          builder: (context) => IconButton(
            icon: const Icon(Icons.menu, color: Colors.white, size: 30),
            onPressed: () => Scaffold.of(context).openDrawer(),
          ),
        ),
      ),
      drawer: Drawer(
        child: ListView(
          children: [
            const DrawerHeader(
              decoration: BoxDecoration(color: Colors.blueGrey),
              child: Text('GoatFound Menu', style: TextStyle(color: Colors.white)),
            ),
            ListTile(
              leading: const Icon(Icons.bar_chart),
              title: const Text('Statistics'),
              onTap: () {
                Navigator.pop(context);
                Navigator.pushNamed(context, AppRoutes.reportsByFaculty);
              },
            ),
            ListTile(
  leading: const Icon(Icons.category_outlined),
  title: const Text('Category Statistics'),
  onTap: () {
    Navigator.pop(context);
    Navigator.pushNamed(context, AppRoutes.categoryStats);
  },
),
  ListTile(
    leading: const Icon(Icons.change_circle),
    title: const Text('Password Changes (by Faculty)'),
    onTap: () {
      Navigator.pop(context);
      Navigator.pushNamed(context, AppRoutes.passwordChangesByFaculty);
    },
  ),

          ],
        ),
      ),
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
                              errorBuilder: (_, __, ___) => const Icon(Icons.broken_image),
                            ),
                          )
                        : const Icon(Icons.search),
                    title: Text(it.title),
                    subtitle: Text([
                      if (it.location != null && it.location!.isNotEmpty) '📍 ${it.location}',
                      if (it.category != null && it.category!.isNotEmpty) '🏷️ ${it.category}',
                      '🕒 ${it.createdAt.toLocal()}',
                    ].where((e) => e.isNotEmpty).join(' · ')),
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
              onPressed: () => Navigator.pushNamed(context, AppRoutes.lostReport)
                  .then((_) => context.read<FeedViewModel>().load()),
              child: const Text('Report a lost item'),
            ),
          ),
        ),
      ),
    );
  }
}
