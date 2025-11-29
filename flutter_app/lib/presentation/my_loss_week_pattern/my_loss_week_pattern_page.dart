import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../viewmodels/my_loss_week_pattern/my_loss_week_pattern_viewmodel.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';

class MyLossWeekPatternPage extends StatelessWidget {
  const MyLossWeekPatternPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => MyLossWeekPatternViewModel(
        client: Supabase.instance.client,
      )..load(),
      child: const _Body(),
    );
  }
}

class _Body extends StatelessWidget {
  const _Body({super.key});

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<MyLossWeekPatternViewModel>();

    return Scaffold(
      appBar: const TopBar(
        title: 'My Weekly Loss Pattern',
        actions: [DebugNavButton()],
      ),
      body: vm.isLoading
          ? const Center(child: CircularProgressIndicator())
          : vm.error != null
              ? Center(child: Text(vm.error!))
              : Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Number of lost items per weekday:',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 16),
                      Expanded(
                        child: ListView.separated(
                          itemCount: vm.stats.length,
                          separatorBuilder: (_, __) =>
                              const SizedBox(height: 10),
                          itemBuilder: (_, index) {
                            final s = vm.stats[index];
                            final maxCount = vm.stats
                                    .map((e) => e.count)
                                    .fold<int>(0, (a, b) => a > b ? a : b) +
                                1;

                            return Card(
                              child: Padding(
                                padding: const EdgeInsets.all(12),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      s.weekdayLabel,
                                      style: const TextStyle(
                                        fontSize: 16,
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                    const SizedBox(height: 8),
                                    LinearProgressIndicator(
                                      value: s.count / maxCount,
                                    ),
                                    const SizedBox(height: 4),
                                    Text('${s.count} reports'),
                                  ],
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                    ],
                  ),
                ),
    );
  }
}
